package com.vestshop.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientOrderUpdateShippingRequest {
    private String tenNguoiNhanHang;
    private String soDienThoaiNhanHang;
    private String tinhThanhNhanHang;
    private String quanHuyenNhanHang;
    private String phuongXaNhanHang;
    private String diaChiNhanHangChiTiet;
    private String ghiChu;
}