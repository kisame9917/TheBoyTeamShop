package com.vestshop.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HoaDonDetailResponse {
    private Long id;
    private String maHoaDon;

    private Long idKhachHang;
    private Long idNhanVien;
    private Long idPhieuGiamGia;

    private Integer trangThaiDon;
    private String tenTrangThaiDon;

    private Boolean loaiDon;

    private BigDecimal phiVanChuyen;
    private BigDecimal tongTien;
    private BigDecimal tongTienGiam;
    private BigDecimal tongTienSauGiam;

    private String tenKhachHang;
    private String soDienThoai;
    private String diaChiKhachHang;
    private String emailKhachHang;

    private String qrCode;
    private String ghiChu;

    private Boolean trangThai;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    private List<Item> items;

    private List<LichSuHoaDonResponse> lichSuHoaDon;
    private List<LichSuThanhToanResponse> lichSuThanhToan;
    private List<GiaoDichThanhToanResponse> giaoDichThanhToan;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Item {
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
