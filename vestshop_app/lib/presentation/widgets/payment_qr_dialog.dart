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

              // Khi đơn tại quầy bị remove realtime sau thanh toán xong
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

  @override
  Widget build(BuildContext context) {
    final qr = qrData?.trim();

    debugPrint('QR raw = $qr');

    if (qr == null || qr.isEmpty) {
      return const Dialog(
        child: Padding(
          padding: EdgeInsets.all(16),
          child: Text('Không có dữ liệu QR'),
        ),
      );
    }

    final canShowImage = _isImageUrl(qr);
    final imageUrl = canShowImage ? _resolveImageUrl(qr) : null;

    debugPrint('QR imageUrl = $imageUrl');

    return Dialog(
      insetPadding: const EdgeInsets.all(16),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: SizedBox(
        width: 500,
        child: Padding(
          padding: const EdgeInsets.all(16),
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
                        fontSize: 28,
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
                'Số tiền: ${widget.order.tongTienSauGiam.toStringAsFixed(0)} đ',
                style: const TextStyle(
                  fontSize: 18,
                  color: Colors.red,
                  fontWeight: FontWeight.w700,
                ),
              ),
              const SizedBox(height: 8),
              const Text(
                'Sẽ tự đóng khi thanh toán được ghi nhận realtime',
                style: TextStyle(
                  fontSize: 14,
                  color: Colors.orange,
                  fontWeight: FontWeight.w600,
                ),
              ),
              const SizedBox(height: 16),
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  border: Border.all(color: Colors.black12),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: canShowImage
                    ? Column(
                        children: [
                          Image.network(
                            imageUrl!,
                            width: 240,
                            height: 240,
                            fit: BoxFit.contain,
                            loadingBuilder: (context, child, progress) {
                              if (progress == null) return child;
                              return const Padding(
                                padding: EdgeInsets.all(24),
                                child: CircularProgressIndicator(),
                              );
                            },
                            errorBuilder: (_, error, __) {
                              debugPrint('QR load error = $error');
                              return Column(
                                children: [
                                  const Text('Không tải được ảnh QR'),
                                  const SizedBox(height: 8),
                                  SelectableText(imageUrl),
                                ],
                              );
                            },
                          ),
                        ],
                      )
                    : Column(
                        children: [
                          const Text('Dữ liệu QR hiện không phải link ảnh'),
                          const SizedBox(height: 8),
                          SelectableText(qr),
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