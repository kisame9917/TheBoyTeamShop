package com.vestshop.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhanVienRequest {

    /** Quyền hạn / vai trò (id_quyen_han) */
    @NotNull
    private Long quyenHanId;

    /** Mã nhân viên (ma_nhan_vien) */
    @NotBlank
    @Size(max = 80)
    private String maNhanVien;

    /** Tên nhân viên (ten_nhan_vien) */
    @NotBlank
    @Size(max = 255)
    private String tenNhanVien;

    /** Số điện thoại (so_dien_thoai) */
    @Size(max = 20)
    private String soDienThoai;

    /** CCCD (cccd) */
    @Size(max = 20)
    private String cccd;

    /** Email (email) */
    @Email
    @Size(max = 255)
    private String email;

    /** Tài khoản (tai_khoan) */
    @NotBlank
    @Size(max = 80)
    private String taiKhoan;

    /** Mật khẩu (mat_khau). Khi update có thể bỏ trống để giữ nguyên. */
    @Size(max = 255)
    private String matKhau;

    private LocalDate ngaySinh;
    private Boolean gioiTinh;

    @Size(max = 255)
    private String diaChi;

    /** Trạng thái (trang_thai). Null sẽ mặc định true khi tạo mới. */
    private Boolean trangThai;
}

