import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/order_provider.dart';
import 'presentation/screens/order/order_history_screen.dart';

void main() {
  runApp(const VestShopApp());
}

class VestShopApp extends StatelessWidget {
  const VestShopApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => OrderProvider()),
      ],
      child: MaterialApp(
        debugShowCheckedModeBanner: false,
        title: 'VestShop App',
        theme: ThemeData(
          colorSchemeSeed: Colors.green,
          useMaterial3: true,
        ),
        home: const OrderHistoryScreen(),
      ),
    );
  }
}