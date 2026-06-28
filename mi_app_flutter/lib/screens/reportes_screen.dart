import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class ReportesScreen extends StatefulWidget {
  const ReportesScreen({super.key});

  @override
  State<ReportesScreen> createState() => _ReportesScreenState();
}

class _ReportesScreenState extends State<ReportesScreen> {
  final supabase = Supabase.instance.client;
  bool isLoading = true;
  List<Map<String, dynamic>> clientes = [];

  @override
  void initState() {
    super.initState();
    fetchClientes();
  }

  Future<void> fetchClientes() async {
    setState(() => isLoading = true);

    try {
      // Obtener todos los clientes
      final response = await supabase.from('clientes').select();
      final data = response as List<dynamic>;

      setState(() {
        clientes = data.map((e) => Map<String, dynamic>.from(e)).toList();
        isLoading = false;
      });
    } catch (e) {
      setState(() => isLoading = false);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error al obtener clientes: $e')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Reporte de Clientes'),
        backgroundColor: const Color(0xFF0D47A1),
      ),
      body: isLoading
          ? const Center(child: CircularProgressIndicator())
          : Padding(
              padding: const EdgeInsets.all(16),
              child: ListView.builder(
                itemCount: clientes.length,
                itemBuilder: (context, index) {
                  final cliente = clientes[index];
                  return Card(
                    margin: const EdgeInsets.symmetric(vertical: 8),
                    elevation: 3,
                    child: Padding(
                      padding: const EdgeInsets.all(12),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            "${cliente['cli_nom'] ?? 'N/A'} ${cliente['cli_ape'] ?? ''}",
                            style: const TextStyle(
                                fontSize: 16, fontWeight: FontWeight.bold),
                          ),
                          const SizedBox(height: 6),
                          Text("ID: ${cliente['cli_id'] ?? 'N/A'}"),
                          Text("Teléfono: ${cliente['cli_tel'] ?? 'N/A'}"),
                          Text("Email: ${cliente['cli_email'] ?? 'N/A'}"),
                          Text("Ciudad ID: ${cliente['ciu_cod'] ?? 'N/A'}"),
                          Text(
                              "Estado documento: ${cliente['estado_documento'] ?? 'N/A'}"),
                          Text("Observaciones: ${cliente['obs_doc'] ?? 'N/A'}"),
                          const SizedBox(height: 10),
                          Align(
                            alignment: Alignment.centerRight,
                            child: ElevatedButton.icon(
                              onPressed: () {
                                showDialog(
                                  context: context,
                                  builder: (context) => AlertDialog(
                                    title: Text(
                                        "${cliente['cli_nom'] ?? 'N/A'} ${cliente['cli_ape'] ?? ''}"),
                                    content: Column(
                                      mainAxisSize: MainAxisSize.min,
                                      crossAxisAlignment:
                                          CrossAxisAlignment.start,
                                      children: [
                                        Text("ID: ${cliente['cli_id'] ?? 'N/A'}"),
                                        Text(
                                            "Nombre: ${cliente['cli_nom'] ?? 'N/A'}"),
                                        Text(
                                            "Apellido: ${cliente['cli_ape'] ?? 'N/A'}"),
                                        Text(
                                            "Teléfono: ${cliente['cli_tel'] ?? 'N/A'}"),
                                        Text(
                                            "Email: ${cliente['cli_email'] ?? 'N/A'}"),
                                        Text(
                                            "Ciudad ID: ${cliente['ciu_cod'] ?? 'N/A'}"),
                                        Text(
                                            "Estado documento: ${cliente['estado_documento'] ?? 'N/A'}"),
                                        Text(
                                            "Observaciones: ${cliente['obs_doc'] ?? 'N/A'}"),
                                      ],
                                    ),
                                    actions: [
                                      TextButton(
                                        onPressed: () =>
                                            Navigator.pop(context),
                                        child: const Text("Cerrar"),
                                      )
                                    ],
                                  ),
                                );
                              },
                              icon: const Icon(Icons.visibility),
                              label: const Text('Ver'),
                            ),
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
    );
  }
}
