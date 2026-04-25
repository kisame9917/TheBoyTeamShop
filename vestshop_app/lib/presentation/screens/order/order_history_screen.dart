import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../core/utils/format_utils.dart';
import '../../../data/models/order_model.dart';
import '../../../providers/order_provider.dart';
import '../../widgets/payment_qr_dialog.dart';
import 'order_detail_screen.dart';

class OrderHistoryScreen extends StatefulWidget {
  const OrderHistoryScreen({super.key});

  @override
  State<OrderHistoryScreen> createState() => _OrderHistoryScreenState();
}

class _OrderHistoryScreenState extends State<OrderHistoryScreen> {
  @override
  void initState() {
    super.initState();

    Future.microtask(() {
      final p = context.read<OrderProvider>();
      p.startRealtimeOnly();
    });
  }

  @override
  void dispose() {
    context.read<OrderProvider>().disconnectRealtime();
    super.dispose();
  }

  bool _hasQr(OrderModel order) {
    final qrCode = order.qrCode?.trim();

    if (qrCode != null && qrCode.isNotEmpty) {
      return true;
    }

    for (final tx in order.giaoDichThanhToan) {
      final value = tx.duLieuQr?.trim();

      if (value != null && value.isNotEmpty) {
        return true;
      }
    }

    return false;
  }

  Future<void> _openDetail(OrderModel order) async {
    final removed = await Navigator.push<bool>(
      context,
      MaterialPageRoute(
        builder: (_) => OrderDetailScreen(order: order),
      ),
    );

    if (removed == true && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Đã ghi nhận thanh toán realtime'),
        ),
      );
    }
  }

  Future<void> _showQr(OrderModel order) async {
    final removed = await showDialog<bool>(
      context: context,
      barrierDismissible: false,
      builder: (_) => PaymentQrDialog(order: order),
    );

    if (removed == true && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Đã ghi nhận thanh toán realtime'),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<OrderProvider>();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Đơn hàng tại quầy'),
      ),
      body: provider.isLoading
          ? const Center(
              child: CircularProgressIndicator(),
            )
          : provider.orders.isEmpty
              ? const Center(
                  child: Text('Chưa có đơn hàng đang mở'),
                )
              : ListView.builder(
                  padding: const EdgeInsets.all(12),
                  itemCount: provider.orders.length,
                  itemBuilder: (context, index) {
                    final o = provider.orders[index];

                    return Card(
                      margin: const EdgeInsets.only(bottom: 12),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(16),
                      ),
                      child: InkWell(
                        borderRadius: BorderRadius.circular(16),
                        onTap: () => _openDetail(o),
                        child: Padding(
                          padding: const EdgeInsets.all(12),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Expanded(
                                    child: Text(
                                      o.maHoaDon,
                                      style: const TextStyle(
                                        fontWeight: FontWeight.bold,
                                        fontSize: 16,
                                      ),
                                    ),
                                  ),
                                  if (_hasQr(o))
                                    Container(
                                      padding: const EdgeInsets.symmetric(
                                        horizontal: 10,
                                        vertical: 5,
                                      ),
                                      decoration: BoxDecoration(
                                        color: Colors.green.shade50,
                                        borderRadius: BorderRadius.circular(999),
                                        border: Border.all(
                                          color: Colors.green.shade200,
                                        ),
                                      ),
                                      child: const Row(
                                        mainAxisSize: MainAxisSize.min,
                                        children: [
                                          Icon(
                                            Icons.qr_code,
                                            size: 16,
                                            color: Colors.green,
                                          ),
                                          SizedBox(width: 4),
                                          Text(
                                            'Có QR',
                                            style: TextStyle(
                                              color: Colors.green,
                                              fontSize: 12,
                                              fontWeight: FontWeight.w700,
                                            ),
                                          ),
                                        ],
                                      ),
                                    ),
                                ],
                              ),

                              const SizedBox(height: 6),

                              Text('Khách: ${o.tenKhachHang ?? "Khách lẻ"}'),
                              Text('SĐT: ${o.soDienThoai ?? "-"}'),

                              const SizedBox(height: 12),

                              ...o.items.map((it) {
                                final imageUrl = it.anhDaiDien?.trim();

                                return Container(
                                  margin: const EdgeInsets.only(bottom: 8),
                                  padding: const EdgeInsets.all(10),
                                  decoration: BoxDecoration(
                                    color: Colors.grey.shade100,
                                    borderRadius: BorderRadius.circular(12),
                                  ),
                                  child: Row(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Container(
                                        width: 52,
                                        height: 52,
                                        decoration: BoxDecoration(
                                          color: Colors.white,
                                          borderRadius: BorderRadius.circular(8),
                                        ),
                                        clipBehavior: Clip.antiAlias,
                                        child: imageUrl != null &&
                                                imageUrl.isNotEmpty
                                            ? Image.network(
                                                imageUrl,
                                                width: 52,
                                                height: 52,
                                                fit: BoxFit.cover,
                                                errorBuilder: (_, error, __) {
                                                  debugPrint(
                                                    'IMAGE ERROR: $error',
                                                  );
                                                  debugPrint(
                                                    'IMAGE URL FAILED: $imageUrl',
                                                  );
                                                  return const Icon(
                                                    Icons.broken_image,
                                                  );
                                                },
                                                loadingBuilder: (
                                                  context,
                                                  child,
                                                  loadingProgress,
                                                ) {
                                                  if (loadingProgress == null) {
                                                    return child;
                                                  }

                                                  return const Center(
                                                    child: SizedBox(
                                                      width: 16,
                                                      height: 16,
                                                      child:
                                                          CircularProgressIndicator(
                                                        strokeWidth: 2,
                                                      ),
                                                    ),
                                                  );
                                                },
                                              )
                                            : const Icon(Icons.image),
                                      ),

                                      const SizedBox(width: 10),

                                      Expanded(
                                        child: Column(
                                          crossAxisAlignment:
                                              CrossAxisAlignment.start,
                                          children: [
                                            Text(
                                              it.tenSanPham ?? '',
                                              style: const TextStyle(
                                                fontWeight: FontWeight.w600,
                                              ),
                                            ),
                                            const SizedBox(height: 4),
                                            Text(
                                              '${it.mauSac ?? "-"} - ${it.kichCo ?? "-"}',
                                            ),
                                            Text('SL: ${it.soLuong}'),
                                          ],
                                        ),
                                      ),

                                      Text(
                                        FormatUtils.money(it.thanhTien),
                                      ),
                                    ],
                                  ),
                                );
                              }),

                              const Divider(),

                              _row(
                                'Tổng tiền hàng',
                                FormatUtils.money(o.tongTien),
                              ),
                              _row(
                                'Giảm giá voucher',
                                '- ${FormatUtils.money(o.tongTienGiam)}',
                              ),
                              _row(
                                'Phí ship',
                                FormatUtils.money(o.phiVanChuyen),
                              ),

                              const SizedBox(height: 6),

                              _row(
                                'Thành tiền',
                                FormatUtils.money(o.tongTienSauGiam),
                                isBold: true,
                              ),

                              const SizedBox(height: 12),

                              if (_hasQr(o))
                                SizedBox(
                                  width: double.infinity,
                                  child: ElevatedButton.icon(
                                    onPressed: () => _showQr(o),
                                    icon: const Icon(Icons.qr_code),
                                    label: const Text('Xem QR thanh toán'),
                                  ),
                                )
                              else
                                Container(
                                  width: double.infinity,
                                  padding: const EdgeInsets.all(10),
                                  decoration: BoxDecoration(
                                    color: Colors.orange.shade50,
                                    borderRadius: BorderRadius.circular(10),
                                    border: Border.all(
                                      color: Colors.orange.shade100,
                                    ),
                                  ),
                                  child: const Row(
                                    crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                    children: [
                                      Icon(
                                        Icons.info_outline,
                                        size: 18,
                                        color: Colors.orange,
                                      ),
                                      SizedBox(width: 8),
                                      Expanded(
                                        child: Text(
                                          'Chưa có QR. Hãy bấm Thanh toán QR bên màn bán hàng tại quầy.',
                                          style: TextStyle(
                                            color: Colors.orange,
                                          ),
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                            ],
                          ),
                        ),
                      ),
                    );
                  },
                ),
    );
  }

  Widget _row(String left, String right, {bool isBold = false}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 3),
      child: Row(
        children: [
          Expanded(
            child: Text(
              left,
              style: TextStyle(
                fontWeight: isBold ? FontWeight.bold : FontWeight.normal,
              ),
            ),
          ),
          Text(
            right,
            style: TextStyle(
              fontWeight: isBold ? FontWeight.bold : FontWeight.normal,
              color: isBold ? Colors.green : null,
            ),
          ),
        ],
      ),
    );
  }
}