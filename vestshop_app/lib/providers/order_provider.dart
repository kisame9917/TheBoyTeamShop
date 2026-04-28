import 'dart:convert';

import 'package:flutter/material.dart';

import '../core/constants/api_constants.dart';
import '../core/network/ws_client.dart';
import '../data/models/order_model.dart';

class OrderProvider extends ChangeNotifier {
  final WsClient _wsClient = WsClient();

  List<OrderModel> orders = [];
  bool isLoading = false;
  bool _disposed = false;
  OrderModel? qrOrderToShow;
  int qrSignal = 0;

  void startRealtimeOnly() {
    orders = [];
    isLoading = false;
    _safeNotify();
    connectRealtime();
  }

  Future<void> loadOrders() async {
    startRealtimeOnly();
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

              final type = json['type']?.toString();

              if ((type == 'UPSERT' || type == 'SHOW_QR') &&
                  json['data'] != null) {
                var incoming = OrderModel.fromJson(
                  Map<String, dynamic>.from(json['data'] as Map),
                );

                final qrCode = json['qrCode']?.toString().trim();
                if (type == 'SHOW_QR' && qrCode != null && qrCode.isNotEmpty) {
                  incoming = incoming.copyWith(qrCode: qrCode);
                }

                if (incoming.trangThaiDon != 0) {
                  orders.removeWhere((e) => e.id == incoming.id);
                  _safeNotify();
                  return;
                }

                final idx = orders.indexWhere((e) => e.id == incoming.id);
                if (idx >= 0) {
                  orders[idx] = incoming;
                } else {
                  orders.insert(0, incoming);
                }

                if (type == 'SHOW_QR') {
                  qrOrderToShow = incoming;
                  qrSignal++;
                }

                _safeNotify();
                return;
              }

              if (type == 'REMOVE') {
                final dynamic rawId = json['hoaDonId'];
                final int? id = rawId is int ? rawId : int.tryParse('$rawId');
                if (id == null) return;

                orders.removeWhere((e) => e.id == id);
                if (qrOrderToShow?.id == id) {
                  qrOrderToShow = null;
                }
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

  void markQrHandled(int signal) {
    if (qrSignal == signal) {
      qrOrderToShow = null;
    }
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
}
