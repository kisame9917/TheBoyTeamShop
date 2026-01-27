package com.vestshop.Service.impl;

import com.vestshop.Entity.KhachHang;
import com.vestshop.Entity.PhieuGiamGia;
import com.vestshop.Service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    // lấy từ application.properties
    @Value("${spring.mail.username}")
    private String fromEmail;

    // bạn có thể đổi tên này tùy ý
    @Value("${app.mail.from-name:VestShop}")
    private String fromName;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPersonalVoucherEmail(KhachHang kh, PhieuGiamGia pgg, String maPhieuCaNhan) {
        try {
            if (kh == null) return;

            String to = kh.getEmail();
            if (to == null || to.isBlank()) return;

            String customerName = (kh.getTenKhachHang() == null || kh.getTenKhachHang().isBlank())
                    ? "Quý khách"
                    : kh.getTenKhachHang();

            String subject = "[VestShop] Bạn nhận được phiếu giảm giá: " + pgg.getTenGiamGia();

            String loaiGiam = Boolean.TRUE.equals(pgg.getLoaiGiam()) ? "Giảm %" : "Giảm tiền";
            String giaTri = Boolean.TRUE.equals(pgg.getLoaiGiam())
                    ? (pgg.getGiaTriPhanTram() + "% (tối đa " + pgg.getGiaTriGiamToiDa() + "đ)")
                    : (pgg.getGiaTriTienMat() + "đ");

            DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            String start = (pgg.getNgayBatDau() == null) ? "-" : pgg.getNgayBatDau().format(f);
            String end = (pgg.getNgayKetThuc() == null) ? "-" : pgg.getNgayKetThuc().format(f);

            String html = """
                <div style="font-family: Arial, sans-serif; line-height: 1.6">
                  <h3>Xin chào %s,</h3>
                  <p>Bạn vừa nhận được <b>phiếu giảm giá cá nhân</b> từ VestShop.</p>

                  <ul>
                    <li><b>Tên phiếu:</b> %s</li>
                    <li><b>Mã phiếu cá nhân:</b> %s</li>
                    <li><b>Loại giảm:</b> %s</li>
                    <li><b>Giá trị:</b> %s</li>
                    <li><b>Áp dụng từ:</b> %s</li>
                    <li><b>Đến:</b> %s</li>
                  </ul>

                  <p>Cảm ơn bạn đã mua sắm tại VestShop!</p>
                </div>
                """.formatted(customerName, pgg.getTenGiamGia(), maPhieuCaNhan, loaiGiam, giaTri, start, end);

            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mime,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            // ✅ đổi tên hiển thị
            helper.setFrom(fromEmail, fromName);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(mime);

        } catch (Exception e) {
            // Không làm fail luồng tạo phiếu vì mail lỗi
            System.out.println("Send mail failed: " + e.getMessage());
        }
    }
}
