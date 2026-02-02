package com.vestshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopKhachHangResponse {
    private Long idKhachHang;
    private String tenKhachHang;
    private String soDienThoai;
    private Long soLanMua;
    private BigDecimal tongTienChiTieu;
}
