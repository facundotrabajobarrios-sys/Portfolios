from datetime import date

from odoo import api, fields, models
from odoo.exceptions import UserError
from odoo.tools import float_compare, float_is_zero
from dateutil.relativedelta import relativedelta


class EstateProperty(models.Model):
    #information about the model
    _name = 'estate.property'
    _description = 'Propiedad Inmobiliaria'
    _order = 'id desc' #para poner en orden descendente
    _sql_constraints = [
        ('check_expected_price_positive', 'CHECK(expected_price > 0)',
         'El precio esperado debe ser estrictamente positivo.'),
        ('check_selling_price_positive', 'CHECK(selling_price >= 0)',
         'El precio de venta debe ser positivo.'),
    ]

    # Basic fields
    name = fields.Char(string='Nombre', required=True)
    description = fields.Text(string='Descripción')
    active = fields.Boolean(string='Activo', default=True)
    state = fields.Selection(
        selection=[
            ('new', 'Nuevo'),
            ('offer_received', 'Oferta recibida'),
            ('offer_accepted', 'Oferta aceptada'),
            ('sold', 'Vendido'),
            ('canceled', 'Cancelado'),
        ],
        string='Estado',
        default='new',
        required=True,
        copy=False,
    )

    # Address fields
    postal_code = fields.Char(string='Código Postal')

    # Date fields
    date_availability = fields.Date(
        string='Fecha Disponibilidad',
        #relativedelta se utiliza para calcular la fecha de disponibilidad sumando 3 meses a la fecha actual, lo que garantiza que la fecha de disponibilidad sea siempre válida incluso en meses con menos de 31 días.
        default=lambda self: fields.Date.today() + relativedelta(months=3), copy=False) 

    # Price fields
    #expected significa que es el precio que el vendedor espera recibir por la propiedad,
    #mientras que selling_price es el precio real al que se vende la propiedad.
    expected_price = fields.Float(string='Precio Esperado', required=True)
    selling_price = fields.Float(string='Precio de Venta', readonly=True, copy=False)
    best_price = fields.Float(string='Mejor oferta', compute='_compute_best_price')

    # Property characteristics
    bedrooms = fields.Integer(string='Dormitorios', default=2)
    living_area = fields.Integer(string='Área Viviente')
    facades = fields.Integer(string='Fachadas')
    garage = fields.Boolean(string='Garaje')
    garden = fields.Boolean(string='Jardín')
    garden_area = fields.Integer(string='Área del Jardín')
    garden_orientation = fields.Selection(
        selection=[
            ('north', 'Norte'),
            ('south', 'Sur'),
            ('east', 'Este'),
            ('west', 'Oeste'),
        ],
        string='Orientación del Jardín',
    )
    total_area = fields.Float(string='Área total (m²)', compute='_compute_total_area')

    # Relational fields
    property_type_id = fields.Many2one(
        'estate.property.type',
        string='Tipo de Propiedad',
        options="{'no_create': True, 'no_edit': True}",
    )
    buyer_id = fields.Many2one('res.partner', string='Comprador')
    seller_id = fields.Many2one(
        'res.partner',
        string='Vendedor',
        #env.user ve el usario actual, partner_id octiene el contato asociado al usuario actual y el id obtiene el id
        default=lambda self: self.env.user.partner_id.id,
    )
    tag_ids = fields.Many2many('estate.property.tag', string='Etiquetas')
    offer_ids = fields.One2many(
        'estate.property.offer',
        'property_id',
        string='Ofertas',
    )
    offer_count = fields.Integer(
        string='Número de ofertas',
        compute='_compute_offer_count',
    )

    # Compute methods
    @api.depends('living_area', 'garden_area')
    def _compute_total_area(self):
        for record in self:
            record.total_area = (record.living_area or 0) + (record.garden_area or 0)

    @api.depends('offer_ids.price')
    def _compute_best_price(self):
        for record in self:
            record.best_price = max(record.offer_ids.mapped('price')) if record.offer_ids else 0

    @api.depends('offer_ids')
    def _compute_offer_count(self):
        for record in self:
            record.offer_count = len(record.offer_ids)

    # Onchange methods
    @api.onchange('garden')
    def _onchange_garden(self):
        if self.garden:
            self.garden_area = 10
            self.garden_orientation = 'north'
        else:
            self.garden_area = False
            self.garden_orientation = False

    # Constraints
    @api.constrains('selling_price', 'expected_price')
    def _check_selling_price(self):
        for record in self:
            #pricision_digits=2 significa que se comparan los precios con una precisión de 2 decimales.
            if float_is_zero(record.selling_price, precision_digits=2):
                continue
            # el *0.9 significa que el precio de venta no puede ser inferior al 90% del precio esperado.
            min_price = record.expected_price * 0.9
            if float_compare(record.selling_price, min_price, precision_digits=2) < 0:
                raise UserError(
                    'El precio de venta no puede ser inferior al 90%% del precio esperado.\n'
                    'Precio esperado: %s, Precio mínimo permitido: %s'
                    % (record.expected_price, min_price)
                )

    # Action methods
    def action_sold(self):
        for record in self:
            if record.state == 'canceled':
                raise UserError('No se puede vender una propiedad cancelada.')
            record.state = 'sold'
        return True

    def action_cancel(self):
        for record in self:
            if record.state == 'sold':
                raise UserError('No se puede cancelar una propiedad vendida.')
            record.state = 'canceled'
        return True

    # CRUD overrides
    @api.ondelete(at_uninstall=False)
    def _unlink_if_allowed(self):
        for record in self:
            if record.state not in ['new', 'canceled']:
                raise UserError('Solo se pueden eliminar propiedades con estado "Nuevo" o "Cancelado".')