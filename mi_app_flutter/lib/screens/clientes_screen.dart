import 'package:flutter/material.dart';
import 'package:pdf/pdf.dart';
import 'package:pdf/widgets.dart' as pw;
import 'package:printing/printing.dart';
import '../services/supabase_service.dart';

class ClientesScreen extends StatefulWidget {
  final String usuario;
  const ClientesScreen({required this.usuario, super.key});

  @override
  State<ClientesScreen> createState() => _ClientesScreenState();
}

class _ClientesScreenState extends State<ClientesScreen> {
  final _formKey = GlobalKey<FormState>();
  final TextEditingController nombreController = TextEditingController();
  final TextEditingController apellidoController = TextEditingController();
  final TextEditingController telefonoController = TextEditingController();
  final TextEditingController emailController = TextEditingController();
  int? ciudadSeleccionada;
  bool isLoading = false;

  List<Map<String, dynamic>> ciudades = [];
  List<Map<String, dynamic>> clientes = [];

  @override
  void initState() {
    super.initState();
    cargarCiudades();
    cargarClientes();
  }

  void cargarCiudades() async {
    final data = await SupabaseService.obtenerCiudades();
    setState(() {
      ciudades = data;
      if (ciudades.isNotEmpty) ciudadSeleccionada = ciudades[0]['ciu_cod'];
    });
  }

  void cargarClientes() async {
    final data = await SupabaseService.obtenerClientes();
    setState(() {
      clientes = data;
    });
  }

  void guardarCliente() async {
    if (_formKey.currentState!.validate()) {
      setState(() => isLoading = true);

      Map<String, dynamic> datosCliente = {
        "cli_nom": nombreController.text.trim(),
        "cli_ape": apellidoController.text.trim(),
        "cli_tel": telefonoController.text.trim(),
        "cli_email": emailController.text.trim(),
        "ciu_cod": ciudadSeleccionada,
      };

      bool success = await SupabaseService.guardarCliente(datosCliente);

      setState(() => isLoading = false);

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            success
                ? "Datos guardados correctamente"
                : "Error al guardar datos",
          ),
          backgroundColor: success ? Colors.green : Colors.red,
        ),
      );

      if (success) {
        nombreController.clear();
        apellidoController.clear();
        telefonoController.clear();
        emailController.clear();
        if (ciudades.isNotEmpty) ciudadSeleccionada = ciudades[0]['ciu_cod'];

        cargarClientes();
      }
    }
  }

  Future<void> generarPdf() async {
    final pdf = pw.Document();

    final fecha = DateTime.now();

    pdf.addPage(
      pw.MultiPage(
        pageFormat: PdfPageFormat.a4,
        margin: const pw.EdgeInsets.all(24),
        build: (context) => [
          // Cabecera
          pw.Column(
            crossAxisAlignment: pw.CrossAxisAlignment.start,
            children: [
              pw.Text(
                "Reporte de Clientes",
                style: pw.TextStyle(
                  fontSize: 22,
                  fontWeight: pw.FontWeight.bold,
                  color: PdfColors.blue900,
                ),
              ),
              pw.SizedBox(height: 5),
              pw.Text(
                "Generado por: ${widget.usuario}",
                style: const pw.TextStyle(fontSize: 12),
              ),
              pw.Text(
                "Fecha: ${fecha.day}/${fecha.month}/${fecha.year}",
                style: const pw.TextStyle(fontSize: 12),
              ),
              pw.Divider(),
              pw.SizedBox(height: 20),
            ],
          ),

          // Tabla de clientes
          pw.Table.fromTextArray(
            headers: ["#", "Nombre", "Apellido", "Teléfono", "Email", "Ciudad"],
            headerStyle: pw.TextStyle(
              fontWeight: pw.FontWeight.bold,
              color: PdfColors.white,
            ),
            headerDecoration: const pw.BoxDecoration(color: PdfColors.blue),
            cellHeight: 25,
            cellAlignments: {
              0: pw.Alignment.center,
              1: pw.Alignment.centerLeft,
              2: pw.Alignment.centerLeft,
              3: pw.Alignment.center,
              4: pw.Alignment.centerLeft,
              5: pw.Alignment.center,
            },
            data: List.generate(clientes.length, (index) {
              final c = clientes[index];
              return [
                (index + 1).toString(),
                c['cli_nom'] ?? '',
                c['cli_ape'] ?? '',
                c['cli_tel'] ?? '',
                c['cli_email'] ?? '',
                c['ciudad']?['ciu_descri'] ?? '',
              ];
            }),
          ),
        ],
      ),
    );

    await Printing.sharePdf(
      bytes: await pdf.save(),
      filename: 'reporte_clientes.pdf',
    );
  }

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 2, // Dos pestañas
      child: Scaffold(
        appBar: AppBar(
          title: Text('Clientes - ${widget.usuario}'),
          backgroundColor: const Color(0xFF0D47A1),
          bottom: const TabBar(
            tabs: [
              Tab(text: 'Datos de Clientes', icon: Icon(Icons.person)),
              Tab(text: 'Clientes Registrados', icon: Icon(Icons.list)),
            ],
          ),
        ),
        body: TabBarView(
          children: [
            // ----------------- FORMULARIO -----------------
            Padding(
              padding: const EdgeInsets.all(20),
              child: Form(
                key: _formKey,
                child: ListView(
                  children: [
                    TextFormField(
                      controller: nombreController,
                      decoration: const InputDecoration(
                        labelText: "Nombre",
                        prefixIcon: Icon(Icons.person),
                      ),
                      validator: (value) =>
                          value!.isEmpty ? "Ingrese el nombre" : null,
                    ),
                    const SizedBox(height: 10),
                    TextFormField(
                      controller: apellidoController,
                      decoration: const InputDecoration(
                        labelText: "Apellido",
                        prefixIcon: Icon(Icons.person_outline),
                      ),
                      validator: (value) =>
                          value!.isEmpty ? "Ingrese el apellido" : null,
                    ),
                    const SizedBox(height: 10),
                    TextFormField(
                      controller: telefonoController,
                      decoration: const InputDecoration(
                        labelText: "Teléfono",
                        prefixIcon: Icon(Icons.phone),
                      ),
                      validator: (value) =>
                          value!.isEmpty ? "Ingrese el teléfono" : null,
                    ),
                    const SizedBox(height: 10),
                    TextFormField(
                      controller: emailController,
                      decoration: const InputDecoration(
                        labelText: "Email",
                        prefixIcon: Icon(Icons.email),
                      ),
                      validator: (value) =>
                          value!.isEmpty ? "Ingrese el email" : null,
                    ),
                    const SizedBox(height: 10),
                    DropdownButtonFormField<int>(
                      initialValue: ciudadSeleccionada,
                      items: ciudades
                          .map(
                            (c) => DropdownMenuItem<int>(
                              value: c['ciu_cod'],
                              child: Text(c['ciu_descri']),
                            ),
                          )
                          .toList(),
                      onChanged: (val) =>
                          setState(() => ciudadSeleccionada = val),
                      decoration: const InputDecoration(
                        labelText: "Ciudad",
                        prefixIcon: Icon(Icons.location_city),
                      ),
                    ),
                    const SizedBox(height: 20),
                    ElevatedButton(
                      onPressed: isLoading ? null : guardarCliente,
                      child: isLoading
                          ? const CircularProgressIndicator(color: Colors.white)
                          : const Text("Guardar"),
                    ),
                    const SizedBox(height: 20),
                    ElevatedButton(
                      onPressed: () => Navigator.pop(context),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.grey,
                      ),
                      child: const Text("Ir al Inicio"),
                    ),
                  ],
                ),
              ),
            ),

            // ----------------- REPORTE -----------------
            Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                children: [
                  ElevatedButton.icon(
                    onPressed: clientes.isEmpty ? null : generarPdf,
                    icon: const Icon(Icons.picture_as_pdf),
                    label: const Text("Descargar PDF"),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.red,
                      foregroundColor: Colors.white,
                    ),
                  ),
                  const SizedBox(height: 20),
                  Expanded(
                    child: clientes.isEmpty
                        ? const Center(
                            child: Text("No hay clientes registrados"),
                          )
                        : ListView.builder(
                            itemCount: clientes.length,
                            itemBuilder: (_, index) {
                              final c = clientes[index];
                              return Card(
                                child: ListTile(
                                  leading: CircleAvatar(
                                    child: Text(c['cli_nom'][0].toUpperCase()),
                                  ),
                                  title: Text(
                                    "${c['cli_nom']} ${c['cli_ape']}",
                                  ),
                                  subtitle: Text(
                                    "Tel: ${c['cli_tel']} | Email: ${c['cli_email']}",
                                  ),
                                ),
                              );
                            },
                          ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
