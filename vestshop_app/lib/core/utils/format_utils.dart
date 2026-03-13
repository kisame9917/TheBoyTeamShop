import 'package:intl/intl.dart';

class FormatUtils {
  static String money(num value) {
    final f = NumberFormat.currency(locale: 'vi_VN', symbol: 'đ', decimalDigits: 0);
    return f.format(value);
  }
}