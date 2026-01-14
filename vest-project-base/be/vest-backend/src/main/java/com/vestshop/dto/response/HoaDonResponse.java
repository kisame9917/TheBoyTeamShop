package com.vestshop.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HoaDonResponse {
    private Long id;
    private String maHoaDon;

    private Long idKhachHang;
    private Long idNhanVien;
    private Long idPhieuGiamGia;

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

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Item {
        private Long idSanPhamChiTiet;
        private Integer soLuong;
        private BigDecimal donGia;
        private BigDecimal thanhTien;
    }
}
