package com.vestshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhieuGiamGiaCaNhanResponse {
    private Long idPggCaNhan;

    private Long idKhachHang;
    private String maKhachHang;
    private String tenKhachHang;
    private String soDienThoai;
    private String email;

    private String maPhieuGiamGiaCaNhan;
    private LocalDateTime ngayNhan;
    private Boolean daSuDung;
}
