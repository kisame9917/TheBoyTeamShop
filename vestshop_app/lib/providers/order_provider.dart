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

  Future<void> Function(OrderModel order)? onShowQr;
  void Function(int orderId, String message)? onQrPaid;

  void startRealtimeOnly() {
    orders = [];
    isLoading = false;
    _safeNotify();
    connectRealtime();
  }

  Future<void> loadOrders() async {
    isLoading = false;
    _safeNotify();
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
              final rawId = json['hoaDonId'];
              final int? hoaDonId = rawId is int ? rawId : int.tryParse('$rawId');

              if (type == 'UPSERT' && json['data'] != null) {
                final incoming = OrderModel.fromJson(
                  Map<String, dynamic>.from(json['data'] as Map),
                );

                if (incoming.trangThaiDon != 0) {
                  orders.removeWhere((e) => e.id == incoming.id);
                  _safeNotify();
                  return;
                }

                _upsertOrder(incoming);
                return;
              }

              if (type == 'SHOW_QR') {
                OrderModel? order;

                if (json['data'] != null) {
                  order = OrderModel.fromJson(
                    Map<String, dynamic>.from(json['data'] as Map),
                  );
                  _upsertOrder(order, notify: false);
                } else if (hoaDonId != null) {
                  final idx = orders.indexWhere((e) => e.id == hoaDonId);
                  if (idx >= 0) order = orders[idx];
                }

                if (order != null) {
                  _safeNotify();
                  onShowQr?.call(order);
                }
                return;
              }

              if (type == 'QR_PAID') {
                if (hoaDonId == null) return;

                orders.removeWhere((e) => e.id == hoaDonId);
                _safeNotify();

                final message = json['message']?.toString().trim();
                onQrPaid?.call(
                  hoaDonId,
                  message == null || message.isEmpty
                      ? 'Thanh toán QR thành công'
                      : message,
                );
                return;
              }

              if (type == 'REMOVE') {
                if (hoaDonId == null) return;
                orders.removeWhere((e) => e.id == hoaDonId);
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

  void _upsertOrder(OrderModel incoming, {bool notify = true}) {
    final idx = orders.indexWhere((e) => e.id == incoming.id);
    if (idx >= 0) {
      orders[idx] = incoming;
    } else {
      orders.insert(0, incoming);
    }

    if (notify) _safeNotify();
  }

  void disconnectRealtime() {
    _wsClient.disconnect();
  }

  void _safeNotify() {
    if (!_disposed) notifyListeners();
  }

  @override
  void dispose() {
    _disposed = true;
    _wsClient.disconnect();
    super.dispose();
  }
}
