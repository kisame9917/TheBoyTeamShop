import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

import '../../core/constants/api_constants.dart';
import '../../data/models/order_model.dart';

class PaymentQrDialog extends StatelessWidget {
  final OrderModel order;

  const PaymentQrDialog({
    super.key,
    required this.order,
  });

  String? get qrData {
    if (order.qrCode != null && order.qrCode!.trim().isNotEmpty) {
      return order.qrCode;
    }

    for (final tx in order.giaoDichThanhToan) {
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
                    onPressed: () => Navigator.pop(context),
                    icon: const Icon(Icons.close),
                  )
                ],
              ),
              const SizedBox(height: 8),
              Text(
                'Mã HD: ${order.maHoaDon}',
                style: const TextStyle(fontSize: 16),
              ),
              const SizedBox(height: 8),
              Text(
                'Số tiền: ${order.tongTienSauGiam.toStringAsFixed(0)} đ',
                style: const TextStyle(
                  fontSize: 18,
                  color: Colors.red,
                  fontWeight: FontWeight.w700,
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
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: () => Navigator.pop(context),
                  child: const Text('Đóng'),
                ),
              )
            ],
          ),
        ),
      ),
    );
  }
}