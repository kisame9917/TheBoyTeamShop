package com.vestshop.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PosDraftSyncRequest {
    private String maHoaDon;
    private Boolean loaiDon;
    private BigDecimal phiVanChuyen;

    private Long idKhachHang;
    private String tenKhachHang;
    private String soDienThoai;
    private String emailKhachHang;
    private String diaChiKhachHang;

    private Long idPhieuGiamGia;
    private Long pggId;
    private Integer giamThuCongPercent;

    private BigDecimal paid;
    private String ghiChu;

    private String tenNguoiNhanHang;
    private String soDienThoaiNhanHang;
    private String tinhThanhNhanHang;
    private String quanHuyenNhanHang;
    private String phuongXaNhanHang;
    private String diaChiNhanHangChiTiet;

    private List<Item> items;

    @Data
    public static class Item {
        private Long idSanPhamChiTiet;
        private Integer soLuong;
    }
}