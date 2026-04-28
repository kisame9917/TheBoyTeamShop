import 'package:flutter/material.dart';

import '../../../core/constants/api_constants.dart';
import '../../../core/utils/format_utils.dart';
import '../../../data/models/order_model.dart';

class OrderDetailScreen extends StatelessWidget {
  final OrderModel order;

  const OrderDetailScreen({
    super.key,
    required this.order,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(order.maHoaDon)),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _summaryCard(),
          const SizedBox(height: 20),
          const Text(
            'Sản phẩm',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),
          if (order.items.isEmpty)
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                border: Border.all(color: Colors.black12),
                borderRadius: BorderRadius.circular(12),
              ),
              child: const Text('Chưa có sản phẩm trong đơn'),
            )
          else
            ...order.items.map(_itemCard),
        ],
      ),
    );
  }

  Widget _summaryCard() {
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(
                    order.maHoaDon,
                    style: const TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
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
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            _row('Khách hàng', order.tenKhachHang ?? 'Khách lẻ'),
            _row('SĐT', order.soDienThoai ?? '-'),
            const Divider(),
            _row('Tổng tiền hàng', FormatUtils.money(order.tongTien)),
            _row('Giảm giá', '- ${FormatUtils.money(order.tongTienGiam)}'),
            _row('Phí ship', FormatUtils.money(order.phiVanChuyen)),
            _row(
              'Cần thanh toán',
              FormatUtils.money(order.tongTienSauGiam),
              isBold: true,
            ),
          ],
        ),
      ),
    );
  }

  Widget _itemCard(OrderItemModel item) {
    final imageUrl = _resolveImageUrl(item.anhDaiDien);

    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.grey.shade100,
        borderRadius: BorderRadius.circular(12),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 60,
            height: 60,
            clipBehavior: Clip.antiAlias,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(10),
            ),
            child: imageUrl != null
                ? Image.network(
                    imageUrl,
                    fit: BoxFit.cover,
                    errorBuilder: (_, __, ___) {
                      return const Icon(Icons.broken_image);
                    },
                  )
                : const Icon(Icons.image),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  item.tenSanPham ?? '',
                  style: const TextStyle(fontWeight: FontWeight.w700),
                ),
                const SizedBox(height: 4),
                Text('${item.mauSac ?? "-"} - ${item.kichCo ?? "-"}'),
                Text('Số lượng: ${item.soLuong}'),
                Text('Đơn giá: ${FormatUtils.money(item.donGia)}'),
              ],
            ),
          ),
          Text(
            FormatUtils.money(item.thanhTien),
            style: const TextStyle(fontWeight: FontWeight.w700),
          ),
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

  Widget _row(String label, String value, {bool isBold = false}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          Expanded(child: Text(label)),
          Text(
            value,
            style: TextStyle(
              fontWeight: isBold ? FontWeight.bold : FontWeight.normal,
              color: isBold ? Colors.red : null,
            ),
          ),
        ],
      ),
    );
  }
}
