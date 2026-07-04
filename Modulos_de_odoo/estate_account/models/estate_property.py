from odoo import api, fields, models
from odoo.exceptions import UserError


class EstateProperty(models.Model):
    _inherit = 'estate.property'

    def _get_income_account(self):
        #self.env.company es la forma recomendada en Odoo 15+ para obtener la compañía actual, en lugar de self.env.user.company_id
        company = self.env.company
        #buscar por códigos comunes de cuentas de ingresos en español
        account = self.env['account.account'].search([
            ('code', 'in', ['400000', '4000', '4', 'ING']),
            ('company_id', '=', company.id),
            ('account_type', '=', 'income'),
        ], limit=1)
        #si no se encuentra por código, buscar cualquier cuenta de ingresos disponible
        if not account:
            account = self.env['account.account'].search([
                ('account_type', '=', 'income'),
                ('company_id', '=', company.id),
            ], limit=1)
        if not account:
            raise UserError(
                'No se encontró ninguna cuenta de ingresos en el sistema.\n'
                'Por favor, verifica que el módulo de Contabilidad esté instalado\n'
                'y que exista al menos una cuenta de tipo "Ingresos".'
            )
        return account
    #busca o crea un diario de ventas específico para las ventas inmobiliarias
    #para guardar las ventas de propiedades, lo que facilita la contabilidad y el seguimiento de estas transacciones
    def _get_or_create_sale_journal(self):
        company = self.env.company
        journal = self.env['account.journal'].search([
            ('type', '=', 'sale'),
            ('company_id', '=', company.id),
        ], limit=1)
        if journal:
            return journal
        income_account = self._get_income_account()
        journal = self.env['account.journal'].create({
            'name': 'Ventas Inmobiliarias',
            'code': 'VENINM',
            'type': 'sale',
            'company_id': company.id,
        })
        return journal

    def action_sold(self):
        #action_sold es el método que se llama cuando se marca una propiedad como vendida. 
        #Aquí se crea la factura para el comprador con la comisión y las tasas administrativas.
        result = super().action_sold()
        if not self.buyer_id:
            return result
        #aqui se obtiene o crea el diario de ventas específico para las ventas inmobiliarias, y se obtiene la cuenta de ingresos para asignarla a las líneas de la factura. 
        sale_journal = self._get_or_create_sale_journal()
        income_account = self._get_income_account()
        #Luego se calcula la comisión (6% del precio de venta) y la tasa administrativa (100.00),
        admin_fee = 100.00
        #y se crean las líneas de factura correspondientes (prepara los datos de la factura).
        commission = self.selling_price * 0.06
        invoice_vals = {
            'partner_id': self.buyer_id.id,
            'move_type': 'out_invoice',
            'journal_id': sale_journal.id,
            'invoice_date': fields.Date.today(),
            'invoice_line_ids': [
                (0, 0, {
                    'name': 'Comisión de venta (6%%)',
                    'quantity': 1.0,
                    'price_unit': commission,
                    'account_id': income_account.id,
                }),
                (0, 0, {
                    'name': 'Tasas administrativas',
                    'quantity': 1.0,
                    'price_unit': admin_fee,
                    'account_id': income_account.id,
                }),
            ]
        }
        #Finalmente, se crea la factura en el sistema.
        self.env['account.move'].create(invoice_vals)
        return result