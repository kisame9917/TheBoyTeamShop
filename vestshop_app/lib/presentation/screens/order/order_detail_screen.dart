import 'package:flutter/material.dart';

import '../../../core/constants/api_constants.dart';
import '../../../core/network/api_client.dart';
import '../../../data/models/order_model.dart';
import '../../../data/services/order_service.dart';
import '../../widgets/payment_qr_dialog.dart';

class OrderDetailScreen extends StatefulWidget {
  final OrderModel order;

  const OrderDetailScreen({
    super.key,
    required this.order,
  });

  @override
  State<OrderDetailScreen> createState() => _OrderDetailScreenState();
}

class _OrderDetailScreenState extends State<OrderDetailScreen> {
  late final OrderService _orderService;
  late OrderModel _order;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _order = widget.order;
    _orderService = OrderService(
      ApiClient(baseUrl: ApiConstants.baseUrl),
    );
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

  Future<void> _refreshDetail() async {
    setState(() => _isLoading = true);
    try {
      final order = await _orderService.getOrderDetail(_order.id);
      if (!mounted) return;
      setState(() {
        _order = order;
      });
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Không tải được chi tiết đơn: $e')),
      );
    } finally {
      if (mounted) {
        setState(() => _isLoading = false);
      }
    }
  }

  Future<void> _showQr() async {
  try {
    final order = await _orderService.getOrderDetail(_order.id);

    if (!mounted) return;

    final removed = await showDialog<bool>(
      context: context,
      barrierDismissible: false,
      builder: (_) => PaymentQrDialog(order: order),
    );

    if (removed == true) {
      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Đã ghi nhận thanh toán realtime')),
      );

      Navigator.pop(context, true);
    }
  } catch (e) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Không lấy được QR: $e')),
    );
  }
}

  Widget _buildInfoRow(String label, String value, {Color? valueColor}) {
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

  Widget _buildOrderItem(OrderItemModel item) {
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
          if (item.anhDaiDien != null && item.anhDaiDien!.isNotEmpty)
            ClipRRect(
              borderRadius: BorderRadius.circular(8),
              child: Image.network(
                item.anhDaiDien!,
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
              ),
            )
          else
            Container(
              width: 72,
              height: 72,
              decoration: BoxDecoration(
                color: Colors.grey.shade200,
                borderRadius: BorderRadius.circular(8),
              ),
              alignment: Alignment.center,
              child: const Icon(Icons.inventory_2_outlined),
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

  Widget _buildSummaryCard() {
    return Container(
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
          _buildInfoRow('Mã hóa đơn', _order.maHoaDon),
          _buildInfoRow('Khách hàng', _order.tenKhachHang ?? 'Khách lẻ'),
          _buildInfoRow('SĐT', _order.soDienThoai ?? '-'),
          _buildInfoRow('Tổng tiền', _formatMoney(_order.tongTien)),
          _buildInfoRow('Giảm giá', _formatMoney(_order.tongTienGiam)),
          _buildInfoRow('Phí ship', _formatMoney(_order.phiVanChuyen)),
          _buildInfoRow(
            'Cần thanh toán',
            _formatMoney(_order.tongTienSauGiam),
            valueColor: Colors.red,
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(_order.maHoaDon),
        actions: [
          IconButton(
            onPressed: _isLoading ? null : _refreshDetail,
            icon: const Icon(Icons.refresh),
          ),
        ],
      ),
      body: Stack(
        children: [
          RefreshIndicator(
            onRefresh: _refreshDetail,
            child: ListView(
              padding: const EdgeInsets.all(16),
              children: [
                _buildSummaryCard(),
                const SizedBox(height: 16),
                Row(
                  children: [
                    Expanded(
                      child: ElevatedButton.icon(
                        onPressed: _showQr,
                        icon: const Icon(Icons.qr_code),
                        label: const Text('Thanh toán QR'),
                      ),
                    ),
                  ],
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
                if (_order.items.isEmpty)
                  Container(
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      border: Border.all(color: Colors.black12),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: const Text('Chưa có sản phẩm trong đơn'),
                  )
                else
                  ..._order.items.map(_buildOrderItem),
              ],
            ),
          ),
          if (_isLoading)
            Container(
              color: Colors.black.withOpacity(0.05),
              child: const Center(
                child: CircularProgressIndicator(),
              ),
            ),
        ],
      ),
    );
  }
}