import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../providers/order_provider.dart';
import '../../../core/utils/format_utils.dart';
import '../../../data/models/order_model.dart';
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
      context.read<OrderProvider>().startRealtimeOnly();
    });
  }

  @override
  void dispose() {
    context.read<OrderProvider>().disconnectRealtime();
    super.dispose();
  }

  Future<void> _openDetail(BuildContext context, OrderModel order) async {
    final removed = await Navigator.push<bool>(
      context,
      MaterialPageRoute(
        builder: (_) => OrderDetailScreen(order: order),
      ),
    );

    if (!context.mounted) return;

    if (removed == true) {
      context.read<OrderProvider>().loadOrders();
    }
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<OrderProvider>();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Đơn POS đang mở'),
        actions: [
          IconButton(
            onPressed: provider.isLoading
                ? null
                : () => context.read<OrderProvider>().loadOrders(),
            icon: const Icon(Icons.refresh),
          ),
        ],
      ),
      body: provider.isLoading
          ? const Center(child: CircularProgressIndicator())
          : provider.orders.isEmpty
              ? const Center(child: Text('Chưa có đơn POS đang mở'))
              : RefreshIndicator(
                  onRefresh: () => context.read<OrderProvider>().loadOrders(),
                  child: ListView.builder(
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
                          onTap: () => _openDetail(context, o),
                          child: Padding(
                            padding: const EdgeInsets.all(12),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
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
                                    Container(
                                      padding: const EdgeInsets.symmetric(
                                        horizontal: 10,
                                        vertical: 4,
                                      ),
                                      decoration: BoxDecoration(
                                        color: o.loaiDon
                                            ? Colors.orange.shade50
                                            : Colors.green.shade50,
                                        borderRadius: BorderRadius.circular(99),
                                      ),
                                      child: Text(
                                        o.loaiDon ? 'Giao hàng' : 'Tại quầy',
                                        style: TextStyle(
                                          color: o.loaiDon
                                              ? Colors.orange.shade800
                                              : Colors.green.shade800,
                                          fontWeight: FontWeight.w700,
                                          fontSize: 12,
                                        ),
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
                                      crossAxisAlignment:
                                          CrossAxisAlignment.start,
                                      children: [
                                        Container(
                                          width: 52,
                                          height: 52,
                                          decoration: BoxDecoration(
                                            color: Colors.white,
                                            borderRadius:
                                                BorderRadius.circular(8),
                                          ),
                                          clipBehavior: Clip.antiAlias,
                                          child: imageUrl != null &&
                                                  imageUrl.isNotEmpty
                                              ? Image.network(
                                                  imageUrl,
                                                  width: 52,
                                                  height: 52,
                                                  fit: BoxFit.cover,
                                                  errorBuilder: (_, __, ___) {
                                                    return const Icon(
                                                      Icons.broken_image,
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
                                        Text(FormatUtils.money(it.thanhTien)),
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
                                const SizedBox(height: 10),
                                SizedBox(
                                  width: double.infinity,
                                  child: OutlinedButton.icon(
                                    onPressed: () => _openDetail(context, o),
                                    icon: const Icon(Icons.qr_code),
                                    label: const Text('Mở QR thanh toán'),
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ),
                      );
                    },
                  ),
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