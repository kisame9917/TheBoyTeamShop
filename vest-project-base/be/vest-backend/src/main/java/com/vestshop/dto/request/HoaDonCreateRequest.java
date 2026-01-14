package com.vestshop.dto.request;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HoaDonCreateRequest {
    private Long idKhachHang;
    private Long idNhanVien;
    private Long idPhieuGiamGia;

    private Boolean loaiDon;
    private BigDecimal phiVanChuyen;

    private String tenKhachHang;
    private String soDienThoai;
    private String diaChiKhachHang;
    private String emailKhachHang;

    private String qrCode;
    private String ghiChu;

    private List<Item> items;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Item {
        private Long idSanPhamChiTiet;
        private Integer soLuong;
    }
}
