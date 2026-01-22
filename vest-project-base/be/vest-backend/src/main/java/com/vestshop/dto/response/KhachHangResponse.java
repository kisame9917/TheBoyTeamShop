package com.vestshop.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHangResponse {
    private Long id;
    private String maKhachHang;
    private String tenKhachHang;

    private Boolean gioiTinh;  // true=Nam, false=Nữ
    private String email;
    private String soDienThoai;

    private String taiKhoan;

    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    private Boolean trangThai;
    private String anhDaiDien;
    // fill form
    private String tenNguoiNhan;
    private String sdtNguoiNhan;
    private String tinhThanh;
    private String quanHuyen;
    private String phuongXa;
    private String diaChiChiTiet;
    // Địa chỉ mặc định dạng string để list hiển thị nhanh
    private String diaChi;

    // Địa chỉ mặc định chi tiết để modal/detail/form dùng
    private DiaChiKhachHangResponse diaChiMacDinh;
}

