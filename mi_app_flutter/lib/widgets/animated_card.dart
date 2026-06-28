import 'package:flutter/material.dart';

class AnimatedCard extends StatelessWidget {
  final IconData icon;
  final String title;
  final Color color;
  final AnimationController controller;
  final double delay; // delay para controlar la animación secuencial
  final VoidCallback? onTap; // 👈 callback para manejar taps

  const AnimatedCard({
    required this.icon,
    required this.title,
    required this.color,
    required this.controller,
    this.delay = 0,
    this.onTap,
    super.key,
  });

  @override
  Widget build(BuildContext context) {
    return FadeTransition(
      // FadeTransition para animar la opacidad
      opacity: Tween<double>(begin: 0, end: 1).animate(
        CurvedAnimation(parent: controller, curve: Interval(delay, 1.0)),
      ),
      child: ScaleTransition(
        scale: Tween<double>(begin: 0.8, end: 1.0).animate(
          CurvedAnimation(
            parent: controller,
            curve: Interval(delay, 1.0, curve: Curves.elasticOut),
          ),
        ),
        child: Card(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
          ),
          color: color,
          child: InkWell(
            onTap: onTap, // 👈 usamos el callback
            borderRadius: BorderRadius.circular(16),
            child: Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(icon, size: 50, color: Colors.white),
                  const SizedBox(height: 10),
                  Text(
                    title,
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
