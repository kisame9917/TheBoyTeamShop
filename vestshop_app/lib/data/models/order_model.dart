class OrderItemModel {
  final int? idSanPhamChiTiet;
  final String? maSanPhamChiTiet;
  final String? tenSanPham;
  final String? mauSac;
  final String? kichCo;
  final int soLuong;
  final double donGia;
  final double thanhTien;
  final String? anhDaiDien;

  OrderItemModel({
    required this.idSanPhamChiTiet,
    required this.maSanPhamChiTiet,
    required this.tenSanPham,
    required this.mauSac,
    required this.kichCo,
    required this.soLuong,
    required this.donGia,
    required this.thanhTien,
    required this.anhDaiDien,
  });

  factory OrderItemModel.fromJson(Map<String, dynamic> json) {
    return OrderItemModel(
      idSanPhamChiTiet: json['idSanPhamChiTiet'] as int?,
      maSanPhamChiTiet: json['maSanPhamChiTiet']?.toString(),
      tenSanPham: json['tenSanPham']?.toString(),
      mauSac: json['mauSac']?.toString(),
      kichCo: json['kichCo']?.toString(),
      soLuong: (json['soLuong'] as num?)?.toInt() ?? 0,
      donGia: (json['donGia'] as num?)?.toDouble() ?? 0,
      thanhTien: (json['thanhTien'] as num?)?.toDouble() ?? 0,
      anhDaiDien: json['anhDaiDien']?.toString(),
    );
  }
}

class OrderModel {
  final int id;
  final String maHoaDon;
  final int trangThaiDon;
  final String? tenKhachHang;
  final String? soDienThoai;
  final double tongTien;
  final double tongTienGiam;
  final double tongTienSauGiam;
  final double phiVanChuyen;
  final int? idPhieuGiamGia;
  final List<OrderItemModel> items;

  OrderModel({
    required this.id,
    required this.maHoaDon,
    required this.trangThaiDon,
    required this.tenKhachHang,
    required this.soDienThoai,
    required this.tongTien,
    required this.tongTienGiam,
    required this.tongTienSauGiam,
    required this.phiVanChuyen,
    required this.idPhieuGiamGia,
    required this.items,
  });

  factory OrderModel.fromJson(Map<String, dynamic> json) {
    final rawItems =
        json['items'] ??
        json['chiTietHoaDons'] ??
        json['chiTietHoaDon'] ??
        json['hoaDonChiTiets'] ??
        [];

    final parsedItems = rawItems is List
        ? rawItems
            .map((e) => OrderItemModel.fromJson(Map<String, dynamic>.from(e)))
            .toList()
        : <OrderItemModel>[];

    return OrderModel(
      id: (json['id'] as num?)?.toInt() ?? 0,
      maHoaDon: json['maHoaDon']?.toString() ?? '',
      trangThaiDon: (json['trangThaiDon'] as num?)?.toInt() ?? 0,
      tenKhachHang: json['tenKhachHang']?.toString(),
      soDienThoai: json['soDienThoai']?.toString(),
      tongTien: (json['tongTien'] as num?)?.toDouble() ?? 0,
      tongTienGiam: (json['tongTienGiam'] as num?)?.toDouble() ?? 0,
      tongTienSauGiam: (json['tongTienSauGiam'] as num?)?.toDouble() ?? 0,
      phiVanChuyen: (json['phiVanChuyen'] as num?)?.toDouble() ?? 0,
      idPhieuGiamGia: (json['idPhieuGiamGia'] as num?)?.toInt(),
      items: parsedItems,
    );
  }
}