package com.vestshop.dto.request;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaChiKhachHangRequest {
    private Long id;
    private String tenNguoiNhan;
    private String soDienThoai;
    private String tinhThanh;
    private String quanHuyen;
    private String phuongXa;
    private String diaChiChiTiet;
    private String quocGia;
    private Boolean laMacDinh;
}
