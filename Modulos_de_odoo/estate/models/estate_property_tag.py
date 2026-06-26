from odoo import fields, models


class EstatePropertyTag(models.Model):
    _name = 'estate.property.tag'
    _description = 'Etiqueta de Propiedad'
    _order = 'name'
    _sql_constraints = [
        ('unique_tag_name', 'UNIQUE(name)',
         'El nombre de la etiqueta debe ser único.'),
    ]

    name = fields.Char(string='Nombre', required=True)
    color = fields.Integer(string='Color')