package com.vestshop.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHangRequest {

    /**
     * Mã khách hàng (ma_khach_hang)
     */
    @NotBlank
    @Size(max = 80)
    private String maKhachHang;

    /**
     * Tên khách hàng (ten_khach_hang)
     */
    @NotBlank
    @Size(max = 255)
    private String tenKhachHang;

    /**
     * Số điện thoại (so_dien_thoai)
     */
    @Size(max = 20)
    private String soDienThoai;

    /**
     * Tài khoản đăng nhập (tai_khoan)
     */
    @NotBlank
    @Size(max = 80)
    private String taiKhoan;

    /**
     * Mật khẩu (mat_khau). Khi update có thể bỏ trống để giữ nguyên.
     */
    @Size(max = 255)
    private String matKhau;

    /**
     * Email (email)
     */
    @Email
    @Size(max = 255)
    private String email;

    /**
     * Trạng thái (trang_thai). Null sẽ mặc định true khi tạo mới.
     */
    private Boolean trangThai;
}

