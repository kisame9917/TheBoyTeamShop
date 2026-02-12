package com.vestshop.dto.request;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BanHangRequest {
    private String maHoaDon;
    private Boolean loaiDon;          // FE gửi false
    @NotNull
    private BigDecimal phiVanChuyen;  // FE gửi 0

    private Long idKhachHang;
    private Long idPhieuGiamGia;

    private Integer giamThuCongPercent; // 0..100 (khi không dùng voucher)

    private String tenKhachHang;
    private String soDienThoai;
    private String emailKhachHang;
    private String diaChiKhachHang;
    private String ghiChu;

    private BigDecimal paid; // tiền khách đưa

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
