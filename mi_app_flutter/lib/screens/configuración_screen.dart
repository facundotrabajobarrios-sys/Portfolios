import 'package:flutter/material.dart';
import '../services/supabase_service.dart';

class ConfiguracionScreen extends StatefulWidget {
  final String usuario;
  final int usuId; // ID del usuario logueado
  const ConfiguracionScreen({required this.usuario, required this.usuId, super.key});

  @override
  State<ConfiguracionScreen> createState() => _ConfiguracionScreenState();
}

class _ConfiguracionScreenState extends State<ConfiguracionScreen> {
  bool loading = true;
  Map<String, dynamic>? datosUsuario;

  @override
  void initState() {
    super.initState();
    cargarDatosUsuario();
  }

  Future<void> cargarDatosUsuario() async {
    setState(() => loading = true);
    datosUsuario = await SupabaseService.obtenerUsuarioPorId(widget.usuId);
    setState(() => loading = false);
  }

  void abrirFormulario({bool editar = false}) {
    showDialog(
      context: context,
      builder: (_) => AlertDialog(
        title: Text(editar ? 'Editar Usuario' : 'Agregar Usuario'),
        content: UsuarioForm(
          usuario: editar ? datosUsuario : null,
          onSaved: () async {
            Navigator.pop(context);
            if (editar) await cargarDatosUsuario();
          },
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Configuración')),
      body: loading
          ? const Center(child: CircularProgressIndicator())
          : Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.start,
                children: [
                  ElevatedButton.icon(
                    onPressed: () => abrirFormulario(editar: true),
                    icon: const Icon(Icons.edit),
                    label: const Text('Editar Usuario Actual'),
                    style: ElevatedButton.styleFrom(
                      minimumSize: const Size(double.infinity, 50),
                      backgroundColor: Colors.orange,
                    ),
                  ),
                  const SizedBox(height: 20),
                  ElevatedButton.icon(
                    onPressed: () => abrirFormulario(editar: false),
                    icon: const Icon(Icons.person_add),
                    label: const Text('Agregar Nuevo Usuario'),
                    style: ElevatedButton.styleFrom(
                      minimumSize: const Size(double.infinity, 50),
                      backgroundColor: Colors.green,
                    ),
                  ),
                ],
              ),
            ),
    );
  }
}

// ---------------------------------------------------
// Formulario reutilizable para agregar/editar usuario
// ---------------------------------------------------
class UsuarioForm extends StatefulWidget {
  final Map<String, dynamic>? usuario;
  final VoidCallback onSaved;
  const UsuarioForm({this.usuario, required this.onSaved, super.key});

  @override
  State<UsuarioForm> createState() => _UsuarioFormState();
}

class _UsuarioFormState extends State<UsuarioForm> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController nombreController;
  late TextEditingController apellidoController;
  late TextEditingController correoController;
  late TextEditingController telController;
  late TextEditingController passController;
  late TextEditingController rolController;

  @override
  void initState() {
    super.initState();
    nombreController = TextEditingController(text: widget.usuario?['user_nombre'] ?? '');
    apellidoController = TextEditingController(text: widget.usuario?['user_apellido'] ?? '');
    correoController = TextEditingController(text: widget.usuario?['user_correo'] ?? '');
    telController = TextEditingController(text: widget.usuario?['user_tel'] ?? '');
    passController = TextEditingController(text: widget.usuario?['user_pass'] ?? '');
    rolController = TextEditingController(text: widget.usuario?['user_rol'] ?? '');
  }

  @override
  void dispose() {
    nombreController.dispose();
    apellidoController.dispose();
    correoController.dispose();
    telController.dispose();
    passController.dispose();
    rolController.dispose();
    super.dispose();
  }

  void guardarUsuario() async {
    if (!_formKey.currentState!.validate()) return;

    Map<String, dynamic> data = {
      'user_nombre': nombreController.text.trim(),
      'user_apellido': apellidoController.text.trim(),
      'user_correo': correoController.text.trim(),
      'user_tel': telController.text.trim(),
      'user_pass': passController.text.trim(),
      'user_rol': rolController.text.trim(),
    };

    bool success;
    if (widget.usuario != null) {
      // actualizar
      success = await SupabaseService.actualizarUsuario(widget.usuario!['user_id'], data);
    } else {
      // insertar
      success = await SupabaseService.insertarUsuario(data);
    }

    if (success) widget.onSaved();
  }

  @override
  Widget build(BuildContext context) {
    return Form(
      key: _formKey,
      child: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextFormField(controller: nombreController, decoration: const InputDecoration(labelText: 'Nombre'), validator: (v) => v!.isEmpty ? 'Requerido' : null),
            TextFormField(controller: apellidoController, decoration: const InputDecoration(labelText: 'Apellido'), validator: (v) => v!.isEmpty ? 'Requerido' : null),
            TextFormField(controller: correoController, decoration: const InputDecoration(labelText: 'Correo'), validator: (v) => v!.isEmpty ? 'Requerido' : null),
            TextFormField(controller: telController, decoration: const InputDecoration(labelText: 'Teléfono')),
            TextFormField(controller: passController, decoration: const InputDecoration(labelText: 'Contraseña'), obscureText: true),
            TextFormField(controller: rolController, decoration: const InputDecoration(labelText: 'Rol')),
            const SizedBox(height: 15),
            ElevatedButton(
              onPressed: guardarUsuario,
              child: Text(widget.usuario != null ? 'Actualizar' : 'Agregar'),
            ),
          ],
        ),
      ),
    );
  }
}
