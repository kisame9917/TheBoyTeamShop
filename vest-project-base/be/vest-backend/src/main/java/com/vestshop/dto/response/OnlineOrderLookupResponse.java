package com.vestshop.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnlineOrderLookupResponse {

    private Long id;
    private String maHoaDon;

    private Integer trangThaiDon;
    private String tenTrangThaiDon;

    private String paymentMethod;
    private String paymentStatus;

    private String tenKhachHang;
    private String soDienThoai;

    private String tenNguoiNhanHang;
    private String soDienThoaiNhanHang;

    private String tinhThanhNhanHang;
    private String quanHuyenNhanHang;
    private String phuongXaNhanHang;
    private String diaChiNhanHangChiTiet;

    private BigDecimal phiVanChuyen;
    private BigDecimal tongTien;
    private BigDecimal tongTienGiam;
    private BigDecimal tongTienSauGiam;

    private String ghiChu;
    private LocalDateTime ngayTao;

    private List<Item> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {
        private Long idHoaDonChiTiet;
        private Long idSanPhamChiTiet;
        private String maSanPhamChiTiet;
        private String tenSanPham;
        private String mauSac;
        private String kichCo;
        private Integer soLuong;
        private BigDecimal donGia;
        private BigDecimal thanhTien;
        private String anhDaiDien;
    }
}