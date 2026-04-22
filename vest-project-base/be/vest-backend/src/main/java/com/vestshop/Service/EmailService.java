package com.vestshop.Service;

import com.vestshop.dto.response.HoaDonDetailResponse;

/** Gửi email thông tin tài khoản cho nhân viên khi đăng ký. */
public interface EmailService {
    void sendNewNhanVienCredentials(String toEmail, String tenNhanVien, String taiKhoan, String matKhau);
    void sendResetPasswordOtp(String to, String tenNhanVien, String otp);
    void sendShippingOrderConfirmation(String toEmail, String tenNguoiNhan, HoaDonDetailResponse order);
    void sendNewKhachHangCredentials(String toEmail, String tenKhachHang, String taiKhoan, String matKhau);
}
