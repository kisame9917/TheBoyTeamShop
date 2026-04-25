import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:qr_flutter/qr_flutter.dart';

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
    final qrCode = widget.order.qrCode?.trim();
    if (qrCode != null && qrCode.isNotEmpty) {
      return qrCode;
    }

    for (final tx in widget.order.giaoDichThanhToan) {
      final value = tx.duLieuQr?.trim();
      if (value != null && value.isNotEmpty) {
        return value;
      }
    }

    return null;
  }

  String _resolveQrPayload(String value) {
    final trimmed = value.trim();

    if (trimmed.startsWith('http://') || trimmed.startsWith('https://')) {
      return trimmed;
    }

    if (trimmed.startsWith('/')) {
      return '${ApiConstants.serverUrl}$trimmed';
    }

    return trimmed;
  }

  String _formatMoney(double value) {
    final number = value.round().toString();
    final buffer = StringBuffer();
    int count = 0;

    for (int i = number.length - 1; i >= 0; i--) {
      buffer.write(number[i]);
      count++;

      if (count % 3 == 0 && i != 0) {
        buffer.write('.');
      }
    }

    return '${buffer.toString().split('').reversed.join()} đ';
  }

  @override
  Widget build(BuildContext context) {
    final rawQr = qrData;
    final qrPayload = rawQr == null ? null : _resolveQrPayload(rawQr);

    debugPrint('QR raw = $rawQr');
    debugPrint('QR payload = $qrPayload');

    if (qrPayload == null || qrPayload.isEmpty) {
      return Dialog(
        insetPadding: const EdgeInsets.all(16),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(Icons.qr_code_2, size: 56, color: Colors.grey),
              const SizedBox(height: 12),
              const Text(
                'Không có dữ liệu QR',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 8),
              const Text(
                'Hãy bấm Thanh toán QR bên màn bán hàng tại quầy trước.',
                textAlign: TextAlign.center,
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
      );
    }

    return Dialog(
      insetPadding: const EdgeInsets.all(16),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: SizedBox(
        width: 500,
        child: Padding(
          padding: const EdgeInsets.all(18),
          child: Column(
            mainAxisSize: MainAxisSize.min,
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

              Text(
                'Mã HD: ${widget.order.maHoaDon}',
                style: const TextStyle(fontSize: 16),
              ),

              const SizedBox(height: 8),

              Text(
                'Số tiền: ${_formatMoney(widget.order.tongTienSauGiam)}',
                style: const TextStyle(
                  fontSize: 18,
                  color: Colors.red,
                  fontWeight: FontWeight.w700,
                ),
              ),

              const SizedBox(height: 16),

              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(18),
                decoration: BoxDecoration(
                  color: Colors.white,
                  border: Border.all(color: Colors.black12),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Column(
                  children: [
                    QrImageView(
                      data: qrPayload,
                      version: QrVersions.auto,
                      size: 260,
                      backgroundColor: Colors.white,
                      gapless: true,
                    ),
                    const SizedBox(height: 12),
                    const Text(
                      'Quét mã để thanh toán',
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                    const SizedBox(height: 8),
                    SelectableText(
                      qrPayload,
                      textAlign: TextAlign.center,
                      style: const TextStyle(fontSize: 12),
                    ),
                  ],
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
    );
  }
}