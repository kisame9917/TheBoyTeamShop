import 'package:flutter/foundation.dart';
import '../../core/network/api_client.dart';
import '../models/order_model.dart';

class OrderService {
  final ApiClient apiClient;
  OrderService(this.apiClient);

  Future<List<OrderModel>> getActivePosDrafts() async {
    final data = await apiClient.get('/hoa-don/drafts/pos-active');
    debugPrint('POS DRAFT RAW DATA = $data');

    if (data is! List) {
      throw Exception('API không trả về List. data=$data');
    }

    final result = data
        .map((e) => OrderModel.fromJson(Map<String, dynamic>.from(e)))
        .toList();

    debugPrint('POS DRAFT PARSED LENGTH = ${result.length}');
    for (final o in result) {
      debugPrint('ORDER id=${o.id}, ma=${o.maHoaDon}, items=${o.items.length}');
    }

    return result;
  }
}