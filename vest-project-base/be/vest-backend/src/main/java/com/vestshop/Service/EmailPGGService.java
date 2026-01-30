package com.vestshop.Service;

import com.vestshop.Entity.KhachHang;
import com.vestshop.Entity.PhieuGiamGia;
import org.springframework.stereotype.Service;

@Service
public interface EmailPGGService {
    void sendPersonalVoucherAssignedEmail(KhachHang kh, PhieuGiamGia pgg, String maPhieuCaNhan);
    void sendPersonalVoucherStartedEmail(KhachHang kh, PhieuGiamGia pgg, String maPhieuCaNhan);
    void sendPersonalVoucherEndedEmail(KhachHang kh, PhieuGiamGia pgg, String maPhieuCaNhan);
    void sendPersonalVoucherUpdatedEmail(KhachHang kh, PhieuGiamGia oldPgg, PhieuGiamGia newPgg, String maPhieuCaNhan);

}
