package com.vestshop.Service.impl;

import com.vestshop.Entity.*;
import com.vestshop.Repository.*;
import com.vestshop.Service.HoaDonService;
import com.vestshop.Service.NotificationRealtimeService;
import com.vestshop.Service.PosRealtimeService;
import com.vestshop.common.TrangThaiDonHang;
import com.vestshop.dto.request.*;
import com.vestshop.dto.response.*;
import com.vestshop.spec.HoaDonSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.vestshop.Service.EmailService;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HoaDonServiceImpl implements HoaDonService {
    private final NhanVienRepository nhanVienRepository;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final AnhChiTietSanPhamRepository anhChiTietSanPhamRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final LichSuThanhToanRepository lichSuThanhToanRepository;
    private final GiaoDichThanhToanRepository giaoDichThanhToanRepository;
    private final KhachHangRepository khachHangRepository;
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    private final PosRealtimeService posRealtimeService;
    private final NotificationRealtimeService notificationRealtimeService;
    private final EmailService emailService;
//    @Override
//    @Transactional
//    public HoaDonDetailResponse createPos(BanHangRequest req) {
//
//        if (req == null || req.getItems() == null || req.getItems().isEmpty()) {
//            throw new IllegalArgumentException("Giỏ hàng trống");
//        }
//
//        // 1) Gom item + validate qty
//        Map<Long, Integer> buyMap = new HashMap<>();
//        for (var it : req.getItems()) {
//            if (it == null || it.getIdSanPhamChiTiet() == null) continue;
//            int qty = it.getSoLuong() == null ? 0 : it.getSoLuong();
//            if (qty <= 0) continue;
//            buyMap.merge(it.getIdSanPhamChiTiet(), qty, Integer::sum);
//        }
//        if (buyMap.isEmpty()) throw new IllegalArgumentException("Giỏ hàng trống");
//
//        // 2) Load SPCT (KHÔNG check tồn kiểu need>stock vì bạn đã trừ kho ở FE)
//        List<SanPhamChiTiet> spcts = sanPhamChiTietRepository.findAllById(buyMap.keySet());
//        if (spcts.size() != buyMap.size()) {
//            throw new IllegalArgumentException("Có sản phẩm chi tiết không tồn tại");
//        }
//
//        // 3) Tính tiền gốc
//        BigDecimal tongTien = BigDecimal.ZERO;
//        for (SanPhamChiTiet spct : spcts) {
//            int sl = buyMap.get(spct.getId());
//            BigDecimal donGia = spct.getDonGia() == null ? BigDecimal.ZERO : spct.getDonGia();
//            tongTien = tongTien.add(donGia.multiply(BigDecimal.valueOf(sl)));
//        }
//
//        // 4) Tính giảm giá
//        BigDecimal tongTienGiam = BigDecimal.ZERO;
//        PhieuGiamGia pgg = null;
//
//        if (req.getIdPhieuGiamGia() != null) {
//            pgg = phieuGiamGiaRepository.findById(req.getIdPhieuGiamGia())
//                    .orElseThrow(() -> new IllegalArgumentException("Phiếu giảm giá không tồn tại"));
//
//            if (Boolean.FALSE.equals(pgg.getTrangThai())) throw new IllegalArgumentException("Voucher không hoạt động");
//
//            Integer slVoucher = pgg.getSoLuong(); // null = không giới hạn (nếu bạn dùng vậy)
//            if (slVoucher != null && slVoucher <= 0) throw new IllegalArgumentException("Voucher đã hết lượt");
//
//            BigDecimal min = pgg.getDonHangToiThieu() == null ? BigDecimal.ZERO : pgg.getDonHangToiThieu();
//            if (tongTien.compareTo(min) < 0) throw new IllegalArgumentException("Chưa đạt đơn tối thiểu để dùng voucher");
//
//            // ✅ Nếu có % thì dùng %
//            BigDecimal pt = pgg.getGiaTriPhanTram(); // BigDecimal
//            if (pt != null && pt.compareTo(BigDecimal.ZERO) > 0) {
//                // pt=0.4 => 40% ; pt=40 => 40%
//                if (pt.compareTo(BigDecimal.ONE) <= 0) tongTienGiam = tongTien.multiply(pt);
//                else tongTienGiam = tongTien.multiply(pt).divide(BigDecimal.valueOf(100));
//
//                BigDecimal cap = pgg.getGiaTriGiamToiDa();
//                if (cap != null && cap.compareTo(BigDecimal.ZERO) > 0 && tongTienGiam.compareTo(cap) > 0) {
//                    tongTienGiam = cap;
//                }
//            } else {
//                // ✅ Không có % thì dùng tiền mặt
//                BigDecimal tm = pgg.getGiaTriTienMat();
//                tongTienGiam = tm == null ? BigDecimal.ZERO : tm;
//            }
//
//            if (tongTienGiam.compareTo(tongTien) > 0) tongTienGiam = tongTien;
//
//            // ✅ trừ lượt voucher 1 lần duy nhất (nếu có quản lý số lượng)
//            if (pgg.getSoLuong() != null) {
//                pgg.setSoLuong(pgg.getSoLuong() - 1);
//                phieuGiamGiaRepository.save(pgg);
//            }
//
//        } else {
//            int percent = req.getGiamThuCongPercent() == null ? 0 : req.getGiamThuCongPercent();
//            percent = Math.max(0, Math.min(100, percent));
//            tongTienGiam = tongTien.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100));
//        }
//
//        BigDecimal tongTienSauGiam = tongTien.subtract(tongTienGiam);
//        if (tongTienSauGiam.compareTo(BigDecimal.ZERO) < 0) tongTienSauGiam = BigDecimal.ZERO;
//
//        // 5) Check thanh toán
//        BigDecimal paid = req.getPaid() == null ? BigDecimal.ZERO : req.getPaid();
//        if (paid.compareTo(tongTienSauGiam) < 0) throw new IllegalArgumentException("Khách thanh toán chưa đủ");
//        BigDecimal tienThua = paid.subtract(tongTienSauGiam);
//        if (tienThua.compareTo(BigDecimal.ZERO) < 0) tienThua = BigDecimal.ZERO;
//
//        // 6) Tạo hóa đơn
//        HoaDon hd = new HoaDon();
//        hd.setMaHoaDon(req.getMaHoaDon() == null ? ("HD" + System.currentTimeMillis()) : req.getMaHoaDon());
//        hd.setLoaiDon(req.getLoaiDon() == null ? false : req.getLoaiDon());
//        hd.setPhiVanChuyen(req.getPhiVanChuyen() == null ? BigDecimal.ZERO : req.getPhiVanChuyen());
//
//        hd.setTrangThaiDon(TrangThaiDonHang.HOAN_THANH.getCode());
//        hd.setTongTien(tongTien);
//        hd.setTongTienGiam(tongTienGiam);
//        hd.setTongTienSauGiam(tongTienSauGiam);
//
//        hd.setTenKhachHang(req.getTenKhachHang());
//        hd.setSoDienThoai(req.getSoDienThoai());
//        hd.setEmailKhachHang(req.getEmailKhachHang());
//        hd.setDiaChiKhachHang(req.getDiaChiKhachHang());
//        hd.setGhiChu(req.getGhiChu());
//
//        hd.setNgayTao(LocalDateTime.now());
//        hd.setTrangThai(true);
//
//        if (req.getIdKhachHang() != null) {
//            hd.setKhachHang(khachHangRepository.findById(req.getIdKhachHang()).orElse(null));
//        }
//        if (pgg != null) hd.setPhieuGiamGia(pgg);
//
//        NhanVien nv = getCurrentNhanVien();
//        hd.setNhanVien(nv);
//        hd.setNguoiTao(nv != null ? nv.getTenNhanVien() : currentUser());
//
//        hd = hoaDonRepository.save(hd);
//
//        // 7) Tạo chi tiết
//        for (SanPhamChiTiet spct : spcts) {
//            int sl = buyMap.get(spct.getId());
//            HoaDonChiTiet ct = new HoaDonChiTiet();
//            ct.setHoaDon(hd);
//            ct.setSanPhamChiTiet(spct);
//            ct.setSoLuong(sl);
//            ct.setNgayTao(LocalDateTime.now());
//            ct.setNguoiTao(nv != null ? nv.getTenNhanVien() : currentUser());
//            ct.setTrangThai(true);
//            hoaDonChiTietRepository.save(ct);
//        }
//
//        // 8) Lịch sử hóa đơn
//        LichSuHoaDon ls = new LichSuHoaDon();
//        ls.setHoaDon(hd);
//        ls.setHanhDong("Tạo đơn tại quầy");
//        ls.setGhiChu("POS checkout");
//        ls.setThoiGian(LocalDateTime.now());
//        ls.setTrangThai(true);
//        lichSuHoaDonRepository.save(ls);
//
//        // 9) Lịch sử thanh toán (để hiện trong chi tiết hóa đơn)
//        LichSuThanhToan payHis = new LichSuThanhToan();
//        payHis.setHoaDon(hd);
//
//        String maGd = (req.getMaGiaoDich() == null || req.getMaGiaoDich().isBlank())
//                ? ("POS-" + hd.getMaHoaDon() + "-" + System.currentTimeMillis())
//                : req.getMaGiaoDich().trim();
//        payHis.setMaGiaoDich(maGd);
//
//        payHis.setSoTien(tongTienSauGiam);
//        payHis.setNgayThanhToan(LocalDateTime.now());
//
//        String hinhThuc = "TIEN_MAT";
//        if (req.getIdPhuongThucThanhToan() != null) {
//            PhuongThucThanhToan pttt = phuongThucThanhToanRepository
//                    .findById(req.getIdPhuongThucThanhToan())
//                    .orElse(null);
//            if (pttt != null && pttt.getTenPhuongThucThanhToan() != null) {
//                hinhThuc = pttt.getTenPhuongThucThanhToan();
//            }
//        }
//        payHis.setHinhThucThanhToan(hinhThuc);
//
//        String note = (req.getGhiChuThanhToan() != null && !req.getGhiChuThanhToan().isBlank())
//                ? req.getGhiChuThanhToan()
//                : (req.getGhiChu() != null ? req.getGhiChu() : "POS checkout");
//        payHis.setGhiChu(note + " | Khách đưa: " + paid + " | Tiền thừa: " + tienThua);
//
//        payHis.setTrangThai(true);
//        lichSuThanhToanRepository.saveAndFlush(payHis);
//
//        return buildDetail(hd);
//    }

    // TODO: nếu bạn có Security/JWT thì thay bằng user hiện tại
    private NhanVien getCurrentNhanVien() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        String key = auth.getName();
        if (key == null || key.isBlank() || "anonymousUser".equalsIgnoreCase(key)) return null;

        return nhanVienRepository.findByTaiKhoan(key)
                .or(() -> nhanVienRepository.findByEmail(key))
                .orElse(null);
    }

    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }

    @Transactional
    @Override
    public TaohoadonResponse createDraft(TaoHoaDonChoXacNhanRequest req) {
        String ma = (req != null && req.getMaHoaDon() != null && !req.getMaHoaDon().trim().isEmpty())
                ? req.getMaHoaDon().trim()
                : ("HD" + System.currentTimeMillis());

        HoaDon hd = new HoaDon();
        hd.setMaHoaDon(ma);
        hd.setLoaiDon(false);
        hd.setPhiVanChuyen(BigDecimal.ZERO);

        // ✅ trạng thái 0
        hd.setTrangThaiDon(0);

        // tổng tiền lúc tạo draft = 0
        hd.setTongTien(BigDecimal.ZERO);
        hd.setTongTienGiam(BigDecimal.ZERO);
        hd.setTongTienSauGiam(BigDecimal.ZERO);

        hd.setNgayTao(LocalDateTime.now());
        NhanVien nv = getCurrentNhanVien();
        hd.setNhanVien(nv);
        hd.setNguoiTao(nv != null ? nv.getTenNhanVien() : currentUser());
        hd.setTrangThai(true);

        hd = hoaDonRepository.save(hd);

        // cai nay cua anh nhe
        posRealtimeService.pushUpsert(buildDetail(hd));

        return new TaohoadonResponse(hd.getId(), hd.getMaHoaDon(), hd.getTrangThaiDon());
    }

    @Override
    @Transactional
    public HoaDonDetailResponse checkoutDraft(Long hoaDonId, BanHangRequest req) {

        HoaDon hd = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new IllegalArgumentException("Hóa đơn không tồn tại"));

        if (hd.getTrangThaiDon() == null || hd.getTrangThaiDon() != 0) {
            throw new IllegalArgumentException("Hóa đơn không ở trạng thái nháp (0)");
        }

        if (req == null || req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng trống");
        }

        // 1) Xóa chi tiết cũ
        hoaDonChiTietRepository.deleteByHoaDonId(hoaDonId);

        // 2) Gom item
        Map<Long, Integer> buyMap = new HashMap<>();
        for (var it : req.getItems()) {
            if (it == null || it.getIdSanPhamChiTiet() == null) continue;
            int qty = it.getSoLuong() == null ? 0 : it.getSoLuong();
            if (qty <= 0) continue;
            buyMap.merge(it.getIdSanPhamChiTiet(), qty, Integer::sum);
        }
        if (buyMap.isEmpty()) throw new IllegalArgumentException("Giỏ hàng trống");

        // 3) Load SPCT
        List<SanPhamChiTiet> spcts = sanPhamChiTietRepository.findAllById(buyMap.keySet());
        if (spcts.size() != buyMap.size()) {
            throw new IllegalArgumentException("Có sản phẩm không tồn tại");
        }

        // 4) Tính tiền gốc
        BigDecimal tongTien = BigDecimal.ZERO;
        for (SanPhamChiTiet spct : spcts) {
            int sl = buyMap.get(spct.getId());
            BigDecimal donGia = spct.getDonGia() == null ? BigDecimal.ZERO : spct.getDonGia();
            tongTien = tongTien.add(donGia.multiply(BigDecimal.valueOf(sl)));
        }

        // 5) Tính giảm giá
        BigDecimal tongTienGiam = BigDecimal.ZERO;

        Long voucherId = (req.getIdPhieuGiamGia() != null) ? req.getIdPhieuGiamGia() : req.getPggId();

        if (voucherId != null) {
            PhieuGiamGia pgg = phieuGiamGiaRepository.findById(voucherId)
                    .orElseThrow(() -> new IllegalArgumentException("PGG không tồn tại"));

            if (Boolean.FALSE.equals(pgg.getTrangThai())) throw new IllegalArgumentException("Voucher đã bị tắt");

            Integer slVoucher = pgg.getSoLuong();
            if (slVoucher != null && slVoucher <= 0) throw new IllegalArgumentException("Voucher đã hết lượt");

            BigDecimal min = pgg.getDonHangToiThieu() == null ? BigDecimal.ZERO : pgg.getDonHangToiThieu();
            if (tongTien.compareTo(min) < 0) throw new IllegalArgumentException("Chưa đạt đơn tối thiểu");

            BigDecimal pt = pgg.getGiaTriPhanTram();
            if (pt != null && pt.compareTo(BigDecimal.ZERO) > 0) {
                if (pt.compareTo(BigDecimal.ONE) <= 0) tongTienGiam = tongTien.multiply(pt);
                else tongTienGiam = tongTien.multiply(pt).divide(BigDecimal.valueOf(100));

                BigDecimal cap = pgg.getGiaTriGiamToiDa();
                if (cap != null && cap.compareTo(BigDecimal.ZERO) > 0 && tongTienGiam.compareTo(cap) > 0) {
                    tongTienGiam = cap;
                }
            } else {
                BigDecimal tm = pgg.getGiaTriTienMat();
                tongTienGiam = tm == null ? BigDecimal.ZERO : tm;
            }

            if (tongTienGiam.compareTo(tongTien) > 0) tongTienGiam = tongTien;

            // trừ lượt voucher 1 lần (nếu có quản lý)
            if (pgg.getSoLuong() != null) {
                pgg.setSoLuong(pgg.getSoLuong() - 1);
                phieuGiamGiaRepository.save(pgg);
            }

            hd.setPhieuGiamGia(pgg);

        } else {
            Integer percent = req.getGiamThuCongPercent();
            int p = percent == null ? 0 : Math.max(0, Math.min(100, percent));
            tongTienGiam = tongTien.multiply(BigDecimal.valueOf(p)).divide(BigDecimal.valueOf(100));
            hd.setPhieuGiamGia(null);
        }

        BigDecimal tongTienSauGiam = tongTien.subtract(tongTienGiam);
        if (tongTienSauGiam.compareTo(BigDecimal.ZERO) < 0) tongTienSauGiam = BigDecimal.ZERO;

        // 6) Check thanh toán
        BigDecimal paid = req.getPaid() == null ? BigDecimal.ZERO : req.getPaid();
        if (paid.compareTo(tongTienSauGiam) < 0) throw new IllegalArgumentException("Khách thanh toán chưa đủ");
        BigDecimal tienThua = paid.subtract(tongTienSauGiam);
        if (tienThua.compareTo(BigDecimal.ZERO) < 0) tienThua = BigDecimal.ZERO;

        // 7) Update thông tin hóa đơn
        boolean isShip = Boolean.TRUE.equals(req.getLoaiDon());
        hd.setLoaiDon(isShip);

        hd.setPhiVanChuyen(req.getPhiVanChuyen() == null ? BigDecimal.ZERO : req.getPhiVanChuyen());

// khách hàng
        if (req.getIdKhachHang() != null) {
            KhachHang kh = khachHangRepository.findById(req.getIdKhachHang())
                    .orElseThrow(() -> new IllegalArgumentException("Khách hàng không tồn tại"));
            hd.setKhachHang(kh);
        } else {
            hd.setKhachHang(null);
        }
// KHÁCH HÀNG = người mua (không bị ship ghi đè)
        hd.setTenKhachHang(hasText(req.getTenKhachHang()) ? req.getTenKhachHang().trim() : null);
        hd.setSoDienThoai(hasText(req.getSoDienThoai()) ? req.getSoDienThoai().trim() : null);

        hd.setEmailKhachHang(hasText(req.getEmailKhachHang()) ? req.getEmailKhachHang().trim() : null);

// ĐỊA CHỈ KHÁCH = địa chỉ người mua (nếu có)
        hd.setDiaChiKhachHang(hasText(req.getDiaChiKhachHang()) ? req.getDiaChiKhachHang().trim() : null);
        if (isShip) {
            hd.setTenNguoiNhanHang(hasText(req.getTenNguoiNhanHang()) ? req.getTenNguoiNhanHang().trim() : null);
            hd.setSoDienThoaiNhanHang(hasText(req.getSoDienThoaiNhanHang()) ? req.getSoDienThoaiNhanHang().trim() : null);

            hd.setTinhThanhNhanHang(hasText(req.getTinhThanhNhanHang()) ? req.getTinhThanhNhanHang().trim() : null);
            hd.setQuanHuyenNhanHang(hasText(req.getQuanHuyenNhanHang()) ? req.getQuanHuyenNhanHang().trim() : null);
            hd.setPhuongXaNhanHang(hasText(req.getPhuongXaNhanHang()) ? req.getPhuongXaNhanHang().trim() : null);

            hd.setDiaChiNhanHangChiTiet(hasText(req.getDiaChiNhanHangChiTiet()) ? req.getDiaChiNhanHangChiTiet().trim() : null);
        } else {
            hd.setTenNguoiNhanHang(null);
            hd.setSoDienThoaiNhanHang(null);
            hd.setTinhThanhNhanHang(null);
            hd.setQuanHuyenNhanHang(null);
            hd.setPhuongXaNhanHang(null);
            hd.setDiaChiNhanHangChiTiet(null);
        }
        hd.setGhiChu(req.getGhiChu());

        hd.setTongTien(tongTien);
        hd.setTongTienGiam(tongTienGiam);
        hd.setTongTienSauGiam(tongTienSauGiam);

        if (isShip) {
            hd.setTrangThaiDon(1); // Đang xử lý
        } else {
            hd.setTrangThaiDon(4); // Hoàn thành
        }
        hd.setNgayCapNhat(LocalDateTime.now());

        NhanVien nv = getCurrentNhanVien();
        hd.setNhanVien(nv);
        hd.setNguoiCapNhat(nv != null ? nv.getTenNhanVien() : currentUser());

        hoaDonRepository.save(hd);

        // 8) Lưu chi tiết mới
        for (SanPhamChiTiet spct : spcts) {
            int sl = buyMap.get(spct.getId());
            HoaDonChiTiet ct = new HoaDonChiTiet();
            ct.setHoaDon(hd);
            ct.setSanPhamChiTiet(spct);
            ct.setSoLuong(sl);
            ct.setNgayTao(LocalDateTime.now());
            ct.setNguoiTao(nv != null ? nv.getTenNhanVien() : currentUser());
            ct.setTrangThai(true);
            hoaDonChiTietRepository.save(ct);
        }

        // 9) Lịch sử hóa đơn
        LichSuHoaDon ls = new LichSuHoaDon();
        ls.setHoaDon(hd);
        ls.setHanhDong("Thanh toán hóa đơn nháp");
        ls.setGhiChu("POS checkout draft");
        ls.setThoiGian(LocalDateTime.now());
        ls.setTrangThai(true);
        lichSuHoaDonRepository.save(ls);
// 10) Lịch sử thanh toán (DB chuẩn: lưu FK id_phuong_thuc_thanh_toan)
        LichSuThanhToan payHis = new LichSuThanhToan();
        payHis.setHoaDon(hd);

// ✅ maGd
        String maGd = (req.getMaGiaoDich() == null || req.getMaGiaoDich().isBlank())
                ? ("POS-" + hd.getMaHoaDon() + "-" + System.currentTimeMillis())
                : req.getMaGiaoDich().trim();

        payHis.setMaGiaoDich(maGd);
        payHis.setSoTien(tongTienSauGiam);
        payHis.setNgayThanhToan(LocalDateTime.now());

// ✅ Ưu tiên lấy theo id FE gửi
        PhuongThucThanhToan pttt = null;
        Long ptttId = req.getIdPhuongThucThanhToan();
        if (ptttId != null && ptttId > 0) {
            pttt = phuongThucThanhToanRepository.findById(ptttId).orElse(null);
        }

// ✅ Nếu FE không gửi id: tự suy ra theo maGd (QR => CK)
        if (pttt == null) {
            boolean isCk = (maGd != null && maGd.toUpperCase().startsWith("QR-"))
                    || (req.getGhiChuThanhToan() != null && req.getGhiChuThanhToan().toUpperCase().contains("QR"));

            if (isCk) {
                // DB bạn: CK có hinh_thuc = 2
                pttt = phuongThucThanhToanRepository.findFirstByHinhThucAndTrangThaiTrue(2).orElse(null);
            } else {
                // DB bạn: TM có hinh_thuc = 1
                pttt = phuongThucThanhToanRepository.findFirstByHinhThucAndTrangThaiTrue(1).orElse(null);
            }
        }

// ✅ SET FK để FE không bị trống "Phương thức"
        if (pttt != null) {
            payHis.setPhuongThucThanhToan(pttt); // ✅ QUAN TRỌNG
            payHis.setHinhThucThanhToan(pttt.getTenPhuongThucThanhToan()); // giữ cột text cũ
        }

        String note = (req.getGhiChuThanhToan() != null && !req.getGhiChuThanhToan().isBlank())
                ? req.getGhiChuThanhToan()
                : (req.getGhiChu() != null ? req.getGhiChu() : "POS checkout");

        payHis.setGhiChu(note);
        payHis.setTrangThai(true);

        lichSuThanhToanRepository.saveAndFlush(payHis);

        HoaDonDetailResponse detail = buildDetail(hd);

        String shippingEmail = hasText(req.getEmailNguoiNhanHang())
                ? req.getEmailNguoiNhanHang().trim()
                : (hasText(req.getEmailKhachHang()) ? req.getEmailKhachHang().trim() : null);

        if (isShip && hasText(shippingEmail)) {
            try {
                String shippingRecipientName = hasText(req.getTenNguoiNhanHang())
                        ? req.getTenNguoiNhanHang().trim()
                        : (hasText(detail.getTenKhachHang()) ? detail.getTenKhachHang().trim() : "Quý khách");

                emailService.sendShippingOrderConfirmation(
                        shippingEmail,
                        shippingRecipientName,
                        detail
                );
            } catch (Exception ex) {
                log.warn("[MAIL] Không gửi được email xác nhận đơn giao hàng {} tới {}: {}",
                        hd.getMaHoaDon(), shippingEmail, ex.getMessage(), ex);
            }
        }

        posRealtimeService.pushRemove(hd.getId());
        notificationRealtimeService.pushToRole(
                "ADMIN",
                NotificationEventResponse.builder()
                        .id(String.valueOf(System.currentTimeMillis()))
                        .title("Hóa đơn " + hd.getMaHoaDon() + " đã thanh toán")
                        .time("Vừa xong")
                        .link("/orders/" + hd.getId())
                        .type("ORDER_CHECKOUT")
                        .createdAt(java.time.OffsetDateTime.now().toString())
                        .build()
        );
        return detail;
    }


    @Override
    @Transactional
    public void cancelDraft(Long hoaDonId, CancelDraftRequest req) {

        HoaDon hd = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new IllegalArgumentException("Hóa đơn không tồn tại"));

        // ✅ chỉ hủy hóa đơn nháp
        if (hd.getTrangThaiDon() == null || hd.getTrangThaiDon() != 0) {
            throw new IllegalArgumentException("Chỉ được hủy hóa đơn nháp (trạng thái 0)");
        }

        // 1) Hoàn tồn kho theo items FE gửi lên
        Map<Long, Integer> plusMap = new HashMap<>();

        if (req != null && req.getItems() != null) {
            for (CancelDraftRequest.Item it : req.getItems()) {
                if (it == null || it.getIdSanPhamChiTiet() == null) continue;
                int qty = it.getSoLuong() == null ? 0 : it.getSoLuong();
                if (qty <= 0) continue;
                plusMap.merge(it.getIdSanPhamChiTiet(), qty, Integer::sum);
            }
        }

        if (!plusMap.isEmpty()) {
            List<SanPhamChiTiet> spcts = sanPhamChiTietRepository.findAllById(plusMap.keySet());
            if (spcts.size() != plusMap.size()) {
                throw new IllegalArgumentException("Có SPCT không tồn tại khi hoàn kho");
            }

            for (SanPhamChiTiet spct : spcts) {
                int add = plusMap.getOrDefault(spct.getId(), 0);
                int current = spct.getSoLuongTon() == null ? 0 : spct.getSoLuongTon();
                spct.setSoLuongTon(current + add);
            }
            sanPhamChiTietRepository.saveAll(spcts);
        }

        // 2) Nếu draft đã từng có chi tiết (phòng khi bạn lưu sớm) thì xóa
        // (cần method deleteByHoaDonId trong HoaDonChiTietRepository)
        hoaDonChiTietRepository.deleteByHoaDonId(hoaDonId);

        // 3) Hủy hóa đơn nháp
        hd.setTrangThaiDon(TrangThaiDonHang.DA_HUY.getCode());
        hd.setTrangThai(false);
        hd.setNgayCapNhat(LocalDateTime.now());
        NhanVien nv = getCurrentNhanVien();
        hd.setNhanVien(nv != null ? nv : hd.getNhanVien());
        hd.setNguoiCapNhat(nv != null ? nv.getTenNhanVien() : currentUser());
        hoaDonRepository.save(hd);

        // 4) Lịch sử hóa đơn
        LichSuHoaDon ls = new LichSuHoaDon();
        ls.setHoaDon(hd);
        ls.setHanhDong("Hủy hóa đơn nháp");
        ls.setGhiChu(req != null && req.getReason() != null ? req.getReason() : "Đóng tab bán hàng");
        ls.setThoiGian(LocalDateTime.now());
        ls.setTrangThai(true);
        lichSuHoaDonRepository.save(ls);
        posRealtimeService.pushRemove(hd.getId());
    }
    @Override
    @Transactional(readOnly = true)
    public Page<HoaDonListResponse> search(
            String keyword,
            Integer trangThaiDon,
            String phanLoai,
            Boolean loaiDon,
            LocalDateTime from,
            LocalDateTime to,
            BigDecimal minTotal,
            BigDecimal maxTotal,
            Boolean hasVoucher,
            Long idNhanVien,
            Boolean active,
            Pageable pageable
    ) {
        // ✅ 1) Ẩn hóa đơn huỷ (trangThai=false) mặc định
        if (active == null) active = true;

        Specification<HoaDon> spec = HoaDonSpecifications.advanced(
                keyword, trangThaiDon, phanLoai, loaiDon, from, to,
                minTotal, maxTotal, hasVoucher, idNhanVien, active
        );

        // ✅ 2) Nếu người dùng KHÔNG lọc trạng thái => ẩn luôn trạng thái "chờ xác nhận" (0)
        // (Nếu user chọn trạng thái=0 thì vẫn xem được)
//        if (trangThaiDon == null) {
//            spec = spec.and((root, query, cb) -> cb.notEqual(root.get("trangThaiDon"), 0));
//            // Nếu muốn ẩn thêm trạng thái khác, dùng:
//            // spec = spec.and((root, query, cb) -> cb.not(root.get("trangThaiDon").in(List.of(0, 1))));
//        }
        if (trangThaiDon == null) {
            spec = spec.and((root, query, cb) -> {
                var isDraftPos = cb.and(
                        cb.equal(root.get("trangThaiDon"), 0),
                        cb.equal(root.get("loaiDon"), false)
                );
                return cb.not(isDraftPos);
            });
        }

        Page<HoaDon> page = hoaDonRepository.findAll(spec, pageable);

        return page.map(hd -> {
            TrangThaiDonHang st = TrangThaiDonHang.fromCode(hd.getTrangThaiDon());
            NhanVien nv = hd.getNhanVien();

            String tenNhanVien;
            String tenChucVu;

            if (Boolean.TRUE.equals(hd.getLoaiDon())) {
                tenNhanVien = "System";
                tenChucVu = "Hệ thống";
            } else {
                tenNhanVien = (nv == null ? null : nv.getTenNhanVien());
                tenChucVu = (nv == null || nv.getQuyenHan() == null)
                        ? null
                        : nv.getQuyenHan().getTenQuyenHan();
            }

            return HoaDonListResponse.builder()
                    .id(hd.getId())
                    .maHoaDon(hd.getMaHoaDon())
                    .trangThai(hd.getTrangThai())
                    .trangThaiDon(hd.getTrangThaiDon())
                    .tenTrangThaiDon(st.getTen())
                    .loaiDon(hd.getLoaiDon())
                    .tongTienSauGiam(hd.getTongTienSauGiam())
                    .tenKhachHang(hd.getTenKhachHang())
                    .soDienThoai(hd.getSoDienThoai())
                    .tenNhanVien(tenNhanVien)
                    .tenChucVu(tenChucVu)
                    .ngayTao(hd.getNgayTao())
                    .build();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public HoaDonDetailResponse getDetailById(Long id) {
        HoaDon hd = hoaDonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoá đơn"));
        return buildDetail(hd);
    }

    @Override
    @Transactional(readOnly = true)
    public HoaDonDetailResponse getDetailByMaHoaDon(String maHoaDon) {
        HoaDon hd = hoaDonRepository.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoá đơn"));
        return buildDetail(hd);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LichSuHoaDonResponse> getLichSuHoaDon(Long idHoaDon) {

        HoaDon hd = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoá đơn"));

        NhanVien nv = hd.getNhanVien();
        String maNv = nv == null ? null : nv.getMaNhanVien();
        String tenNv = nv == null ? null : nv.getTenNhanVien();

        return lichSuHoaDonRepository.findAllByHoaDon_IdOrderByThoiGianDesc(idHoaDon)
                .stream()
                .map(x -> LichSuHoaDonResponse.builder()
                        .id(x.getId())
                        .hanhDong(x.getHanhDong())
                        .ghiChu(x.getGhiChu())
                        .thoiGian(x.getThoiGian())
                        .maNhanVien(maNv)
                        .tenNhanVien(tenNv)
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LichSuThanhToanResponse> getLichSuThanhToan(Long idHoaDon) {
        return lichSuThanhToanRepository.findAllByHoaDonIdFetchPTTT(idHoaDon)
                .stream()
                .map(x -> {
                    PhuongThucThanhToan pttt = x.getPhuongThucThanhToan();

                    return LichSuThanhToanResponse.builder()
                            .id(x.getId())
                            .maGiaoDich(x.getMaGiaoDich())
                            .soTien(x.getSoTien())
                            .ngayThanhToan(x.getNgayThanhToan())

                            .idPhuongThucThanhToan(pttt != null ? pttt.getId() : null)
                            .tenPhuongThucThanhToan(pttt != null ? pttt.getTenPhuongThucThanhToan() : null)
                            .hinhThuc(pttt != null ? pttt.getHinhThuc() : null)

                            .ghiChu(x.getGhiChu())
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GiaoDichThanhToanResponse> getGiaoDichThanhToan(Long idHoaDon) {
        return giaoDichThanhToanRepository.findAllByHoaDon_IdOrderByThoiGianTaoDesc(idHoaDon)
                .stream()
                .map(x -> GiaoDichThanhToanResponse.builder()
                        .id(x.getId())
                        .idPhuongThucThanhToan(x.getPhuongThucThanhToan() == null ? null : x.getPhuongThucThanhToan().getId())
                        .tenPhuongThucThanhToan(x.getPhuongThucThanhToan() == null ? null : x.getPhuongThucThanhToan().getTenPhuongThucThanhToan())
                        .soTien(x.getSoTien())
                        .maGiaoDich(x.getMaGiaoDich())
                        .maYeuCau(x.getMaYeuCau())
                        .maGiaoDichNgoai(x.getMaGiaoDichNgoai())
                        .maThamChieu(x.getMaThamChieu())
                        .duLieuQr(x.getDuLieuQr())
                        .thoiHan(x.getThoiHan())
                        .thoiGianTao(x.getThoiGianTao())
                        .thoiGianCapNhat(x.getThoiGianCapNhat())
                        .ghiChu(x.getGhiChu())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HoaDonDetailResponse changeStatus(Long idHoaDon, HoaDonChangeStatusRequest req) {
        if (req == null || req.getTrangThaiDon() == null) {
            throw new IllegalArgumentException("Thiếu trangThaiDon");
        }

        HoaDon hd = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoá đơn"));

        TrangThaiDonHang newSt = TrangThaiDonHang.fromCode(req.getTrangThaiDon());
        TrangThaiDonHang oldSt = TrangThaiDonHang.fromCode(hd.getTrangThaiDon());

        if (oldSt == TrangThaiDonHang.DA_HUY) {
            throw new IllegalArgumentException("Đơn đã huỷ, không thể đổi trạng thái");
        }
        if (oldSt == TrangThaiDonHang.DA_HOAN) {
            throw new IllegalArgumentException("Đơn đã hoàn, không thể đổi trạng thái");
        }

        // CHỈ áp dụng cho đơn online
        if (Boolean.TRUE.equals(hd.getLoaiDon())) {
            // online: chỉ khi sang ĐANG_GIAO mới trừ kho
            if (newSt == TrangThaiDonHang.DANG_GIAO && oldSt != TrangThaiDonHang.DANG_GIAO) {
                truTonKhoKhiDangGiao(hd);
            }

            // online: nếu đã giao/đã trừ kho rồi mà hủy thì hoàn kho
            if (newSt == TrangThaiDonHang.DA_HUY && daTruTonKho(oldSt)) {
                hoanTonKho(hd);
            }
        }

        hd.setTrangThaiDon(newSt.getCode());
        hd.setNgayCapNhat(LocalDateTime.now());

        NhanVien nv = getCurrentNhanVien();
        hd.setNhanVien(nv != null ? nv : hd.getNhanVien());
        hd.setNguoiCapNhat(nv != null ? nv.getTenNhanVien() : currentUser());

        hoaDonRepository.save(hd);

        if (isOnlineCodNeedPaymentHistory(hd, newSt)) {
            createCodPaymentHistoryIfNeeded(hd);
        }

        LichSuHoaDon ls = new LichSuHoaDon();
        ls.setHoaDon(hd);
        ls.setHanhDong("Cập nhật trạng thái: " + oldSt.getTen() + " -> " + newSt.getTen());
        ls.setGhiChu(req.getGhiChu());
        ls.setThoiGian(LocalDateTime.now());
        ls.setTrangThai(true);
        lichSuHoaDonRepository.save(ls);

        return buildDetail(hd);
    }
    @Override
    @Transactional
    public HoaDonDetailResponse hoanHang(Long idHoaDon, HoaDonReturnRequest req) {
        HoaDon hd = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoá đơn"));

        TrangThaiDonHang stNow = TrangThaiDonHang.fromCode(hd.getTrangThaiDon());
        if (stNow == TrangThaiDonHang.DA_HUY || stNow == TrangThaiDonHang.DA_HOAN) {
            throw new IllegalArgumentException("Đơn đã huỷ/đã hoàn, không thể hoàn hàng");
        }

        boolean isOnline = Boolean.TRUE.equals(hd.getLoaiDon());

        // Chỉ hoàn kho khi:
        // - Đơn tại quầy: luôn hoàn kho
        // - Đơn online: chỉ hoàn nếu trước đó đã ở trạng thái đã trừ kho
        boolean needRestock = !isOnline || daTruTonKho(stNow);

        if (needRestock) {
            List<HoaDonChiTiet> cts = hoaDonChiTietRepository.findAllByHoaDon_Id(idHoaDon);

            Map<Long, Integer> plus = new HashMap<>();
            for (HoaDonChiTiet ct : cts) {
                if (ct.getSanPhamChiTiet() == null) continue;
                plus.merge(ct.getSanPhamChiTiet().getId(), ct.getSoLuong(), Integer::sum);
            }

            if (!plus.isEmpty()) {
                List<SanPhamChiTiet> spcts = sanPhamChiTietRepository.findAllById(plus.keySet());
                for (SanPhamChiTiet spct : spcts) {
                    Integer add = plus.getOrDefault(spct.getId(), 0);
                    Integer current = spct.getSoLuongTon() == null ? 0 : spct.getSoLuongTon();
                    spct.setSoLuongTon(current + add);
                    spct.setNgayCapNhat(LocalDateTime.now());
                }
                sanPhamChiTietRepository.saveAll(spcts);
            }
        }

        hd.setTrangThaiDon(TrangThaiDonHang.DA_HOAN.getCode());
        hd.setNgayCapNhat(LocalDateTime.now());

        NhanVien nv = getCurrentNhanVien();
        hd.setNhanVien(nv != null ? nv : hd.getNhanVien());
        hd.setNguoiCapNhat(nv != null ? nv.getTenNhanVien() : currentUser());
        hoaDonRepository.save(hd);

        LichSuHoaDon ls = new LichSuHoaDon();
        ls.setHoaDon(hd);
        ls.setHanhDong("Hoàn hàng");
        ls.setGhiChu(req == null ? null : req.getLyDo());
        ls.setThoiGian(LocalDateTime.now());
        ls.setTrangThai(true);
        lichSuHoaDonRepository.save(ls);

        return buildDetail(hd);
    }

    private void createCodPaymentHistoryIfNeeded(HoaDon hd) {
        if (hd == null || hd.getId() == null) return;

        if (lichSuThanhToanRepository.existsByHoaDon_Id(hd.getId())) {
            return;
        }

        PhuongThucThanhToan pttt = phuongThucThanhToanRepository
                .findFirstByHinhThucAndTrangThaiTrue(1)
                .orElse(null);

        LichSuThanhToan payHis = new LichSuThanhToan();
        payHis.setHoaDon(hd);
        payHis.setMaGiaoDich(null);
        payHis.setSoTien(hd.getTongTienSauGiam() == null ? BigDecimal.ZERO : hd.getTongTienSauGiam());
        payHis.setNgayThanhToan(LocalDateTime.now());

        if (pttt != null) {
            payHis.setPhuongThucThanhToan(pttt);
            payHis.setHinhThucThanhToan(pttt.getTenPhuongThucThanhToan());
        } else {
            payHis.setHinhThucThanhToan("COD");
        }

        payHis.setGhiChu("Thanh toán khi nhận hàng");
        payHis.setTrangThai(true);

        lichSuThanhToanRepository.save(payHis);
    }

    private boolean isOnlineCodNeedPaymentHistory(HoaDon hd, TrangThaiDonHang newSt) {
        if (hd == null || newSt == null) return false;
        return Boolean.TRUE.equals(hd.getLoaiDon())
                && newSt == TrangThaiDonHang.HOAN_THANH;
    }

    private HoaDonDetailResponse buildDetail(HoaDon hd) {
        List<HoaDonChiTiet> cts = hoaDonChiTietRepository.findAllByHoaDon_Id(hd.getId());

        List<HoaDonDetailResponse.Item> items = cts.stream().map(ct -> {
            SanPhamChiTiet spct = ct.getSanPhamChiTiet();

            BigDecimal donGia = (spct != null && spct.getDonGia() != null)
                    ? spct.getDonGia()
                    : BigDecimal.ZERO;

            Integer sl = ct.getSoLuong() == null ? 0 : ct.getSoLuong();
            BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(sl));

            String maSpct = spct == null ? null : spct.getMaSanPhamChiTiet();
            String tenSp = (spct != null && spct.getSanPham() != null) ? spct.getSanPham().getTenSanPham() : null;
            String mau = (spct != null && spct.getMauSac() != null) ? spct.getMauSac().getTen() : null;
            String size = (spct != null && spct.getKichCo() != null) ? spct.getKichCo().getSoSize() : null;

            String anh = null;
            if (spct != null) {
                anh = anhChiTietSanPhamRepository
                        .findTop1BySanPhamChiTiet_IdAndTrangThaiTrueOrderByThuTuHienThiAscIdAsc(spct.getId())
                        .map(img -> img.getMediaAsset() != null && img.getMediaAsset().getSecureUrl() != null ? img.getMediaAsset().getSecureUrl() : img.getTen())
                        .orElse(spct.getMediaPrimary() != null && spct.getMediaPrimary().getSecureUrl() != null ? spct.getMediaPrimary().getSecureUrl() : spct.getAnh());
            }

            return HoaDonDetailResponse.Item.builder()
                    .idSanPhamChiTiet(spct == null ? null : spct.getId())
                    .maSanPhamChiTiet(maSpct)
                    .tenSanPham(tenSp)
                    .mauSac(mau)
                    .kichCo(size)
                    .soLuong(sl)
                    .donGia(donGia)
                    .thanhTien(thanhTien)
                    .anhDaiDien(anh)
                    .build();
        }).collect(Collectors.toList());

        TrangThaiDonHang st = TrangThaiDonHang.fromCode(hd.getTrangThaiDon());
        NhanVien nv = hd.getNhanVien();

        String maNhanVien;
        String tenNhanVien;

        if (Boolean.TRUE.equals(hd.getLoaiDon())) {
            maNhanVien = "SYSTEM";
            tenNhanVien = "System";
        } else {
            maNhanVien = (nv == null ? null : nv.getMaNhanVien());
            tenNhanVien = (nv == null ? null : nv.getTenNhanVien());
        }

        return HoaDonDetailResponse.builder()
                .id(hd.getId())
                .maHoaDon(hd.getMaHoaDon())
                .idKhachHang(hd.getKhachHang() == null ? null : hd.getKhachHang().getId())
                .idNhanVien(nv == null ? null : nv.getId())
                .maNhanVien(maNhanVien)
                .tenNhanVien(tenNhanVien)
                .idPhieuGiamGia(hd.getPhieuGiamGia() == null ? null : hd.getPhieuGiamGia().getId())
                .trangThaiDon(hd.getTrangThaiDon())
                .tenTrangThaiDon(st.getTen())
                .loaiDon(hd.getLoaiDon())
                .phiVanChuyen(hd.getPhiVanChuyen())
                .tongTien(hd.getTongTien())
                .tongTienGiam(hd.getTongTienGiam())
                .tongTienSauGiam(hd.getTongTienSauGiam())
                .tenKhachHang(hd.getTenKhachHang())
                .soDienThoai(hd.getSoDienThoai())
                .diaChiKhachHang(hd.getDiaChiKhachHang())
                .emailKhachHang(hd.getEmailKhachHang())
                .qrCode(hd.getQrCode())
                .ghiChu(hd.getGhiChu())
                .trangThai(hd.getTrangThai())
                .ngayTao(hd.getNgayTao())
                .ngayCapNhat(hd.getNgayCapNhat())
                .items(items)
                .lichSuHoaDon(getLichSuHoaDon(hd.getId()))
                .lichSuThanhToan(getLichSuThanhToan(hd.getId()))
                .giaoDichThanhToan(getGiaoDichThanhToan(hd.getId()))
                .tenNguoiNhanHang(hd.getTenNguoiNhanHang())
                .soDienThoaiNhanHang(hd.getSoDienThoaiNhanHang())
                .tinhThanhNhanHang(hd.getTinhThanhNhanHang())
                .quanHuyenNhanHang(hd.getQuanHuyenNhanHang())
                .phuongXaNhanHang(hd.getPhuongXaNhanHang())
                .diaChiNhanHangChiTiet(hd.getDiaChiNhanHangChiTiet())
                .build();
    }
    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private String joinAddress(String... parts) {
        if (parts == null) return "";
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p == null) continue;
            String x = p.trim();
            if (x.isEmpty()) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(x);
        }
        return sb.toString();
    }
    @Override
    @Transactional(readOnly = true)
    public List<HoaDonDetailResponse> getPosDrafts() {
        return hoaDonRepository.findAll().stream()
                .filter(hd -> hd.getTrangThaiDon() != null && hd.getTrangThaiDon() == 0)
                .filter(HoaDon::getTrangThai)
                .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao()))
                .map(this::buildDetail)
                .toList();
    }
    @Override
    @Transactional
    public HoaDonDetailResponse syncPosDraft(Long hoaDonId, PosDraftSyncRequest req) {
        HoaDon hd = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new IllegalArgumentException("Hóa đơn không tồn tại"));

        if (hd.getTrangThaiDon() == null || hd.getTrangThaiDon() != 0) {
            throw new IllegalArgumentException("Chỉ sync hóa đơn nháp");
        }

        hoaDonChiTietRepository.deleteByHoaDonId(hoaDonId);

        if (req.getItems() == null || req.getItems().isEmpty()) {
            hd.setTongTien(BigDecimal.ZERO);
            hd.setTongTienGiam(BigDecimal.ZERO);
            hd.setTongTienSauGiam(BigDecimal.ZERO);
            hd.setPhiVanChuyen(BigDecimal.ZERO);
            hoaDonRepository.save(hd);

            HoaDonDetailResponse res = buildDetail(hd);
            posRealtimeService.pushUpsert(res);
            return res;
        }

        Map<Long, Integer> buyMap = new HashMap<>();
        for (PosDraftSyncRequest.Item it : req.getItems()) {
            if (it == null || it.getIdSanPhamChiTiet() == null) continue;
            int qty = it.getSoLuong() == null ? 0 : it.getSoLuong();
            if (qty <= 0) continue;
            buyMap.merge(it.getIdSanPhamChiTiet(), qty, Integer::sum);
        }

        List<SanPhamChiTiet> spcts = sanPhamChiTietRepository.findAllById(buyMap.keySet());
        if (spcts.size() != buyMap.size()) {
            throw new IllegalArgumentException("Có sản phẩm không tồn tại");
        }

        BigDecimal tongTien = BigDecimal.ZERO;
        for (SanPhamChiTiet spct : spcts) {
            int sl = buyMap.get(spct.getId());
            BigDecimal donGia = spct.getDonGia() == null ? BigDecimal.ZERO : spct.getDonGia();
            tongTien = tongTien.add(donGia.multiply(BigDecimal.valueOf(sl)));
        }

        BigDecimal tongTienGiam = BigDecimal.ZERO;
        Long voucherId = req.getIdPhieuGiamGia() != null ? req.getIdPhieuGiamGia() : req.getPggId();

        if (voucherId != null) {
            PhieuGiamGia pgg = phieuGiamGiaRepository.findById(voucherId).orElse(null);
            if (pgg != null && !Boolean.FALSE.equals(pgg.getTrangThai())) {
                BigDecimal min = pgg.getDonHangToiThieu() == null ? BigDecimal.ZERO : pgg.getDonHangToiThieu();
                if (tongTien.compareTo(min) >= 0) {
                    BigDecimal pt = pgg.getGiaTriPhanTram();
                    if (pt != null && pt.compareTo(BigDecimal.ZERO) > 0) {
                        if (pt.compareTo(BigDecimal.ONE) <= 0) tongTienGiam = tongTien.multiply(pt);
                        else tongTienGiam = tongTien.multiply(pt).divide(BigDecimal.valueOf(100));

                        BigDecimal cap = pgg.getGiaTriGiamToiDa();
                        if (cap != null && cap.compareTo(BigDecimal.ZERO) > 0 && tongTienGiam.compareTo(cap) > 0) {
                            tongTienGiam = cap;
                        }
                    } else {
                        BigDecimal tm = pgg.getGiaTriTienMat();
                        tongTienGiam = tm == null ? BigDecimal.ZERO : tm;
                    }
                    hd.setPhieuGiamGia(pgg);
                }
            }
        } else {
            int p = req.getGiamThuCongPercent() == null ? 0 : Math.max(0, Math.min(100, req.getGiamThuCongPercent()));
            tongTienGiam = tongTien.multiply(BigDecimal.valueOf(p)).divide(BigDecimal.valueOf(100));
            hd.setPhieuGiamGia(null);
        }

        if (tongTienGiam.compareTo(tongTien) > 0) tongTienGiam = tongTien;
        BigDecimal tongTienSauGiam = tongTien.subtract(tongTienGiam);
        if (tongTienSauGiam.compareTo(BigDecimal.ZERO) < 0) tongTienSauGiam = BigDecimal.ZERO;

        hd.setLoaiDon(Boolean.TRUE.equals(req.getLoaiDon()));
        hd.setPhiVanChuyen(req.getPhiVanChuyen() == null ? BigDecimal.ZERO : req.getPhiVanChuyen());

        hd.setTenKhachHang(req.getTenKhachHang());
        hd.setSoDienThoai(req.getSoDienThoai());
        hd.setEmailKhachHang(req.getEmailKhachHang());
        hd.setDiaChiKhachHang(req.getDiaChiKhachHang());
        hd.setGhiChu(req.getGhiChu());

        hd.setTenNguoiNhanHang(req.getTenNguoiNhanHang());
        hd.setSoDienThoaiNhanHang(req.getSoDienThoaiNhanHang());
        hd.setTinhThanhNhanHang(req.getTinhThanhNhanHang());
        hd.setQuanHuyenNhanHang(req.getQuanHuyenNhanHang());
        hd.setPhuongXaNhanHang(req.getPhuongXaNhanHang());
        hd.setDiaChiNhanHangChiTiet(req.getDiaChiNhanHangChiTiet());

        hd.setTongTien(tongTien);
        hd.setTongTienGiam(tongTienGiam);
        hd.setTongTienSauGiam(tongTienSauGiam);
        hd.setNgayCapNhat(LocalDateTime.now());

        hoaDonRepository.save(hd);

        for (SanPhamChiTiet spct : spcts) {
            HoaDonChiTiet ct = new HoaDonChiTiet();
            ct.setHoaDon(hd);
            ct.setSanPhamChiTiet(spct);
            ct.setSoLuong(buyMap.get(spct.getId()));
            ct.setNgayTao(LocalDateTime.now());
            ct.setNguoiTao("system");
            ct.setTrangThai(true);
            hoaDonChiTietRepository.save(ct);
        }

        HoaDonDetailResponse res = buildDetail(hd);
        posRealtimeService.pushUpsert(res);
        return res;
    }
    private void truTonKhoKhiDangGiao(HoaDon hd) {
        List<HoaDonChiTiet> cts = hoaDonChiTietRepository.findAllByHoaDon_Id(hd.getId());

        if (cts == null || cts.isEmpty()) {
            throw new IllegalArgumentException("Hóa đơn chưa có sản phẩm");
        }

        for (HoaDonChiTiet ct : cts) {
            SanPhamChiTiet spct = ct.getSanPhamChiTiet();
            if (spct == null || spct.getId() == null) {
                throw new IllegalArgumentException("Chi tiết hóa đơn không hợp lệ");
            }

            SanPhamChiTiet currentSpct = sanPhamChiTietRepository.findById(spct.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy sản phẩm chi tiết id = " + spct.getId()
                    ));

            int tonKho = currentSpct.getSoLuongTon() == null ? 0 : currentSpct.getSoLuongTon();
            int soLuongDat = ct.getSoLuong() == null ? 0 : ct.getSoLuong();

            if (soLuongDat <= 0) {
                throw new IllegalArgumentException("Số lượng sản phẩm không hợp lệ");
            }

            if (tonKho < soLuongDat) {
                throw new IllegalArgumentException(
                        "Sản phẩm không đủ tồn kho để giao: " + currentSpct.getMaSanPhamChiTiet()
                );
            }

            currentSpct.setSoLuongTon(tonKho - soLuongDat);
            currentSpct.setNgayCapNhat(LocalDateTime.now());
            sanPhamChiTietRepository.save(currentSpct);
        }
    }

    private void hoanTonKho(HoaDon hd) {
        List<HoaDonChiTiet> cts = hoaDonChiTietRepository.findAllByHoaDon_Id(hd.getId());

        for (HoaDonChiTiet ct : cts) {
            SanPhamChiTiet spct = ct.getSanPhamChiTiet();
            if (spct == null || spct.getId() == null) continue;

            SanPhamChiTiet currentSpct = sanPhamChiTietRepository.findById(spct.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy sản phẩm chi tiết id = " + spct.getId()
                    ));

            int tonKho = currentSpct.getSoLuongTon() == null ? 0 : currentSpct.getSoLuongTon();
            int soLuong = ct.getSoLuong() == null ? 0 : ct.getSoLuong();

            currentSpct.setSoLuongTon(tonKho + soLuong);
            currentSpct.setNgayCapNhat(LocalDateTime.now());
            sanPhamChiTietRepository.save(currentSpct);
        }
    }

    private boolean daTruTonKho(TrangThaiDonHang st) {
        return st == TrangThaiDonHang.DANG_GIAO
                || st == TrangThaiDonHang.DA_GIAO
                || st == TrangThaiDonHang.HOAN_THANH;
    }

}
