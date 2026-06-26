from odoo import fields, models


class ResUsers(models.Model):
    #_inherit es para extender el modelo res.users, no para crear uno nuevo
    _inherit = 'res.users'

    property_ids = fields.One2many(
        'estate.property',
        'seller_id',
        string='Propiedades a la venta',
        domain=[('state', 'in', ['new', 'offer_received'])],
    )