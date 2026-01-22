package com.vestshop.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhanVienResponse {
    private Long id;

    private Long quyenHanId;
    private String tenQuyenHan;

    private String maNhanVien;
    private String tenNhanVien;
    private String soDienThoai;
    private String cccd;
    private String email;
    private String taiKhoan;

    private LocalDate ngaySinh;
    private Boolean gioiTinh;
    private String diaChi;

    private String nguoiCapNhat;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private Boolean trangThai;

    private String anhDaiDien;
}
