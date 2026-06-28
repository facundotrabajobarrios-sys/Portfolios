import 'dart:io';
import 'package:flutter/material.dart';
import 'package:window_size/window_size.dart' as window_size;
import 'package:supabase_flutter/supabase_flutter.dart';
import 'screens/login_screen.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Inicializa Supabase (reemplaza con tu URL y ANON KEY)
  await Supabase.initialize(
    url: 'https://puohbrtgkwuxivodrgdm.supabase.co', // tu URL
    anonKey:
        'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InB1b2hicnRna3d1eGl2b2RyZ2RtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzc5NTUyNTIsImV4cCI6MjA5MzUzMTI1Mn0.4HsH5YkwKEIS7Fv0YPurcjPFvPtYknWidZkc0yURJUQ', // tu ANON KEY
  );

  if (Platform.isWindows || Platform.isLinux || Platform.isMacOS) {
    window_size.setWindowTitle('Login Corporativo + Dashboard');
    window_size.setWindowMinSize(const Size(360, 640));
    window_size.setWindowMaxSize(const Size(360, 640));
    window_size.setWindowFrame(const Rect.fromLTWH(100, 100, 360, 640));
  }

  runApp(const CorporateApp());
}

class CorporateApp extends StatelessWidget {
  const CorporateApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'App Corporativa',
      theme: ThemeData(
        useMaterial3: true,
        brightness: Brightness.light,
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF1565C0)),
      ),
      darkTheme: ThemeData(
        useMaterial3: true,
        brightness: Brightness.dark,
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF1565C0),
          brightness: Brightness.dark,
        ),
      ),
      themeMode: ThemeMode.system,
      home: const LoginScreen(),
    );
  }
}
