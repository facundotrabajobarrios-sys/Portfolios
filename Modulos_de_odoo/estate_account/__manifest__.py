{
    'name': 'Inmobiliaria - Contabilidad',
    'version': '1.0',
    'depends': [
        'estate',
        'account',
    ],
    'data': [
        'security/ir.model.access.csv',
        'data/account_data.xml',
    ],
    'installable': True,
    'application': False,  # No es una aplicación principal
    'license': 'LGPL-3',
}