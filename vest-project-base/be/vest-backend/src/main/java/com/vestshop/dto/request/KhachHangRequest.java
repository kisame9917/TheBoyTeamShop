package com.vestshop.dto.request;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHangRequest {

    // maKhachHang: FE có thể gửi/hiển thị nhưng BE sẽ tự sinh khi create
    private String maKhachHang;

    private String tenKhachHang;
    private Boolean gioiTinh; // bit: 1 nam(true), 0 nữ(false)
    private String email;
    private String soDienThoai;

    private String taiKhoan;
    private String matKhau;

    private Boolean trangThai; // true: hoạt động, false: không hoạt động
    private String anhDaiDien; // /uploads/khachhang/xxx.jpg
    private LocalDate ngaySinh;
    // ===== ĐỊA CHỈ MẶC ĐỊNH =====
    private String tenNguoiNhan;
    private String sdtNguoiNhan;
    private String diaChiChiTiet;
    private String phuongXa;
    private String quanHuyen;
    private String tinhThanh;
    private String quocGia;
    // ✅ MỚI: danh sách địa chỉ
    private List<DiaChiKhachHangRequest> diaChiList;

    // ✅ MỚI: chọn địa chỉ mặc định khi update
    private Long diaChiMacDinhId;
}
