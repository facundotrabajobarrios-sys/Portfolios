from odoo import api, fields, models


class EstatePropertyType(models.Model):
    _name = 'estate.property.type'
    _description = 'Tipo de Propiedad'
    _order = 'sequence, name'
    _sql_constraints = [
        ('unique_type_name', 'UNIQUE(name)',
         'El nombre del tipo de propiedad debe ser único.'),
    ]
    # Basic fields
    sequence = fields.Integer(string='Secuencia', default=10)
    name = fields.Char(string='Nombre', required=True)
    # Relational fields
    property_ids = fields.One2many('estate.property', 'property_type_id', string='Propiedades')
    offer_ids = fields.One2many(
        'estate.property.offer',
        'property_type_id',
        string='Ofertas',
    )
    # Compute methods
    offer_count = fields.Integer(
        string='Número de ofertas',
        compute='_compute_offer_count',
    )

    @api.depends('offer_ids')
    def _compute_offer_count(self):
        for record in self:
            record.offer_count = len(record.offer_ids)