from datetime import timedelta

from odoo import api, fields, models
from odoo.exceptions import UserError


class EstatePropertyOffer(models.Model):
    #information about the model
    _name = 'estate.property.offer'
    _description = 'Oferta de Propiedad'
    _order = 'price desc'
    _sql_constraints = [
        ('check_price_positive', 'CHECK(price > 0)',
         'El precio de la oferta debe ser estrictamente positivo.'),
    ]
    #fields basics
    price = fields.Float(string='Precio')
    status = fields.Selection(
        selection=[
            ('accepted', 'Aceptado'),
            ('refused', 'Rechazado'),
        ],
        string='Estado',
        copy=False,
    )
    validity = fields.Integer(string='Validez (días)', default=7, readonly=True)
    date_deadline = fields.Date(
        string='Fecha límite',
        compute='_compute_date_deadline',
        #se usa inverse solo cuando el campo compute depende de otros campos que pueden ser modificados por el usuario. 
        inverse='_inverse_date_deadline',
        store=True,
    )

    # Relational fields
    partner_id = fields.Many2one('res.partner', string='Comprador', required=True)
    property_id = fields.Many2one('estate.property', string='Propiedad', required=True)
    property_type_id = fields.Many2one(
        'estate.property.type',
        related='property_id.property_type_id',
        string='Tipo de Propiedad',
        store=True,
    )

    # Compute methods
    @api.depends('create_date', 'validity')
    def _compute_date_deadline(self):
        for record in self:
            #si fecha de creación y validez están presentes, se calcula la fecha límite sumando la validez a la fecha de creación. 
            #Si no, se establece como False.
            if record.create_date and record.validity:
                create_date = record.create_date.date()
                record.date_deadline = create_date + timedelta(days=record.validity)
            else:
                record.date_deadline = False

    def _inverse_date_deadline(self):
        for record in self:
            #si la fecha de creación y la fecha límite están presentes, 
            #se calcula la validez restando la fecha de creación a la fecha límite. Si no, se establece como 0.
            if record.create_date and record.date_deadline:
                create_date = record.create_date.date()
                delta = record.date_deadline - create_date
                record.validity = delta.days
            else:
                record.validity = 0

    # Action methods
    def action_accept(self):
        for record in self:
            #en other_offers se almacenan todas las ofertas relacionadas con la propiedad actual excepto la oferta que se está aceptando. 
            other_offers = record.property_id.offer_ids - record
            #refused significa rechazado, se actualiza el estado de las otras ofertas a 'refused'
            other_offers.write({'status': 'refused'})
            record.status = 'accepted'
            record.property_id.write({
                'buyer_id': record.partner_id.id,
                'selling_price': record.price,
                'state': 'offer_accepted',
            })
        return True

    def action_refuse(self):
        for record in self:
            record.status = 'refused'
        return True

    # CRUD overrides
    @api.model
    def create(self, vals):
        #vals ve los valores que se van a crear, property_id obtiene el id de la propiedad relacionada con la oferta. 
        property_id = vals.get('property_id')
        if property_id:
            #busca en el modelo 'estate.property' la propiedad con el id obtenido y la almacena en la variable property.
            property = self.env['estate.property'].browse(property_id)
            if property.offer_ids:# Si la propiedad ya tiene ofertas
                max_existing = max(property.offer_ids.mapped('price'))# Precio más alto actual
                if vals.get('price', 0) <= max_existing:# Si nueva oferta ≤ máxima existente
                    raise UserError('La oferta debe ser mayor que las ofertas existentes.')
            #se crea la oferta utilizando el método create del modelo padre (super()) y se almacena en la variable offer.
            offer = super().create(vals)
            property.state = 'offer_received' # Actualiza el estado de la propiedad a "Oferta recibida"
            return offer
        return super().create(vals)