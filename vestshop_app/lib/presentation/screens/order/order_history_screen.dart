import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../core/constants/api_constants.dart';
import '../../../core/utils/format_utils.dart';
import '../../../data/models/order_model.dart';
import '../../../providers/order_provider.dart';
import '../../widgets/payment_qr_dialog.dart';

class OrderHistoryScreen extends StatefulWidget {
  const OrderHistoryScreen({super.key});

  @override
  State<OrderHistoryScreen> createState() => _OrderHistoryScreenState();
}

class _OrderHistoryScreenState extends State<OrderHistoryScreen> {
  int _lastQrSignal = 0;

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

  void _listenShowQr(OrderProvider provider) {
    final signal = provider.qrSignal;
    final order = provider.qrOrderToShow;

    if (signal == 0 || signal == _lastQrSignal || order == null) return;

    _lastQrSignal = signal;
    provider.markQrHandled(signal);

    WidgetsBinding.instance.addPostFrameCallback((_) async {
      if (!mounted) return;
      await showDialog<bool>(
        context: context,
        barrierDismissible: false,
        builder: (_) => PaymentQrDialog(order: order),
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<OrderProvider>();
    _listenShowQr(provider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Đơn POS realtime'),
        actions: [
          IconButton(
            onPressed: () => context.read<OrderProvider>().startRealtimeOnly(),
            icon: const Icon(Icons.refresh),
          ),
        ],
      ),
      body: provider.isLoading
          ? const Center(child: CircularProgressIndicator())
          : provider.orders.isEmpty
              ? const Center(
                  child: Padding(
                    padding: EdgeInsets.all(24),
                    child: Text(
                      'Đang chờ dữ liệu realtime từ POS...',
                      textAlign: TextAlign.center,
                    ),
                  ),
                )
              : ListView.builder(
                  padding: const EdgeInsets.all(12),
                  itemCount: provider.orders.length,
                  itemBuilder: (context, index) {
                    final o = provider.orders[index];
                    return _OrderCard(order: o);
                  },
                ),
    );
  }
}

class _OrderCard extends StatelessWidget {
  final OrderModel order;

  const _OrderCard({required this.order});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(
                    order.maHoaDon,
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
                    color: order.loaiDon
                        ? Colors.orange.shade50
                        : Colors.green.shade50,
                    borderRadius: BorderRadius.circular(99),
                  ),
                  child: Text(
                    order.loaiDon ? 'Giao hàng' : 'Tại quầy',
                    style: TextStyle(
                      color: order.loaiDon
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
            Text('Khách: ${order.tenKhachHang ?? "Khách lẻ"}'),
            Text('SĐT: ${order.soDienThoai ?? "-"}'),
            const SizedBox(height: 12),
            ...order.items.map(_buildItem),
            const Divider(),
            _row('Tổng tiền hàng', FormatUtils.money(order.tongTien)),
            _row(
              'Giảm giá voucher',
              '- ${FormatUtils.money(order.tongTienGiam)}',
            ),
            _row('Phí ship', FormatUtils.money(order.phiVanChuyen)),
            const SizedBox(height: 6),
            _row(
              'Thành tiền',
              FormatUtils.money(order.tongTienSauGiam),
              isBold: true,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildItem(OrderItemModel item) {
    final imageUrl = _resolveImageUrl(item.anhDaiDien);

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
            child: imageUrl != null
                ? Image.network(
                    imageUrl,
                    width: 52,
                    height: 52,
                    fit: BoxFit.cover,
                    errorBuilder: (_, __, ___) {
                      return const Icon(Icons.broken_image);
                    },
                  )
                : const Icon(Icons.image),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  item.tenSanPham ?? '',
                  style: const TextStyle(fontWeight: FontWeight.w600),
                ),
                const SizedBox(height: 4),
                Text('${item.mauSac ?? "-"} - ${item.kichCo ?? "-"}'),
                Text('SL: ${item.soLuong}'),
              ],
            ),
          ),
          Text(FormatUtils.money(item.thanhTien)),
        ],
      ),
    );
  }

  String? _resolveImageUrl(String? value) {
    final url = value?.trim();
    if (url == null || url.isEmpty) return null;
    if (url.startsWith('http://') || url.startsWith('https://')) return url;
    if (url.startsWith('/')) return '${ApiConstants.serverUrl}$url';
    return url;
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
