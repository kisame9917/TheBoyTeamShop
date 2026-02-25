// BanHangRequest.java
package com.vestshop.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BanHangRequest {
    private String maHoaDon;
    private Boolean loaiDon;

    @NotNull
    private BigDecimal phiVanChuyen;

    private Long idKhachHang;
    private Long idPhieuGiamGia;

    private Integer giamThuCongPercent;

    private String tenKhachHang;
    private String soDienThoai;
    private String emailKhachHang;
    private String diaChiKhachHang;
    private String ghiChu;

    private BigDecimal paid;

    // ✅ THÊM:
    private Long idPhuongThucThanhToan; // VD: 1=TIEN_MAT, 2=QR...
    private String maGiaoDich;          // nếu QR/bank có mã giao dịch
    private String ghiChuThanhToan;     // ghi chú riêng cho lịch sử thanh toán

    @NotEmpty
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull
        private Long idSanPhamChiTiet;
        @NotNull
        private Integer soLuong;
    }
}