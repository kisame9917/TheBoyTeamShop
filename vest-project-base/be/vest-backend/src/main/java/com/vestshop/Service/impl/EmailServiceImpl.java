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
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.from-name:VestShop}")
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
            // nếu pct là BigDecimal thì toString() sẽ ok; nếu muốn 0.0 thì xử lý thêm
            return pct.toString() + "%";
        }

        // false = giảm tiền
        return formatMoney(pgg.getGiaTriTienMat());
    }

    private void sendPlainText(String to, String subject, String body) throws Exception {
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
        helper.setText(body, false); // text/plain
        mailSender.send(message);
    }

    @Override
    public void sendPersonalVoucherAssignedEmail(KhachHang kh, PhieuGiamGia pgg, String maPhieuCaNhan) {
        String to = getTo(kh);
        String name = getName(kh);

        String subject = "[TheBoyTeam] Bạn nhận được phiếu giảm giá: " +
                (pgg.getTenGiamGia() == null ? "" : pgg.getTenGiamGia());

        String body =
                "Xin chào " + name + ",\n\n" +
                        "Chào mừng bạn đến với TheBoyTeam.\n" +
                        "Bạn vừa nhận được phiếu giảm giá.\n\n" +
                        "Mã phiếu của bạn: " + maPhieuCaNhan + "\n" +
                        "Ưu đãi: " + renderUuDai(pgg) + "\n" +
                        "Bắt đầu từ: " + (pgg.getNgayBatDau() == null ? "-" : pgg.getNgayBatDau().format(FMT)) + "\n" +
                        "Hạn sử dụng đến: " + (pgg.getNgayKetThuc() == null ? "-" : pgg.getNgayKetThuc().format(FMT)) + "\n\n" +
                        "Cảm ơn bạn đã mua sắm tại TheBoyTeam!";

        try {
            sendPlainText(to, subject, body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendPersonalVoucherStartedEmail(KhachHang kh, PhieuGiamGia pgg, String maPhieuCaNhan) {
        String to = getTo(kh);
        String name = getName(kh);

        String subject = "[TheBoyTeam] Phiếu đã bắt đầu áp dụng: " +
                (pgg.getTenGiamGia() == null ? "" : pgg.getTenGiamGia());

        String body =
                "Xin chào " + name + ",\n\n" +
                        "Phiếu giảm giá cá nhân của bạn đã bắt đầu áp dụng.\n\n" +
                        "Mã phiếu của bạn: " + maPhieuCaNhan + "\n" +
                        "Ưu đãi: " + renderUuDai(pgg) + "\n" +
                        "Bắt đầu từ: " + (pgg.getNgayBatDau() == null ? "-" : pgg.getNgayBatDau().format(FMT)) + "\n" +
                        "Hạn sử dụng đến: " + (pgg.getNgayKetThuc() == null ? "-" : pgg.getNgayKetThuc().format(FMT)) + "\n\n" +
                        "Cảm ơn bạn đã mua sắm tại TheBoyTeam!";

        try {
            sendPlainText(to, subject, body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendPersonalVoucherEndedEmail(KhachHang kh, PhieuGiamGia pgg, String maPhieuCaNhan) {
        String to = getTo(kh);
        String name = getName(kh);

        String subject = "[TheBoyTeam] Phiếu đã kết thúc: " +
                (pgg.getTenGiamGia() == null ? "" : pgg.getTenGiamGia());

        String body =
                "Xin chào " + name + ",\n\n" +
                        "Rất xin lỗi bạn, phiếu giảm giá cá nhân của bạn đã kết thúc.\n\n" +
                        "Mã phiếu của bạn: " + maPhieuCaNhan + "\n" +
                        "Ưu đãi: " + renderUuDai(pgg) + "\n" +
                        "Thời điểm kết thúc: " + (pgg.getNgayKetThuc() == null ? "-" : pgg.getNgayKetThuc().format(FMT)) + "\n\n" +
                        "Cảm ơn bạn đã mua sắm tại TheBoyTeam!";

        try {
            sendPlainText(to, subject, body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
