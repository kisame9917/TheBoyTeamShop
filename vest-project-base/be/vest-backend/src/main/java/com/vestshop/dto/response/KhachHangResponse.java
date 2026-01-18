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
    private String soDienThoai;
    private String taiKhoan;
    private String email;

    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private Boolean trangThai;
}

