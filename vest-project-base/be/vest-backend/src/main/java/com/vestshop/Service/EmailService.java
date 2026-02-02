package com.vestshop.Service;

/** Gửi email thông tin tài khoản cho nhân viên khi đăng ký. */
public interface EmailService {
    void sendNewNhanVienCredentials(String toEmail, String tenNhanVien, String taiKhoan, String matKhau);
}
