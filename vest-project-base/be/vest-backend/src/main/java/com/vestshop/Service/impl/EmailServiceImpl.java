package com.vestshop.Service.impl;

import com.vestshop.Exception.ApiException;
import com.vestshop.Service.EmailService;
import com.vestshop.dto.response.HoaDonDetailResponse;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Locale LOCALE_VI = new Locale("vi", "VN");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final JavaMailSender mailSender;


    @Value("${app.mail.enabled:true}")
    private boolean enabled;

    @Value("${app.mail.from:}")
    private String from;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void sendShippingOrderConfirmation(String toEmail, String tenNguoiNhan, HoaDonDetailResponse order) {
        if (toEmail == null || toEmail.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email người nhận không được để trống");
        }
        if (order == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Thông tin đơn hàng không hợp lệ");
        }

        String recipientName = safeText(tenNguoiNhan, "Quý khách");
        String maHoaDon = safeText(order.getMaHoaDon(), "-");
        String subject = "[VestShop] Xác nhận đơn giao hàng #" + maHoaDon;

        String shippingAddress = joinNonBlank(
                order.getDiaChiNhanHangChiTiet(),
                order.getPhuongXaNhanHang(),
                order.getQuanHuyenNhanHang(),
                order.getTinhThanhNhanHang()
        );

        String buyerPhone = safeText(order.getSoDienThoai(), "-");
        String receiverPhone = safeText(order.getSoDienThoaiNhanHang(), "-");
        String status = safeText(order.getTenTrangThaiDon(), "Đang xử lý");
        String createdAt = formatDate(order.getNgayTao());
        String note = safeText(order.getGhiChu(), "Không có");
        String buyerName = safeText(order.getTenKhachHang(), recipientName);
        String lookupUrl = "http://localhost:5174/tra-cuu-don-hang"
                + "?maHoaDon=" + URLEncoder.encode(maHoaDon, StandardCharsets.UTF_8)
                + "&soDienThoai=" + URLEncoder.encode(receiverPhone, StandardCharsets.UTF_8);



        List<HoaDonDetailResponse.Item> items = order.getItems() == null ? List.of() : order.getItems();
        StringBuilder itemsHtml = new StringBuilder();
        StringBuilder itemsPlain = new StringBuilder();

        if (items.isEmpty()) {
            itemsHtml.append("""
            <tr>
              <td colspan="4" style="padding:14px 16px;border-top:1px solid #e5e7eb;color:#6b7280;font-size:14px;text-align:center;">
                Không có chi tiết sản phẩm
              </td>
            </tr>
            """);
            itemsPlain.append("- Không có chi tiết sản phẩm\n");
        } else {
            for (HoaDonDetailResponse.Item item : items) {
                String tenSp = safeText(item.getTenSanPham(), "Sản phẩm");
                String phanLoai = joinNonBlank(item.getMauSac(), item.getKichCo());
                String tenHienThi = phanLoai.isBlank() ? tenSp : tenSp + " (" + phanLoai + ")";
                String soLuong = String.valueOf(item.getSoLuong() == null ? 0 : item.getSoLuong());
                String donGia = formatMoney(item.getDonGia());
                String thanhTien = formatMoney(item.getThanhTien());

                itemsHtml.append("""
                <tr>
                  <td style="padding:12px 16px;border-top:1px solid #e5e7eb;font-size:14px;color:#111827;">%s</td>
                  <td style="padding:12px 16px;border-top:1px solid #e5e7eb;font-size:14px;color:#111827;text-align:center;">%s</td>
                  <td style="padding:12px 16px;border-top:1px solid #e5e7eb;font-size:14px;color:#111827;text-align:right;">%s</td>
                  <td style="padding:12px 16px;border-top:1px solid #e5e7eb;font-size:14px;color:#111827;text-align:right;">%s</td>
                </tr>
                """.formatted(
                        escapeHtml(tenHienThi),
                        escapeHtml(soLuong),
                        escapeHtml(donGia),
                        escapeHtml(thanhTien)
                ));

                itemsPlain.append("- ")
                        .append(tenHienThi)
                        .append(" | SL: ")
                        .append(soLuong)
                        .append(" | Đơn giá: ")
                        .append(donGia)
                        .append(" | Thành tiền: ")
                        .append(thanhTien)
                        .append("\n");
            }
        }

        String html = """
        <!DOCTYPE html>
        <html lang="vi">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <title>Xác nhận đơn giao hàng</title>
        </head>
        <body style="margin:0;padding:0;background:#f4f6fb;font-family:Arial,Helvetica,sans-serif;color:#1f2937;">
          <div style="width:100%%;background:#f4f6fb;padding:32px 16px;">
            <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%%" style="max-width:720px;margin:0 auto;background:#ffffff;border-radius:18px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,0.08);">
              <tr>
                <td style="background:linear-gradient(135deg,#0f172a,#1e3a8a);padding:28px 32px;color:#ffffff;">
                  <div style="font-size:13px;letter-spacing:1px;opacity:0.85;text-transform:uppercase;">VestShop</div>
                  <div style="font-size:26px;font-weight:700;margin-top:8px;">Xác nhận đơn giao hàng</div>
                  <div style="font-size:14px;opacity:0.9;margin-top:8px;">Cảm ơn bạn đã đặt hàng. Chúng tôi đã ghi nhận đơn và đang xử lý giao hàng.</div>
                </td>
              </tr>

              <tr>
                <td style="padding:32px;">
                  <p style="margin:0 0 16px;font-size:15px;line-height:1.7;">Xin chào <strong>%s</strong>,</p>
                  <p style="margin:0 0 20px;font-size:15px;line-height:1.7;color:#374151;">
                    Đơn hàng <strong>#%s</strong> của bạn đã được tạo thành công. Bên dưới là toàn bộ thông tin đặt hàng để bạn tiện theo dõi.
                  </p>

                  <div style="background:#f8fafc;border:1px solid #e5e7eb;border-radius:14px;padding:20px;margin:24px 0;">
                    <div style="font-size:14px;color:#6b7280;margin-bottom:10px;">Thông tin đơn hàng</div>
                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                      <tr><td style="padding:8px 0;color:#6b7280;width:180px;">Mã đơn hàng</td><td style="padding:8px 0;font-weight:600;color:#111827;">%s</td></tr>
                      <tr><td style="padding:8px 0;color:#6b7280;">Thời gian đặt</td><td style="padding:8px 0;font-weight:600;color:#111827;">%s</td></tr>
                      <tr><td style="padding:8px 0;color:#6b7280;">Trạng thái</td><td style="padding:8px 0;font-weight:600;color:#111827;">%s</td></tr>
                      <tr><td style="padding:8px 0;color:#6b7280;">Khách hàng</td><td style="padding:8px 0;font-weight:600;color:#111827;">%s</td></tr>
                      <tr><td style="padding:8px 0;color:#6b7280;">SĐT khách hàng</td><td style="padding:8px 0;font-weight:600;color:#111827;">%s</td></tr>
                      <tr><td style="padding:8px 0;color:#6b7280;">Người nhận</td><td style="padding:8px 0;font-weight:600;color:#111827;">%s</td></tr>
                      <tr><td style="padding:8px 0;color:#6b7280;">SĐT người nhận</td><td style="padding:8px 0;font-weight:600;color:#111827;">%s</td></tr>
                      <tr><td style="padding:8px 0;color:#6b7280;vertical-align:top;">Địa chỉ giao hàng</td><td style="padding:8px 0;font-weight:600;color:#111827;">%s</td></tr>
                      <tr><td style="padding:8px 0;color:#6b7280;vertical-align:top;">Ghi chú</td><td style="padding:8px 0;font-weight:600;color:#111827;">%s</td></tr>
                    </table>
                  </div>

                  <div style="margin:24px 0;">
                    <div style="font-size:16px;font-weight:700;color:#111827;margin-bottom:12px;">Chi tiết sản phẩm</div>
                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="border:1px solid #e5e7eb;border-radius:14px;overflow:hidden;">
                      <tr style="background:#f9fafb;">
                        <th align="left" style="padding:12px 16px;font-size:12px;color:#6b7280;">Sản phẩm</th>
                        <th align="center" style="padding:12px 16px;font-size:12px;color:#6b7280;">SL</th>
                        <th align="right" style="padding:12px 16px;font-size:12px;color:#6b7280;">Đơn giá</th>
                        <th align="right" style="padding:12px 16px;font-size:12px;color:#6b7280;">Thành tiền</th>
                      </tr>
                      %s
                    </table>
                  </div>

                  <div style="background:#111827;color:#ffffff;border-radius:14px;padding:20px;margin-top:24px;">
                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                      <tr><td style="padding:6px 0;opacity:0.82;">Tiền hàng</td><td align="right" style="padding:6px 0;font-weight:600;">%s</td></tr>
                      <tr><td style="padding:6px 0;opacity:0.82;">Giảm giá</td><td align="right" style="padding:6px 0;font-weight:600;">- %s</td></tr>
                      <tr><td style="padding:6px 0;opacity:0.82;">Phí vận chuyển</td><td align="right" style="padding:6px 0;font-weight:600;">%s</td></tr>
                      <tr><td style="padding-top:14px;font-size:18px;font-weight:700;">Tổng thanh toán</td><td align="right" style="padding-top:14px;font-size:18px;font-weight:700;">%s</td></tr>
                    </table>
                  </div>

                 <p style="margin:24px 0 0;font-size:14px;line-height:1.7;color:#4b5563;">
                           Chúng tôi sẽ liên hệ với bạn nếu cần xác nhận thêm thông tin giao hàng.
                         </p>
                
                         <div style="margin:24px 0 8px;text-align:center;">
                           <a href="%s"
                              style="
                                display:inline-block;
                                padding:12px 22px;
                                background:#000f51;
                                color:#ffffff;
                                text-decoration:none;
                                border-radius:10px;
                                font-weight:700;
                                font-size:14px;
                              ">
                              Tra cứu đơn hàng
                           </a>
                         </div>
                
                         <p style="margin:16px 0 0;font-size:14px;line-height:1.7;color:#374151;">
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
                escapeHtml(recipientName),
                escapeHtml(maHoaDon),
                escapeHtml(maHoaDon),
                escapeHtml(createdAt),
                escapeHtml(status),
                escapeHtml(buyerName),
                escapeHtml(buyerPhone),
                escapeHtml(recipientName),
                escapeHtml(receiverPhone),
                escapeHtml(safeText(shippingAddress, "-")),
                escapeHtml(note),
                itemsHtml.toString(),
                escapeHtml(formatMoney(order.getTongTien())),
                escapeHtml(formatMoney(order.getTongTienGiam())),
                escapeHtml(formatMoney(order.getPhiVanChuyen())),
                escapeHtml(formatMoney(order.getTongTienSauGiam())),
                escapeHtml(lookupUrl)
        );

        String plain = "Xin chào " + recipientName + "\n\n"
                + "Đơn giao hàng #" + maHoaDon + " đã được tạo thành công.\n"
                + "Thời gian đặt: " + createdAt + "\n"
                + "Trạng thái: " + status + "\n"
                + "Khách hàng: " + buyerName + "\n"
                + "SĐT khách hàng: " + buyerPhone + "\n"
                + "Người nhận: " + recipientName + "\n"
                + "SĐT người nhận: " + receiverPhone + "\n"
                + "Địa chỉ giao hàng: " + safeText(shippingAddress, "-") + "\n"
                + "Ghi chú: " + note + "\n\n"
                + "Chi tiết sản phẩm:\n"
                + itemsPlain
                + "\nTổng kết thanh toán:\n"
                + "- Tiền hàng: " + formatMoney(order.getTongTien()) + "\n"
                + "- Giảm giá: - " + formatMoney(order.getTongTienGiam()) + "\n"
                + "- Phí vận chuyển: " + formatMoney(order.getPhiVanChuyen()) + "\n"
                + "- Tổng thanh toán: " + formatMoney(order.getTongTienSauGiam()) + "\n\n"
                + "Tra cứu đơn hàng: " + lookupUrl + "\n\n"
                + "Trân trọng,\nVestShop Team";

        sendRichEmail(toEmail.trim(), subject, plain, html);
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.isBlank()) return fallback;
        return value.trim();
    }

    private String joinNonBlank(String... parts) {
        if (parts == null) return "";
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.isBlank()) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(part.trim());
        }
        return sb.toString();
    }

    private String formatMoney(Number value) {
        if (value == null) return "0 ₫";
        return NumberFormat.getInstance(LOCALE_VI).format(value) + " ₫";
    }

    private String formatDate(LocalDateTime value) {
        return value == null ? "-" : value.format(DATE_TIME_FMT);
    }

    private String resolveFromEmail() {
        if (smtpUsername == null || smtpUsername.isBlank()) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Chưa cấu hình SMTP (spring.mail.username). Vui lòng cấu hình MAIL_USERNAME/MAIL_PASSWORD");
        }
        return (from == null || from.isBlank()) ? smtpUsername : from;
    }

    private String buildClientLoginUrl() {
        String base = frontendUrl == null ? "" : frontendUrl.trim();
        if (base.isEmpty()) return "http://localhost:5173/login";
        return base.endsWith("/") ? base + "login" : base + "/login";
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
    public void sendNewKhachHangCredentials(String toEmail, String tenKhachHang, String taiKhoan, String matKhau) {
        if (toEmail == null || toEmail.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email không được để trống để gửi thông tin tài khoản");
        }

        String subject = "[VestShop] Tài khoản khách hàng của bạn đã được tạo";
        String safeName = (tenKhachHang == null || tenKhachHang.isBlank()) ? "bạn" : tenKhachHang.trim();
        String safeUsername = escapeHtml(taiKhoan);
        String safePassword = escapeHtml(matKhau);
        String loginUrl = buildClientLoginUrl();

        String html = """
        <!DOCTYPE html>
        <html lang="vi">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <title>Thông tin tài khoản khách hàng</title>
        </head>
        <body style="margin:0;padding:0;background:#eef2ff;font-family:Arial,Helvetica,sans-serif;color:#1f2937;">
          <div style="width:100%%;background:#eef2ff;padding:32px 16px;">
            <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%%" style="max-width:680px;margin:0 auto;background:#ffffff;border-radius:22px;overflow:hidden;box-shadow:0 18px 42px rgba(15,23,42,0.10);">
              
              <tr>
                <td style="background:linear-gradient(135deg,#000f51 0%%,#1d4ed8 100%%);padding:30px 34px;color:#ffffff;">
                  <div style="font-size:13px;letter-spacing:1.2px;opacity:0.88;text-transform:uppercase;">VestShop</div>
                  <div style="font-size:28px;font-weight:800;margin-top:10px;line-height:1.25;">
                    Tài khoản khách hàng đã được tạo
                  </div>
                  <div style="font-size:14px;opacity:0.92;margin-top:10px;line-height:1.6;">
                    Hệ thống đã tạo tài khoản mua sắm cho bạn. Thông tin đăng nhập nằm ngay bên dưới.
                  </div>
                </td>
              </tr>

              <tr>
                <td style="padding:34px;">
                  <p style="margin:0 0 16px;font-size:15px;line-height:1.8;">
                    Xin chào <strong>%s</strong>,
                  </p>

                  <p style="margin:0 0 20px;font-size:15px;line-height:1.8;color:#374151;">
                    Tài khoản khách hàng của bạn đã được tạo thành công trên hệ thống <strong>VestShop</strong>.
                    Bạn có thể dùng tài khoản này để đăng nhập, theo dõi đơn hàng và mua sắm trực tuyến.
                  </p>

                  <div style="border:1px solid #dbeafe;background:linear-gradient(180deg,#f8fbff 0%%,#eef4ff 100%%);border-radius:18px;padding:22px;margin:24px 0;">
                    <div style="font-size:14px;color:#64748b;margin-bottom:14px;font-weight:600;">
                      Thông tin đăng nhập
                    </div>

                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="border-collapse:collapse;">
                      <tr>
                        <td style="padding:12px 0;border-bottom:1px solid #dbeafe;width:150px;font-size:14px;color:#64748b;">
                          Tài khoản
                        </td>
                        <td style="padding:12px 0;border-bottom:1px solid #dbeafe;font-size:16px;font-weight:700;color:#0f172a;">
                          %s
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:12px 0;font-size:14px;color:#64748b;">
                          Mật khẩu
                        </td>
                        <td style="padding:12px 0;font-size:16px;font-weight:700;color:#dc2626;letter-spacing:0.4px;">
                          %s
                        </td>
                      </tr>
                    </table>
                  </div>

                  <div style="margin:24px 0 22px;text-align:center;">
                    <a href="%s" style="display:inline-block;padding:14px 28px;background:linear-gradient(135deg,#2563eb 0%%,#1d4ed8 100%%);color:#ffffff;text-decoration:none;font-weight:700;font-size:15px;border-radius:14px;box-shadow:0 10px 24px rgba(37,99,235,0.24);">
                      Đăng nhập ngay
                    </a>
                  </div>

                  <div style="background:#fff7ed;border:1px solid #fdba74;color:#9a3412;border-radius:14px;padding:16px 18px;margin:20px 0;font-size:14px;line-height:1.7;">
                    <strong>Lưu ý bảo mật:</strong> Vì đây là mật khẩu được hệ thống tạo tự động, bạn nên đổi mật khẩu sau lần đăng nhập đầu tiên để bảo vệ tài khoản tốt hơn.
                  </div>

                  <div style="background:#f8fafc;border:1px solid #e5e7eb;border-radius:14px;padding:16px 18px;margin:20px 0;font-size:14px;line-height:1.7;color:#475569;">
                    <div style="font-weight:700;color:#0f172a;margin-bottom:6px;">Đường dẫn đăng nhập</div>
                    <div>%s</div>
                  </div>

                  <p style="margin:20px 0 0;font-size:14px;line-height:1.8;color:#4b5563;">
                    Nếu bạn không yêu cầu tạo tài khoản này, vui lòng liên hệ cửa hàng để được hỗ trợ.
                  </p>

                  <p style="margin:24px 0 0;font-size:14px;line-height:1.8;color:#374151;">
                    Trân trọng,<br/>
                    <strong>VestShop Team</strong>
                  </p>
                </td>
              </tr>

              <tr>
                <td style="padding:18px 28px;background:#f8fafc;border-top:1px solid #e5e7eb;font-size:12px;line-height:1.7;color:#6b7280;text-align:center;">
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
                safePassword,
                escapeHtml(loginUrl),
                escapeHtml(loginUrl)
        );

        String plain = "Xin chào " + safeName + "\n\n"
                + "Tài khoản khách hàng của bạn đã được tạo trên hệ thống VestShop.\n\n"
                + "Thông tin đăng nhập:\n"
                + "- Tài khoản: " + taiKhoan + "\n"
                + "- Mật khẩu: " + matKhau + "\n\n"
                + "Đăng nhập tại: " + loginUrl + "\n\n"
                + "Khuyến nghị: Hãy đổi mật khẩu sau lần đăng nhập đầu tiên.\n\n"
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
