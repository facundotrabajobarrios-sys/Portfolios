import 'package:flutter/material.dart';
import '../widgets/animated_card.dart';
import 'clientes_screen.dart';
import 'sucursal_screen.dart';
import 'reportes_screen.dart';
import 'login_screen.dart';
import 'configuración_screen.dart';

class DashboardScreen extends StatefulWidget {
  final String usuario;
  final int usuId; // Nuevo: pasamos el ID del usuario logueado
  const DashboardScreen({
    required this.usuario,
    required this.usuId,
    super.key,
  });

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen>
    with TickerProviderStateMixin {
  late AnimationController _cardsController;
  bool isHoveringLogout = false;

  @override
  void initState() {
    super.initState();
    _cardsController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 800),
    );
    _cardsController.forward();
  }

  @override
  void dispose() {
    _cardsController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Bienvenido, ${widget.usuario}'),
        backgroundColor: const Color(0xFF0D47A1),
        actions: [
          CircleAvatar(
            backgroundColor: Colors.white,
            child: Text(
              widget.usuario[0].toUpperCase(),
              style: const TextStyle(color: Color(0xFF0D47A1)),
            ),
          ),
          const SizedBox(width: 10),
          MouseRegion(
            onEnter: (_) => setState(() => isHoveringLogout = true),
            onExit: (_) => setState(() => isHoveringLogout = false),
            child: IconButton(
              icon: Icon(
                Icons.logout,
                color: isHoveringLogout ? Colors.yellow : Colors.white,
              ),
              tooltip: 'Cerrar sesión',
              onPressed: () {
                Navigator.pushAndRemoveUntil(
                  context,
                  PageRouteBuilder(
                    pageBuilder: (_, __, ___) => const LoginScreen(),
                    transitionsBuilder: (_, anim, __, child) =>
                        FadeTransition(opacity: anim, child: child),
                    transitionDuration: const Duration(milliseconds: 500),
                  ),
                  (route) => false,
                );
              },
            ),
          ),
          const SizedBox(width: 10),
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(20),
        child: GridView.count(
          crossAxisCount: 2,
          crossAxisSpacing: 20,
          mainAxisSpacing: 20,
          children: [
            AnimatedCard(
              icon: Icons.people,
              title: 'Clientes',
              color: Colors.blue,
              controller: _cardsController,
              delay: 0,
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    //MaterialPageRoute para animación de transición
                    builder: (_) => ClientesScreen(usuario: widget.usuario),
                  ),
                );
              },
            ),
            AnimatedCard(
              icon: Icons.location_city,
              title: 'Sucursales',
              color: Colors.orange,
              controller: _cardsController,
              delay: 0.2,
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (_) => SucursalScreen(usuario: widget.usuario),
                  ),
                );
              },
            ),
            AnimatedCard(
              icon: Icons.insert_chart,
              title: 'Reportes',
              color: Colors.green,
              controller: _cardsController,
              delay: 0.4,
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (_) => const ReportesScreen()),
                );
              },
            ),
            AnimatedCard(
              icon: Icons.settings,
              title: 'Configuración',
              color: Colors.purple,
              controller: _cardsController,
              delay: 0.6,
              onTap: () {
                // Pasamos usuario y usuId al ConfiguracionScreen
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (_) => ConfiguracionScreen(
                      usuario: widget.usuario,
                      usuId: widget.usuId,
                    ),
                  ),
                );
              },
            ),
          ],
        ),
      ),
    );
  }
}
