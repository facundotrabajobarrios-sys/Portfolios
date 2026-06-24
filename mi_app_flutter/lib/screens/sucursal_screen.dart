import 'package:flutter/material.dart';
import '../services/supabase_service.dart';

class SucursalScreen extends StatefulWidget {
  final String usuario;
  const SucursalScreen({required this.usuario, super.key});

  @override
  State<SucursalScreen> createState() => _SucursalScreenState();
}

class _SucursalScreenState extends State<SucursalScreen> {
  List<Map<String, dynamic>> sucursales = [];
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    cargarSucursales();
  }

  Future<void> cargarSucursales() async {
    setState(() => isLoading = true);
    try {
      final data = await SupabaseService.obtenerSucursales();
      setState(() => sucursales = data);
    } catch (e) {
      print('Error cargando sucursales: $e');
    } finally {
      setState(() => isLoading = false);
    }
  }

  void mostrarDialogoSucursal({Map<String, dynamic>? sucursal}) {
    final TextEditingController controlador = TextEditingController(
      text: sucursal != null ? sucursal['suc_descri'] : '',
    );

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(sucursal != null ? 'Editar Sucursal' : 'Añadir Sucursal'),
        content: TextField(
          controller: controlador,
          decoration: const InputDecoration(labelText: 'Descripción'),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancelar'),
          ),
          ElevatedButton(
            onPressed: () async {
              final descripcion = controlador.text.trim();
              if (descripcion.isEmpty) return;

              bool ok = false;
              if (sucursal != null) {
                // Editar
                ok = await SupabaseService.actualizarSucursal(
                  sucursal['suc_cod'],
                  {'suc_descri': descripcion},
                );
              } else {
                // Añadir
                ok = await SupabaseService.guardarSucursal(descripcion);
              }

              if (ok) {
                Navigator.pop(context);
                cargarSucursales();
              }
            },
            child: const Text('Guardar'),
          ),
        ],
      ),
    );
  }

  void toggleEstadoSucursal(Map<String, dynamic> sucursal) async {
    bool ok = await SupabaseService.cambiarEstadoSucursal(
      sucursal['suc_cod'],
      sucursal['suc_estado'],
    );
    if (ok) cargarSucursales();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Historial de Sucursales - ${widget.usuario}'),
        backgroundColor: const Color(0xFF0D47A1),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: isLoading
            ? const Center(child: CircularProgressIndicator())
            : sucursales.isEmpty
            ? const Center(child: Text('No hay sucursales registradas'))
            : ListView.builder(
                itemCount: sucursales.length,
                itemBuilder: (context, index) {
                  final suc = sucursales[index];
                  return Card(
                    elevation: 5,
                    margin: const EdgeInsets.symmetric(vertical: 8),
                    child: ListTile(
                      leading: const Icon(Icons.location_city),
                      title: Text(suc['suc_descri']),
                      subtitle: Text('Estado: ${suc['suc_estado']}'),
                      trailing: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          IconButton(
                            icon: const Icon(Icons.edit, color: Colors.orange),
                            onPressed: () =>
                                mostrarDialogoSucursal(sucursal: suc),
                          ),
                          IconButton(
                            icon: Icon(
                              suc['suc_estado'] == 'ACTIVO'
                                  ? Icons.delete
                                  : Icons.restore,
                              color: suc['suc_estado'] == 'ACTIVO'
                                  ? Colors.red
                                  : Colors.green,
                            ),
                            onPressed: () => toggleEstadoSucursal(suc),
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => mostrarDialogoSucursal(),
        backgroundColor: const Color(0xFF0D47A1),
        child: const Icon(Icons.add),
      ),
    );
  }
}
