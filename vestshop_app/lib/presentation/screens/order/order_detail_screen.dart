import 'package:flutter/material.dart';

import '../../../data/models/order_model.dart';

class OrderDetailScreen extends StatelessWidget {
  final OrderModel order;

  const OrderDetailScreen({
    super.key,
    required this.order,
  });

  String _formatMoney(double value) {
    final number = value.round().toString();
    final buffer = StringBuffer();
    int count = 0;

    for (int i = number.length - 1; i >= 0; i--) {
      buffer.write(number[i]);
      count++;
      if (count % 3 == 0 && i != 0) buffer.write('.');
    }

    return '${buffer.toString().split('').reversed.join()} đ';
  }

  Widget _infoRow(String label, String value, {Color? valueColor}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 120,
            child: Text(
              label,
              style: const TextStyle(
                color: Colors.grey,
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: TextStyle(
                fontWeight: FontWeight.w600,
                color: valueColor,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _orderItem(OrderItemModel item) {
    final imageUrl = item.anhDaiDien?.trim();

    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        border: Border.all(color: Colors.black12),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          ClipRRect(
            borderRadius: BorderRadius.circular(8),
            child: imageUrl != null && imageUrl.isNotEmpty
                ? Image.network(
                    imageUrl,
                    width: 72,
                    height: 72,
                    fit: BoxFit.cover,
                    errorBuilder: (_, __, ___) {
                      return Container(
                        width: 72,
                        height: 72,
                        color: Colors.grey.shade200,
                        alignment: Alignment.center,
                        child: const Icon(Icons.image_not_supported_outlined),
                      );
                    },
                  )
                : Container(
                    width: 72,
                    height: 72,
                    color: Colors.grey.shade200,
                    alignment: Alignment.center,
                    child: const Icon(Icons.inventory_2_outlined),
                  ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  item.tenSanPham ?? 'Sản phẩm',
                  style: const TextStyle(
                    fontWeight: FontWeight.w700,
                    fontSize: 15,
                  ),
                ),
                const SizedBox(height: 6),
                Text('Mã: ${item.maSanPhamChiTiet ?? '-'}'),
                Text('Màu: ${item.mauSac ?? '-'}'),
                Text('Size: ${item.kichCo ?? '-'}'),
                Text('Số lượng: ${item.soLuong}'),
                const SizedBox(height: 6),
                Text(
                  'Đơn giá: ${_formatMoney(item.donGia)}',
                  style: const TextStyle(fontWeight: FontWeight.w500),
                ),
                Text(
                  'Thành tiền: ${_formatMoney(item.thanhTien)}',
                  style: const TextStyle(
                    fontWeight: FontWeight.w700,
                    color: Colors.red,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Đơn hàng tại quầy')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              border: Border.all(color: Colors.black12),
              borderRadius: BorderRadius.circular(12),
              color: Colors.white,
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Thông tin đơn hàng',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 12),
                _infoRow('Mã hóa đơn', order.maHoaDon),
                _infoRow('Khách hàng', order.tenKhachHang ?? 'Khách lẻ'),
                _infoRow('SĐT', order.soDienThoai ?? '-'),
                _infoRow('Tổng tiền', _formatMoney(order.tongTien)),
                _infoRow('Giảm giá', _formatMoney(order.tongTienGiam)),
                _infoRow('Phí ship', _formatMoney(order.phiVanChuyen)),
                _infoRow(
                  'Cần thanh toán',
                  _formatMoney(order.tongTienSauGiam),
                  valueColor: Colors.red,
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),
          const Text(
            'Sản phẩm',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
            ),
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
            ...order.items.map(_orderItem),
        ],
      ),
    );
  }
}
