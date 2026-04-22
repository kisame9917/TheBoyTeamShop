import 'dart:convert';
import 'package:flutter/material.dart';
import '../core/constants/api_constants.dart';
import '../core/network/api_client.dart';
import '../core/network/ws_client.dart';
import '../data/models/order_model.dart';
import '../data/services/order_service.dart';

class OrderProvider extends ChangeNotifier {
  final ApiClient _apiClient = ApiClient(baseUrl: ApiConstants.baseUrl);
  late final OrderService _orderService = OrderService(_apiClient);
  final WsClient _wsClient = WsClient();

  List<OrderModel> orders = [];
  bool isLoading = false;
  bool _disposed = false;

Future<void> loadOrders() async {
  isLoading = true;
  _safeNotify();

  try {
    orders = await _orderService.getActivePosDrafts();
    debugPrint('LOAD ORDERS SUCCESS: ${orders.length}');
  } catch (e) {
    debugPrint('loadOrders error: $e');
  } finally {
    isLoading = false;
    _safeNotify();
  }
}
  void connectRealtime() {
    if (_wsClient.isConnected) return;

    _wsClient.connect(
      url: ApiConstants.wsUrl,
      onConnect: (_) {
        _wsClient.subscribe(
          destination: '/topic/pos-orders',
          callback: (frame) {
            try {
              final body = frame?.body;
              if (body == null || body.isEmpty) return;

              final Map<String, dynamic> json =
                  jsonDecode(body) as Map<String, dynamic>;

              final type = json['type'];

              if (type == 'UPSERT' && json['data'] != null) {
                final incoming = OrderModel.fromJson(
                  json['data'] as Map<String, dynamic>,
                );

                final idx = orders.indexWhere((e) => e.id == incoming.id);
                if (idx >= 0) {
                  orders[idx] = incoming;
                } else {
                  orders.insert(0, incoming);
                }
                _safeNotify();
              }

              if (type == 'REMOVE') {
                final dynamic rawId = json['hoaDonId'];
                final int? id = rawId is int ? rawId : int.tryParse('$rawId');
                if (id == null) return;

                orders.removeWhere((e) => e.id == id);
                _safeNotify();
              }
            } catch (e) {
              debugPrint('WS parse error: $e');
            }
          },
        );
      },
    );
  }

  void disconnectRealtime() {
    _wsClient.disconnect();
  }

  void _safeNotify() {
    if (!_disposed) {
      notifyListeners();
    }
  }

  @override
  void dispose() {
    _disposed = true;
    _wsClient.disconnect();
    super.dispose();
  }
  void startRealtimeOnly() {
  orders = [];
  isLoading = false;
  _safeNotify();
  connectRealtime();
}
}