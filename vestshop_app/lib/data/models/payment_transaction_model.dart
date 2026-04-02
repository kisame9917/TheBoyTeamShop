class PaymentTransactionModel {
  final int? id;
  final String? tenPhuongThucThanhToan;
  final double soTien;
  final String? maGiaoDich;
  final String? duLieuQr;
  final String? ghiChu;

  PaymentTransactionModel({
    required this.id,
    required this.tenPhuongThucThanhToan,
    required this.soTien,
    required this.maGiaoDich,
    required this.duLieuQr,
    required this.ghiChu,
  });

  factory PaymentTransactionModel.fromJson(Map<String, dynamic> json) {
    return PaymentTransactionModel(
      id: (json['id'] as num?)?.toInt(),
      tenPhuongThucThanhToan: json['tenPhuongThucThanhToan']?.toString(),
      soTien: (json['soTien'] as num?)?.toDouble() ?? 0,
      maGiaoDich: json['maGiaoDich']?.toString(),
      duLieuQr: json['duLieuQr']?.toString(),
      ghiChu: json['ghiChu']?.toString(),
    );
  }
}