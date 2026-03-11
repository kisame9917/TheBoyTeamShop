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

        String subject = "[VestShop] Tài khoản nhân viên của bạn đã được tạo";
        String safeName = (tenNhanVien == null || tenNhanVien.isBlank()) ? "bạn" : tenNhanVien.trim();
        String safeUsername = escapeHtml(taiKhoan);
        String safePassword = escapeHtml(matKhau);

        String html = """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
              <meta charset="UTF-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1.0" />
              <title>Thông tin tài khoản nhân viên</title>
            </head>
            <body style="margin:0;padding:0;background:#f4f6fb;font-family:Arial,Helvetica,sans-serif;color:#1f2937;">
              <div style="width:100%%;background:#f4f6fb;padding:32px 16px;">
                <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%%" style="max-width:640px;margin:0 auto;background:#ffffff;border-radius:18px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,0.08);">
                  
                  <tr>
                    <td style="background:linear-gradient(135deg,#0f172a,#1e3a8a);padding:28px 32px;color:#ffffff;">
                      <div style="font-size:13px;letter-spacing:1px;opacity:0.85;text-transform:uppercase;">VestShop</div>
                      <div style="font-size:26px;font-weight:700;margin-top:8px;">Tài khoản nhân viên đã được tạo</div>
                      <div style="font-size:14px;opacity:0.9;margin-top:8px;">
                        Chào mừng bạn đến với hệ thống quản lý của VestShop
                      </div>
                    </td>
                  </tr>

                  <tr>
                    <td style="padding:32px;">
                      <p style="margin:0 0 16px;font-size:15px;line-height:1.7;">
                        Xin chào <strong>%s</strong>,
                      </p>

                      <p style="margin:0 0 20px;font-size:15px;line-height:1.7;color:#374151;">
                        Tài khoản nhân viên của bạn đã được tạo thành công trên hệ thống <strong>VestShop</strong>.
                        Bạn có thể sử dụng thông tin dưới đây để đăng nhập.
                      </p>

                      <div style="background:#f8fafc;border:1px solid #e5e7eb;border-radius:14px;padding:20px;margin:24px 0;">
                        <div style="font-size:14px;color:#6b7280;margin-bottom:10px;">Thông tin đăng nhập</div>

                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                          <tr>
                            <td style="padding:10px 0;border-bottom:1px solid #e5e7eb;width:140px;font-size:14px;color:#6b7280;">
                              Tài khoản
                            </td>
                            <td style="padding:10px 0;border-bottom:1px solid #e5e7eb;font-size:15px;font-weight:600;color:#111827;">
                              %s
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:10px 0;width:140px;font-size:14px;color:#6b7280;">
                              Mật khẩu
                            </td>
                            <td style="padding:10px 0;font-size:15px;font-weight:600;color:#111827;">
                              %s
                            </td>
                          </tr>
                        </table>
                      </div>

                      <div style="background:#fff7ed;border:1px solid #fdba74;color:#9a3412;border-radius:12px;padding:14px 16px;margin:20px 0;font-size:14px;line-height:1.6;">
                        <strong>Lưu ý bảo mật:</strong> Sau khi đăng nhập lần đầu, bạn nên đổi mật khẩu ngay để bảo vệ tài khoản.
                      </div>

                      <p style="margin:20px 0 0;font-size:14px;line-height:1.7;color:#4b5563;">
                        Nếu bạn không phải người nhận email này, vui lòng liên hệ quản trị viên hệ thống để được hỗ trợ.
                      </p>

                      <p style="margin:24px 0 0;font-size:14px;line-height:1.7;color:#374151;">
                        Trân trọng,<br/>
                        <strong>VestShop Team</strong>
                      </p>
                    </td>
                  </tr>

                  <tr>
                    <td style="padding:18px 32px;background:#f9fafb;border-top:1px solid #e5e7eb;font-size:12px;line-height:1.6;color:#6b7280;text-align:center;">
                      Đây là email được gửi tự động từ hệ thống VestShop. Vui lòng không trả lời email này.
                    </td>
                  </tr>
                </table>
              </div>
            </body>
            </html>
            """.formatted(
                escapeHtml(safeName),
                safeUsername,
                safePassword
        );

        String plain = "Xin chào " + safeName + "\n\n"
                + "Tài khoản nhân viên của bạn đã được tạo trên hệ thống VestShop.\n\n"
                + "Thông tin đăng nhập:\n"
                + "- Tài khoản: " + taiKhoan + "\n"
                + "- Mật khẩu: " + matKhau + "\n\n"
                + "Khuyến nghị: Hãy đổi mật khẩu sau khi đăng nhập lần đầu.\n\n"
                + "Trân trọng,\nVestShop Team\n\n"
                + "Đây là email tự động, vui lòng không trả lời.";

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
