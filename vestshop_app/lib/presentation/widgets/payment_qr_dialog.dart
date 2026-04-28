import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

import '../../core/constants/api_constants.dart';
import '../../core/network/ws_client.dart';
import '../../data/models/order_model.dart';

class PaymentQrDialog extends StatefulWidget {
  final OrderModel order;

  const PaymentQrDialog({
    super.key,
    required this.order,
  });

  @override
  State<PaymentQrDialog> createState() => _PaymentQrDialogState();
}

class _PaymentQrDialogState extends State<PaymentQrDialog> {
  late final WsClient _wsClient;

  @override
  void initState() {
    super.initState();
    _wsClient = WsClient();
    _connectRealtime();
  }

  @override
  void dispose() {
    _wsClient.disconnect();
    super.dispose();
  }

  void _connectRealtime() {
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
              final int? hoaDonId =
                  rawId is int ? rawId : int.tryParse('$rawId');

              if (!mounted || hoaDonId == null) return;

              if (type == 'REMOVE' && hoaDonId == widget.order.id) {
                Navigator.of(context).pop(true);
              }
            } catch (e) {
              debugPrint('QR realtime parse error: $e');
            }
          },
        );
      },
    );
  }

  String? get qrData {
    if (widget.order.qrCode != null && widget.order.qrCode!.trim().isNotEmpty) {
      return widget.order.qrCode;
    }

    for (final tx in widget.order.giaoDichThanhToan) {
      final value = tx.duLieuQr;
      if (value != null && value.trim().isNotEmpty) {
        return value;
      }
    }

    return null;
  }

  bool _isImageUrl(String value) {
    return value.startsWith('http://') ||
        value.startsWith('https://') ||
        value.startsWith('/');
  }

  String _resolveImageUrl(String value) {
    if (value.startsWith('http://') || value.startsWith('https://')) {
      return value;
    }
    return '${ApiConstants.serverUrl}$value';
  }

  Widget _buildQrImage(String? qr) {
    final value = qr?.trim();

    if (value != null && value.isNotEmpty && _isImageUrl(value)) {
      final imageUrl = _resolveImageUrl(value);
      return Image.network(
        imageUrl,
        width: 260,
        height: 260,
        fit: BoxFit.contain,
        loadingBuilder: (context, child, progress) {
          if (progress == null) return child;
          return const SizedBox(
            width: 260,
            height: 260,
            child: Center(child: CircularProgressIndicator()),
          );
        },
        errorBuilder: (_, error, __) {
          debugPrint('QR load error = $error');
          return Image.asset(
            'assets/images/techcombank-qr.png',
            width: 260,
            height: 260,
            fit: BoxFit.contain,
          );
        },
      );
    }

    return Image.asset(
      'assets/images/techcombank-qr.png',
      width: 260,
      height: 260,
      fit: BoxFit.contain,
    );
  }

  @override
  Widget build(BuildContext context) {
    final qr = qrData?.trim();

    return Dialog(
      insetPadding: const EdgeInsets.all(16),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: SizedBox(
        width: 500,
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    const Expanded(
                      child: Text(
                        'Thanh toán bằng QR',
                        style: TextStyle(
                          fontSize: 24,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                    IconButton(
                      onPressed: () => Navigator.pop(context, false),
                      icon: const Icon(Icons.close),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                _infoRow(
                  'Loại đơn',
                  widget.order.loaiDon ? 'Giao hàng' : 'Tại quầy',
                ),
                _infoRow('Mã HD', widget.order.maHoaDon),
                _infoRow(
                  'Số tiền',
                  '${widget.order.tongTienSauGiam.toStringAsFixed(0)} đ',
                  valueColor: Colors.red,
                ),
                const SizedBox(height: 14),
                Center(child: _buildQrImage(qr)),
                const SizedBox(height: 12),
                Center(
                  child: Text(
                    'Nội dung chuyển khoản: ${widget.order.maHoaDon}',
                    textAlign: TextAlign.center,
                    style: const TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
                const SizedBox(height: 16),
                const Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    SizedBox(
                      width: 18,
                      height: 18,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    ),
                    SizedBox(width: 10),
                    Text('Đang chờ thanh toán realtime...'),
                  ],
                ),
                const SizedBox(height: 16),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: () => Navigator.pop(context, false),
                    child: const Text('Đóng'),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _infoRow(String label, String value, {Color? valueColor}) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 6),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 86,
            child: Text(
              label,
              style: const TextStyle(color: Colors.grey),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: TextStyle(
                fontWeight: FontWeight.w700,
                color: valueColor,
              ),
            ),
          ),
        ],
      ),
    );
  }
}