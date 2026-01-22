package com.vestshop.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaChiKhachHangResponse {
    private Long id;
    private Long idKhachHang;

    private String tenNguoiNhan;
    private String soDienThoai;

    private String diaChiChiTiet;
    private String phuongXa;
    private String quanHuyen;
    private String tinhThanh;
    private String quocGia;

    private Boolean laMacDinh;
    private Boolean trangThai;
}
