package com.vestshop.Service.impl;

import com.vestshop.Entity.KhachHang;
import com.vestshop.Entity.PhieuGiamGia;
import com.vestshop.Service.EmailService;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.from-name:TheBoyTeam}")
    private String fromName;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Locale LOCALE_VI = new Locale("vi", "VN");

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private String getTo(KhachHang kh) {
        if (kh == null) return null;
        return kh.getEmail();
    }

    private String getName(KhachHang kh) {
        if (kh == null) return "Quý khách";
        String s = kh.getTenKhachHang();
        return (s == null || s.isBlank()) ? "Quý khách" : s;
    }

    private String formatMoney(Number v) {
        if (v == null) return "0 ₫";
        return NumberFormat.getInstance(LOCALE_VI).format(v) + " ₫";
    }

    private String renderUuDai(PhieuGiamGia pgg) {
        if (pgg == null) return "-";

        // true = giảm %
        if (Boolean.TRUE.equals(pgg.getLoaiGiam())) {
            Number pct = pgg.getGiaTriPhanTram();
            if (pct == null) return "0%";
            return pct.toString() + "%";
        }

        // false = giảm tiền
        return formatMoney(pgg.getGiaTriTienMat());
    }

    private String fmtDate(LocalDateTime dt) {
        return dt == null ? "-" : dt.format(FMT);
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Gửi email dạng HTML + fallback plain text (chuẩn nhất cho email client)
     */
    private void sendRichEmail(String to, String subject, String plainText, String html) throws Exception {
        if (to == null || to.isBlank()) return;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name()
        );

        helper.setFrom(new InternetAddress(fromEmail, fromName, StandardCharsets.UTF_8.name()));
        helper.setTo(to);
        helper.setSubject(subject);

        // ✅ plain + html
        helper.setText(plainText, html);

        mailSender.send(message);
    }

    /**
     * HTML mail: chỉ gồm Mã giảm giá (to), Ưu đãi, Ngày bắt đầu, Ngày kết thúc. Không CTA.
     */
    private String buildVoucherHtml(
            String title,
            String greetingName,
            String messageLine,
            String maPhieuCaNhan,
            String uuDai,
            String batDau,
            String ketThuc
    ) {
        // preheader (ẩn) giúp email nhìn chuyên nghiệp hơn
        String preheader = esc(messageLine);

        return """
        <!doctype html>
        <html>
        <head>
          <meta charset="utf-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1" />
          <title>%s</title>
        </head>
        <body style="margin:0;padding:0;background:#f5f6f8;">
          <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#f5f6f8;">
            <tr>
              <td align="center" style="padding:24px 12px;">

                <div style="display:none;max-height:0;overflow:hidden;opacity:0;color:transparent;">
                  %s
                </div>

                <table role="presentation" width="600" cellspacing="0" cellpadding="0"
                  style="width:600px;max-width:600px;background:#ffffff;border-radius:16px;overflow:hidden;">

                  <!-- Header -->
                  <tr>
                    <td style="padding:20px 24px;background:#111827;color:#ffffff;">
                      <table role="presentation" width="100%%" cellspacing="0" cellpadding="0">
                        <tr>
                          <td style="font-family:Arial,sans-serif;font-size:18px;font-weight:800;">
                            TheBoyTeam
                          </td>
                          <td align="right" style="font-family:Arial,sans-serif;font-size:12px;color:#cbd5e1;">
                            Voucher dành riêng cho bạn
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>

                  <!-- Body -->
                  <tr>
                    <td style="padding:24px;font-family:Arial,sans-serif;color:#111827;">
                      <div style="font-size:18px;font-weight:900;margin:0 0 6px 0;">%s</div>

                      <div style="font-size:14px;line-height:20px;color:#374151;margin:0 0 14px 0;">
                        Xin chào <b>%s</b> 👋<br/>
                        %s
                      </div>

                      <!-- Voucher card -->
                      <table role="presentation" width="100%%" cellspacing="0" cellpadding="0"
                        style="border:1px dashed #cbd5e1;border-radius:14px;background:#f8fafc;">
                        <tr>
                          <td style="padding:20px;">
                            <div style="font-size:12px;color:#6b7280;margin-bottom:8px;">Mã giảm giá</div>

                            <!-- ✅ MÃ TO -->
                            <div style="
                              font-size:42px;
                              font-weight:1000;
                              letter-spacing:4px;
                              color:#111827;
                              padding:14px 16px;
                              background:#ffffff;
                              border:2px solid #111827;
                              border-radius:14px;
                              text-align:center;
                            ">
                              %s
                            </div>

                            <!-- ✅ Chỉ còn: Ưu đãi + ngày bắt đầu + ngày kết thúc -->
                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="margin-top:14px;">
                              <tr>
                                <td style="font-size:14px;color:#111827;padding:6px 0;">
                                  ✅ Ưu đãi: <b>%s</b>
                                </td>
                              </tr>
                              <tr>
                                <td style="font-size:13px;color:#6b7280;padding:4px 0;">
                                  Bắt đầu: <b style="color:#111827;">%s</b>
                                </td>
                              </tr>
                              <tr>
                                <td style="font-size:13px;color:#6b7280;padding:4px 0;">
                                  Kết thúc: <b style="color:#111827;">%s</b>
                                </td>
                              </tr>
                            </table>

                            
                          </td>
                        </tr>
                      </table>

                    </td>
                  </tr>

                  <!-- Footer -->
                  <tr>
                    <td style="padding:18px 24px;background:#f3f4f6;font-family:Arial,sans-serif;font-size:12px;color:#6b7280;">
                      Bạn nhận email này vì đã đăng ký/đặt hàng tại TheBoyTeam.<br/>
                      © TheBoyTeam
                    </td>
                  </tr>

                </table>
              </td>
            </tr>
          </table>
        </body>
        </html>
        """.formatted(
                esc(title),
                preheader,
                esc(title),
                esc(greetingName),
                esc(messageLine),
                esc(maPhieuCaNhan),
                esc(uuDai),
                esc(batDau),
                esc(ketThuc)
        );
    }

    private String buildVoucherPlain(
            String name,
            String messageLine,
            String maPhieuCaNhan,
            String uuDai,
            String batDau,
            String ketThuc
    ) {
        return "Xin chào " + name + "\n\n" +
                messageLine + "\n\n" +
                "Mã phiếu: " + maPhieuCaNhan + "\n" +
                "Ưu đãi: " + uuDai + "\n" +
                "Bắt đầu: " + batDau + "\n" +
                "Kết thúc: " + ketThuc + "\n\n" +
                "TheBoyTeam";
    }

    // ================== 3 EMAIL (GIỜ GỬI HTML) ==================

    @Override
    public void sendPersonalVoucherAssignedEmail(KhachHang kh, PhieuGiamGia pgg, String maPhieuCaNhan) {
        String to = getTo(kh);
        String name = getName(kh);

        String tenPhieu = (pgg == null || pgg.getTenGiamGia() == null) ? "" : pgg.getTenGiamGia();
        String subject = "[TheBoyTeam] Bạn nhận được phiếu giảm giá: " + tenPhieu;

        String uuDai = renderUuDai(pgg);
        String batDau = (pgg == null) ? "-" : fmtDate(pgg.getNgayBatDau());
        String ketThuc = (pgg == null) ? "-" : fmtDate(pgg.getNgayKetThuc());

        String messageLine = "Bạn vừa nhận được phiếu giảm giá. Vui lòng dùng mã bên dưới khi thanh toán.";
        String title = "🎁 Tặng bạn phiếu giảm giá!";

        String html = buildVoucherHtml(title, name, messageLine, maPhieuCaNhan, uuDai, batDau, ketThuc);
        String plain = buildVoucherPlain(name, messageLine, maPhieuCaNhan, uuDai, batDau, ketThuc);

        try {
            sendRichEmail(to, subject, plain, html);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendPersonalVoucherStartedEmail(KhachHang kh, PhieuGiamGia pgg, String maPhieuCaNhan) {
        String to = getTo(kh);
        String name = getName(kh);

        String tenPhieu = (pgg == null || pgg.getTenGiamGia() == null) ? "" : pgg.getTenGiamGia();
        String subject = "[TheBoyTeam] Phiếu đã bắt đầu áp dụng: " + tenPhieu;

        String uuDai = renderUuDai(pgg);
        String batDau = (pgg == null) ? "-" : fmtDate(pgg.getNgayBatDau());
        String ketThuc = (pgg == null) ? "-" : fmtDate(pgg.getNgayKetThuc());

        String messageLine = "Phiếu giảm giá cá nhân của bạn đã bắt đầu áp dụng. Dùng mã bên dưới khi thanh toán nhé!";
        String title = "✅ Phiếu giảm giá đã bắt đầu!";

        String html = buildVoucherHtml(title, name, messageLine, maPhieuCaNhan, uuDai, batDau, ketThuc);
        String plain = buildVoucherPlain(name, messageLine, maPhieuCaNhan, uuDai, batDau, ketThuc);

        try {
            sendRichEmail(to, subject, plain, html);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendPersonalVoucherEndedEmail(KhachHang kh, PhieuGiamGia pgg, String maPhieuCaNhan) {
        String to = getTo(kh);
        String name = getName(kh);

        String tenPhieu = (pgg == null || pgg.getTenGiamGia() == null) ? "" : pgg.getTenGiamGia();
        String subject = "[TheBoyTeam] Phiếu đã kết thúc: " + tenPhieu;

        String uuDai = renderUuDai(pgg);
        String batDau = (pgg == null) ? "-" : fmtDate(pgg.getNgayBatDau());
        String ketThuc = (pgg == null) ? "-" : fmtDate(pgg.getNgayKetThuc());

        String messageLine = "Rất tiếc, phiếu giảm giá cá nhân của bạn đã hết hạn.";
        String title = "⏳ Phiếu giảm giá đã kết thúc";

        String html = buildVoucherHtml(title, name, messageLine, maPhieuCaNhan, uuDai, batDau, ketThuc);
        String plain = buildVoucherPlain(name, messageLine, maPhieuCaNhan, uuDai, batDau, ketThuc);

        try {
            sendRichEmail(to, subject, plain, html);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
