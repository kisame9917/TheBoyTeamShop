import 'package:flutter/material.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('VestShop'),
      ),
      body: const Center(
        child: Text(
          'Trang chủ VestShop',
          style: TextStyle(fontSize: 20),
        ),
      ),
    );
  }
}