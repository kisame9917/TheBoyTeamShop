package com.vestshop.Service.impl;

import com.vestshop.Exception.ApiException;
import com.vestshop.Service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

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

    private String resolveFromEmail() {
        if (smtpUsername == null || smtpUsername.isBlank()) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Chưa cấu hình SMTP (spring.mail.username). Vui lòng cấu hình MAIL_USERNAME/MAIL_PASSWORD");
        }
        return (from == null || from.isBlank()) ? smtpUsername : from;
    }

    // ✅ HÀM DÙNG CHUNG (HTML + plain fallback)
    private void sendRichEmail(String to, String subject, String plainText, String html) {
        log.info("[MAIL] enabled={}, to={}, subject={}", enabled, to, subject);
        if (!enabled) return;

        if (to == null || to.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email người nhận không được để trống");
        }

        String fromEmail = resolveFromEmail();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);

            // plain + html
            helper.setText(plainText == null ? "" : plainText, html);

            mailSender.send(message);
            log.info("[MAIL] sent OK to={}", to);
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Gửi email thất bại: " + ex.getMessage());
        }
    }

    @Override
    public void sendNewNhanVienCredentials(String toEmail, String tenNhanVien, String taiKhoan, String matKhau) {
        if (toEmail == null || toEmail.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email không được để trống để gửi thông tin tài khoản");
        }

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
                  <hr style="border: none; border-top: 1px solid #eee; margin: 16px 0"/>
                  <p style="color: #666; font-size: 12px">Email này được gửi tự động. Vui lòng không trả lời.</p>
                </div>
                """.formatted(
                escapeHtml(safeName),
                escapeHtml(taiKhoan),
                escapeHtml(matKhau)
        );

        String plain = "Xin chào " + safeName + "\n\n"
                + "Tài khoản nhân viên của bạn trên VestShop:\n"
                + "- Tài khoản: " + taiKhoan + "\n"
                + "- Mật khẩu: " + matKhau + "\n\n"
                + "Email tự động, vui lòng không trả lời.";

        sendRichEmail(toEmail.trim(), subject, plain, html);
    }

    @Override
    public void sendResetPasswordOtp(String to, String tenNhanVien, String otp) {
        if (to == null || to.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email không được để trống để gửi OTP");
        }
        if (otp == null || otp.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "OTP không hợp lệ");
        }

        String name = (tenNhanVien == null || tenNhanVien.isBlank()) ? "bạn" : tenNhanVien.trim();
        String subject = "[VestShop] OTP đặt lại mật khẩu";

        String plain = "Xin chào " + name + "\n\n"
                + "Mã OTP đặt lại mật khẩu của bạn: " + otp + "\n"
                + "OTP hết hạn trong 10 phút. Không chia sẻ mã này cho bất kỳ ai.\n\n"
                + "VestShop";

        String html = """
                <div style="font-family: Arial, Helvetica, sans-serif; font-size: 14px; line-height: 1.5">
                  <p>Xin chào <b>%s</b>,</p>
                  <p>Mã OTP đặt lại mật khẩu của bạn là:</p>
                  <div style="font-size:30px;font-weight:800;letter-spacing:4px">%s</div>
                  <p>OTP hết hạn trong <b>10 phút</b>. Không chia sẻ mã này cho bất kỳ ai.</p>
                  <hr style="border: none; border-top: 1px solid #eee; margin: 16px 0"/>
                  <p style="color: #666; font-size: 12px">Email này được gửi tự động. Vui lòng không trả lời.</p>
                </div>
                """.formatted(escapeHtml(name), escapeHtml(otp));

        sendRichEmail(to.trim(), subject, plain, html);
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
