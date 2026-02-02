package com.vestshop.Service.impl;

import com.vestshop.Exception.ApiException;
import com.vestshop.Service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:true}")
    private boolean enabled;

    @Value("${app.mail.from:}")
    private String from;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Override
    public void sendNewNhanVienCredentials(String toEmail, String tenNhanVien, String taiKhoan, String matKhau) {
        log.info("[MAIL] enabled={}, to={}", enabled, toEmail);
        if (!enabled) return;

        if (toEmail == null || toEmail.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email không được để trống để gửi thông tin tài khoản");
        }

        if (smtpUsername == null || smtpUsername.isBlank()) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Chưa cấu hình SMTP (spring.mail.username). Vui lòng cấu hình MAIL_USERNAME/MAIL_PASSWORD");
        }


        String fromEmail = (from == null || from.isBlank()) ? smtpUsername : from;

        String subject = "[VestShop] Thông tin tài khoản nhân viên";
        String safeName = (tenNhanVien == null || tenNhanVien.isBlank()) ? "bạn" : tenNhanVien.trim();

        String html = """
                <div style="font-family: Arial, Helvetica, sans-serif; font-size: 14px; line-height: 1.5">
                  <p>Xin chào <b>%s</b>,</p>
                  <p>Tài khoản nhân viên của bạn đã được tạo trên hệ thống <b>VestShop</b>. Thông tin đăng nhập:</p>
                  <ul>
                    <li><b>Tài khoản:</b> %s</li>
                    <li><b>Mật khẩu:</b> %s</li>
                  </ul>
                  <p style="margin-top: 12px"><i>Vui lòng đổi mật khẩu ngay sau khi đăng nhập (nếu hệ thống có hỗ trợ).</i></p>
                  <hr style="border: none; border-top: 1px solid #eee; margin: 16px 0"/>
                  <p style="color: #666; font-size: 12px">Email này được gửi tự động. Vui lòng không trả lời.</p>
                </div>
                """.formatted(
                escapeHtml(safeName),
                escapeHtml(taiKhoan),
                escapeHtml(matKhau)
        );

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            log.info("[MAIL] sending from={} to={}", fromEmail, toEmail);
            mailSender.send(message);
            log.info("[MAIL] sending from={} to={}", fromEmail, toEmail);
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Gửi email thất bại: " + ex.getMessage());
        }
    }

    // ✅ BẮT BUỘC có để khỏi lỗi đỏ như bạn đang gặp
    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

}
