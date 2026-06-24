import 'package:flutter/material.dart';
import '../services/supabase_service.dart';
import 'dashboard_screen.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> with TickerProviderStateMixin {
  final TextEditingController usuarioController = TextEditingController();
  final TextEditingController passwordController = TextEditingController();
  String errorMessage = '';
  bool isHovering = false;

  late AnimationController _logoController;
  late AnimationController _fieldsController;
  late AnimationController _buttonController;
  late AnimationController _footerController;

  @override
  void initState() {
    super.initState();
    _logoController = AnimationController(vsync: this, duration: const Duration(milliseconds: 1000));
    _fieldsController = AnimationController(vsync: this, duration: const Duration(milliseconds: 800));
    _buttonController = AnimationController(vsync: this, duration: const Duration(milliseconds: 600));
    _footerController = AnimationController(vsync: this, duration: const Duration(milliseconds: 500));

    _logoController.forward().whenComplete(() {
      _fieldsController.forward().whenComplete(() {
        _buttonController.forward().whenComplete(() {
          _footerController.forward();
        });
      });
    });
  }

  @override
  void dispose() {
    _logoController.dispose();
    _fieldsController.dispose();
    _buttonController.dispose();
    _footerController.dispose();
    usuarioController.dispose();
    passwordController.dispose();
    super.dispose();
  }

  void login() async {
    String correo = usuarioController.text.trim();
    String password = passwordController.text.trim();

    final user = await SupabaseService.login(correo, password);
    if (user != null) {
      setState(() => errorMessage = '');
      Navigator.pushReplacement(
        context,
        PageRouteBuilder(
          pageBuilder: (_, __, ___) => DashboardScreen(
            usuario: user['user_nombre'],   // nombre del usuario
            usuId: user['user_id'],         // ID del usuario
          ),
          transitionsBuilder: (_, anim, __, child) =>
              FadeTransition(opacity: anim, child: child),
          transitionDuration: const Duration(milliseconds: 700),
        ),
      );
    } else {
      setState(() => errorMessage = 'Usuario o contraseña incorrectos');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: TweenAnimationBuilder<double>(
        tween: Tween(begin: 0, end: 1),
        duration: const Duration(seconds: 3),
        builder: (context, value, child) {
          return Container(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: [
                  Color.lerp(const Color(0xFF1565C0), const Color(0xFF42A5F5), value)!,
                  Color.lerp(const Color(0xFF42A5F5), const Color(0xFF64B5F6), value)!
                ],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
            ),
            child: child,
          );
        },
        child: Center(
          child: SingleChildScrollView(
            child: Card(
              elevation: 25,
              shadowColor: Colors.black45,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
              margin: const EdgeInsets.symmetric(horizontal: 30),
              child: Padding(
                padding: const EdgeInsets.all(30),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    FadeTransition(
                      opacity: _logoController,
                      child: ScaleTransition(
                        scale: Tween<double>(begin: 0.5, end: 1).animate(
                          CurvedAnimation(parent: _logoController, curve: Curves.elasticOut),
                        ),
                        child: const CircleAvatar(
                          radius: 55,
                          backgroundColor: Color(0xFF0D47A1),
                          child: Icon(Icons.business_center, size: 55, color: Colors.white),
                        ),
                      ),
                    ),
                    const SizedBox(height: 20),
                    FadeTransition(
                      opacity: _fieldsController,
                      child: Column(
                        children: [
                          TextField(
                            controller: usuarioController,
                            decoration: const InputDecoration(
                              labelText: 'Correo',
                              prefixIcon: Icon(Icons.email),
                            ),
                          ),
                          const SizedBox(height: 20),
                          TextField(
                            controller: passwordController,
                            obscureText: true,
                            decoration: const InputDecoration(
                              labelText: 'Contraseña',
                              prefixIcon: Icon(Icons.lock),
                            ),
                          ),
                          const SizedBox(height: 15),
                          AnimatedSwitcher(
                            duration: const Duration(milliseconds: 500),
                            child: errorMessage.isNotEmpty
                                ? Text(
                                    errorMessage,
                                    key: ValueKey(errorMessage),
                                    style: const TextStyle(color: Colors.red, fontWeight: FontWeight.bold),
                                  )
                                : const SizedBox.shrink(),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 25),
                    FadeTransition(
                      opacity: _buttonController,
                      child: MouseRegion(
                        onEnter: (_) => setState(() => isHovering = true),
                        onExit: (_) => setState(() => isHovering = false),
                        child: AnimatedContainer(
                          duration: const Duration(milliseconds: 200),
                          transform: isHovering ? Matrix4.translationValues(0, -3, 0) : Matrix4.identity(),
                          decoration: BoxDecoration(
                            boxShadow: isHovering
                                ? [BoxShadow(color: Colors.black26, blurRadius: 12, offset: const Offset(0, 6))]
                                : [],
                            borderRadius: BorderRadius.circular(16),
                          ),
                          child: SizedBox(
                            width: double.infinity,
                            child: ElevatedButton(
                              style: ElevatedButton.styleFrom(
                                backgroundColor: const Color(0xFF0D47A1),
                                padding: const EdgeInsets.symmetric(vertical: 18),
                                elevation: 8,
                                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                              ),
                              onPressed: login,
                              child: const Text(
                                'Acceder',
                                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Colors.white),
                              ),
                            ),
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(height: 20),
                    FadeTransition(
                      opacity: _footerController,
                      child: const Text('© 2025 Mi Empresa', style: TextStyle(color: Colors.grey, fontSize: 12)),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
