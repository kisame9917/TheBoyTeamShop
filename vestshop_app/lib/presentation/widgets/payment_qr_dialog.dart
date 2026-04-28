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
                        'Đơn hàng tại quầy',
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
                _infoRow('Mã HD', order.maHoaDon),
                _infoRow(
                  'Số tiền',
                  '${order.tongTienSauGiam.toStringAsFixed(0)} đ',
                  valueColor: Colors.red,
                ),
                const SizedBox(height: 14),
                Center(child: _buildQrImage(qr)),
                const SizedBox(height: 12),
                Center(
                  child: Text(
                    'Nội dung chuyển khoản: ${order.maHoaDon}',
                    textAlign: TextAlign.center,
                    style: const TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
                const SizedBox(height: 14),
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: Colors.orange.shade50,
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: Colors.orange.shade100),
                  ),
                  child: const Text(
                    'Đang chờ admin xác nhận thanh toán QR...',
                    textAlign: TextAlign.center,
                    style: TextStyle(fontWeight: FontWeight.w700),
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
