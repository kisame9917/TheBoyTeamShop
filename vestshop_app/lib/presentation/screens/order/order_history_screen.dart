import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

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
  bool _isQrDialogOpen = false;
  int? _qrDialogOrderId;

  @override
  void initState() {
    super.initState();
    Future.microtask(() {
      final provider = context.read<OrderProvider>();
      provider.onShowQr = _showQrFromAdmin;
      provider.onQrPaid = _handleQrPaidFromAdmin;
      provider.startRealtimeOnly();
    });
  }

  @override
  void dispose() {
    final provider = context.read<OrderProvider>();
    provider.onShowQr = null;
    provider.onQrPaid = null;
    provider.disconnectRealtime();
    super.dispose();
  }

  Future<void> _showQrFromAdmin(OrderModel order) async {
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      if (!mounted) return;

      if (_isQrDialogOpen) {
        Navigator.of(context, rootNavigator: true).pop(false);
        await Future.delayed(const Duration(milliseconds: 120));
      }

      if (!mounted) return;

      _isQrDialogOpen = true;
      _qrDialogOrderId = order.id;

      await showDialog<bool>(
        context: context,
        barrierDismissible: false,
        builder: (_) => PaymentQrDialog(order: order),
      );

      _isQrDialogOpen = false;
      _qrDialogOrderId = null;
    });
  }

  void _handleQrPaidFromAdmin(int orderId, String message) {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;

      if (_isQrDialogOpen && (_qrDialogOrderId == null || _qrDialogOrderId == orderId)) {
        Navigator.of(context, rootNavigator: true).pop(true);
      }

      ScaffoldMessenger.of(context).clearSnackBars();
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(message),
          backgroundColor: Colors.green,
          duration: const Duration(seconds: 2),
        ),
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<OrderProvider>();

    return Scaffold(
      appBar: AppBar(title: const Text('Đơn hàng tại quầy')),
      body: provider.isLoading
          ? const Center(child: CircularProgressIndicator())
          : provider.orders.isEmpty
              ? const Center(child: Text('Chưa có đơn hàng tại quầy đang mở'))
              : ListView.builder(
                  padding: const EdgeInsets.all(12),
                  itemCount: provider.orders.length,
                  itemBuilder: (context, index) {
                    final order = provider.orders[index];
                    return _OrderCard(order: order);
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
            Text(
              order.maHoaDon,
              style: const TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 16,
              ),
            ),
            const SizedBox(height: 6),
            Text('Khách: ${order.tenKhachHang ?? "Khách lẻ"}'),
            Text('SĐT: ${order.soDienThoai ?? "-"}'),
            const SizedBox(height: 12),
            ...order.items.map(_buildItem),
            const Divider(),
            _row('Tổng tiền hàng', FormatUtils.money(order.tongTien)),
            _row('Giảm giá voucher', '- ${FormatUtils.money(order.tongTienGiam)}'),
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
    final imageUrl = item.anhDaiDien?.trim();

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
            child: imageUrl != null && imageUrl.isNotEmpty
                ? Image.network(
                    imageUrl,
                    width: 52,
                    height: 52,
                    fit: BoxFit.cover,
                    errorBuilder: (_, __, ___) => const Icon(Icons.broken_image),
                  )
                : const Icon(Icons.image),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  item.tenSanPham ?? 'Sản phẩm',
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
