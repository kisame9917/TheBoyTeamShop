// BanHangRequest.java
package com.vestshop.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BanHangRequest {
    private String maHoaDon;

    // ✅ giữ Boolean nhưng cho phép FE gửi 0/1 (number/string) mà không lỗi
    private Boolean loaiDon;

    @JsonSetter("loaiDon")
    public void setLoaiDonFlexible(Object v) {
        if (v == null) { this.loaiDon = null; return; }
        if (v instanceof Boolean b) { this.loaiDon = b; return; }
        if (v instanceof Number n) { this.loaiDon = n.intValue() == 1; return; }
        String s = v.toString().trim();
        if ("1".equals(s) || "true".equalsIgnoreCase(s)) this.loaiDon = true;
        else if ("0".equals(s) || "false".equalsIgnoreCase(s)) this.loaiDon = false;
        else this.loaiDon = null;
    }

    @NotNull
    private BigDecimal phiVanChuyen;

    private Long idKhachHang;
    private Long idPhieuGiamGia;

    // ✅ FE đang dùng pggId -> BE map về idPhieuGiamGia
    private Long pggId;

    private Integer giamThuCongPercent;

    private String tenKhachHang;
    private String soDienThoai;
    private String emailKhachHang;
    private String diaChiKhachHang;
    private String ghiChu;

    private BigDecimal paid;

    private Long idPhuongThucThanhToan;
    private String maGiaoDich;
    private String ghiChuThanhToan;

    // =========================
    // ✅ SHIP (FE gửi lên)
    // =========================
    private String tenNguoiNhanHang;
    private String soDienThoaiNhanHang;

    // FE hiện chọn 2 cấp: tỉnh + phường + chi tiết
    private String tinhThanhNhanHang;
    private String phuongXaNhanHang;

    // FE có thể vẫn gửi field này (rỗng) -> nhận để không fail
    private String quanHuyenNhanHang;

    private String diaChiNhanHangChiTiet;

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