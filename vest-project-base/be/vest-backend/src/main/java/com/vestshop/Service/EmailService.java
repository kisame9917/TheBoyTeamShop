package com.vestshop.Service;

import com.vestshop.Entity.KhachHang;
import com.vestshop.Entity.PhieuGiamGia;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public interface  EmailService {
    void sendPersonalVoucherAssignedEmail(KhachHang kh, PhieuGiamGia pgg, String maPhieuCaNhan);
    void sendPersonalVoucherStartedEmail(KhachHang kh, PhieuGiamGia pgg, String maPhieuCaNhan);
    void sendPersonalVoucherEndedEmail(KhachHang kh, PhieuGiamGia pgg, String maPhieuCaNhan);

}
