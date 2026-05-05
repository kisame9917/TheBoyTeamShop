package com.vestshop.Service.impl;

import com.vestshop.Entity.*;
import com.vestshop.Repository.*;
import com.vestshop.Service.EmailService;
import com.vestshop.Service.HoaDonService;
import com.vestshop.Service.NotificationRealtimeService;
import com.vestshop.Service.PosRealtimeService;
import com.vestshop.common.TrangThaiDonHang;
import com.vestshop.dto.request.*;
import com.vestshop.dto.response.*;
import com.vestshop.spec.HoaDonSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public HoaDonDetailResponse confirmRefund(Long hoaDonId, RefundConfirmRequest request) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn"));

        if (hoaDon.getTrangThaiDon() == null || hoaDon.getTrangThaiDon() != TrangThaiDonHang.DA_HUY.getCode()) {
            throw new IllegalArgumentException("Chỉ được xác nhận hoàn tiền cho đơn đã hủy");
        }

        BigDecimal paidAmount = getPaidAmount(hoaDon);
        if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Đơn hàng chưa có thanh toán để hoàn");
        }

        BigDecimal refundAmount = (request != null && request.getSoTienHoan() != null)
                ? request.getSoTienHoan()
                : paidAmount;

        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền hoàn phải lớn hơn 0");
        }

        if (refundAmount.compareTo(paidAmount) > 0) {
            throw new IllegalArgumentException("Số tiền hoàn không được lớn hơn số tiền đã thanh toán");
        }

        createRefundPaymentHistory(
                hoaDon,
                refundAmount,
                request != null ? request.getGhiChu() : null
        );

        hoaDon.setTrangThaiDon(TrangThaiDonHang.DA_HOAN.getCode());
        hoaDon.setNgayCapNhat(LocalDateTime.now());

        NhanVien nv = getCurrentNhanVien();
        hoaDon.setNhanVien(nv != null ? nv : hoaDon.getNhanVien());
        hoaDon.setNguoiCapNhat(nv != null ? nv.getTenNhanVien() : currentUser());

        hoaDonRepository.save(hoaDon);

        LichSuHoaDon ls = new LichSuHoaDon();
        ls.setHoaDon(hoaDon);
        ls.setHanhDong("XAC_NHAN_HOAN_TIEN");
        ls.setGhiChu(
                (request != null && request.getGhiChu() != null && !request.getGhiChu().isBlank())
                        ? request.getGhiChu()
                        : ("Xác nhận hoàn tiền " + refundAmount + " cho đơn hàng " + hoaDon.getMaHoaDon())
        );
        ls.setThoiGian(LocalDateTime.now());
        ls.setTrangThai(true);
        attachHistoryNhanVien(ls, nv);
        lichSuHoaDonRepository.save(ls);

        return buildDetail(hoaDon);
    }

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

    private void attachHistoryNhanVien(LichSuHoaDon lichSu, NhanVien nhanVien) {
        if (lichSu == null) return;
        lichSu.setNhanVien(nhanVien);
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
        hd.setTrangThaiDon(0);
        hd.setTongTien(BigDecimal.ZERO);
        hd.setTongTienGiam(BigDecimal.ZERO);
        hd.setTongTienSauGiam(BigDecimal.ZERO);
        hd.setNgayTao(LocalDateTime.now());

        NhanVien nv = getCurrentNhanVien();
        hd.setNhanVien(nv);
        hd.setNguoiTao(nv != null ? nv.getTenNhanVien() : currentUser());
        hd.setTrangThai(true);

        hd = hoaDonRepository.save(hd);
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

        hoaDonChiTietRepository.deleteByHoaDonId(hoaDonId);

        Map<Long, Integer> buyMap = new HashMap<>();
        for (var it : req.getItems()) {
            if (it == null || it.getIdSanPhamChiTiet() == null) continue;
            int qty = it.getSoLuong() == null ? 0 : it.getSoLuong();
            if (qty <= 0) continue;
            buyMap.merge(it.getIdSanPhamChiTiet(), qty, Integer::sum);
        }
        if (buyMap.isEmpty()) throw new IllegalArgumentException("Giỏ hàng trống");

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
        Long voucherId = (req.getIdPhieuGiamGia() != null) ? req.getIdPhieuGiamGia() : req.getPggId();

        if (voucherId != null) {
            PhieuGiamGia pgg = phieuGiamGiaRepository.findById(voucherId)
                    .orElseThrow(() -> new IllegalArgumentException("PGG không tồn tại"));

            if (Boolean.FALSE.equals(pgg.getTrangThai())) throw new IllegalArgumentException("Voucher đã bị tắt");

            LocalDateTime now = LocalDateTime.now();
            if (pgg.getNgayBatDau() != null && now.isBefore(pgg.getNgayBatDau())) {
                throw new IllegalArgumentException("Voucher chưa tới thời gian áp dụng");
            }
            if (pgg.getNgayKetThuc() != null && now.isAfter(pgg.getNgayKetThuc())) {
                throw new IllegalArgumentException("Voucher đã hết hạn");
            }

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

            hd.setPhieuGiamGia(pgg);

        } else {
            Integer percent = req.getGiamThuCongPercent();
            int p = percent == null ? 0 : Math.max(0, Math.min(100, percent));
            tongTienGiam = tongTien.multiply(BigDecimal.valueOf(p)).divide(BigDecimal.valueOf(100));
            hd.setPhieuGiamGia(null);
        }
        BigDecimal tongTienSauGiam = tongTien.subtract(tongTienGiam);
        if (tongTienSauGiam.compareTo(BigDecimal.ZERO) < 0) tongTienSauGiam = BigDecimal.ZERO;

        boolean isShip = Boolean.TRUE.equals(req.getLoaiDon());
        hd.setLoaiDon(isShip);
        hd.setPhiVanChuyen(req.getPhiVanChuyen() == null ? BigDecimal.ZERO : req.getPhiVanChuyen());

        BigDecimal tongCanThu = tongTienSauGiam.add(
                isShip
                        ? (req.getPhiVanChuyen() == null ? BigDecimal.ZERO : req.getPhiVanChuyen())
                        : BigDecimal.ZERO
        );

        BigDecimal paid = req.getPaid() == null ? BigDecimal.ZERO : req.getPaid();
        if (paid.compareTo(tongCanThu) < 0) {
            throw new IllegalArgumentException("Khách thanh toán chưa đủ");
        }
        BigDecimal tienThua = paid.subtract(tongCanThu);
        if (tienThua.compareTo(BigDecimal.ZERO) < 0) tienThua = BigDecimal.ZERO;

        if (req.getIdKhachHang() != null) {
            KhachHang kh = khachHangRepository.findById(req.getIdKhachHang())
                    .orElseThrow(() -> new IllegalArgumentException("Khách hàng không tồn tại"));
            hd.setKhachHang(kh);
        } else {
            hd.setKhachHang(null);
        }

        hd.setTenKhachHang(hasText(req.getTenKhachHang()) ? req.getTenKhachHang().trim() : null);
        hd.setSoDienThoai(hasText(req.getSoDienThoai()) ? req.getSoDienThoai().trim() : null);
        hd.setEmailKhachHang(hasText(req.getEmailKhachHang()) ? req.getEmailKhachHang().trim() : null);
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
            hd.setTrangThaiDon(8); // DA_XAC_NHAN
        } else {
            hd.setTrangThaiDon(4); // HOAN_THANH
        }
        hd.setNgayCapNhat(LocalDateTime.now());

        NhanVien nv = getCurrentNhanVien();
        hd.setNhanVien(nv);
        hd.setNguoiCapNhat(nv != null ? nv.getTenNhanVien() : currentUser());

        log.info("CHECKOUT_DRAFT hoaDonId={}, isShip={}, trangThaiDon={}",
                hoaDonId, isShip, hd.getTrangThaiDon());

        hoaDonRepository.saveAndFlush(hd);

        if (shouldConsumeVoucherOnCheckout(hd)) {
            consumeVoucherIfNeeded(hd);
        }

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

        LichSuHoaDon ls = new LichSuHoaDon();
        ls.setHoaDon(hd);
        ls.setHanhDong(
                isShip
                        ? TrangThaiDonHang.DA_XAC_NHAN.name()
                        : TrangThaiDonHang.HOAN_THANH.name()
        );
        ls.setGhiChu(
                isShip
                        ? "POS checkout - đơn giao hàng đã xác nhận"
                        : "POS checkout - hoàn thành tại quầy"
        );
        ls.setThoiGian(LocalDateTime.now());
        ls.setTrangThai(true);
        attachHistoryNhanVien(ls, nv);
        lichSuHoaDonRepository.save(ls);

        LichSuThanhToan payHis = new LichSuThanhToan();
        payHis.setHoaDon(hd);

        String maGd = (req.getMaGiaoDich() == null || req.getMaGiaoDich().isBlank())
                ? ("POS-" + hd.getMaHoaDon() + "-" + System.currentTimeMillis())
                : req.getMaGiaoDich().trim();

        payHis.setMaGiaoDich(maGd);
        payHis.setSoTien(tongTienSauGiam);
        payHis.setNgayThanhToan(LocalDateTime.now());

        PhuongThucThanhToan pttt = null;
        Long ptttId = req.getIdPhuongThucThanhToan();
        if (ptttId != null && ptttId > 0) {
            pttt = phuongThucThanhToanRepository.findById(ptttId).orElse(null);
        }

        if (pttt == null) {
            String upperMaGd = maGd == null ? "" : maGd.toUpperCase();
            String upperNote = req.getGhiChuThanhToan() == null ? "" : req.getGhiChuThanhToan().toUpperCase();

            boolean isCk =
                    upperMaGd.startsWith("QR-")
                            || upperMaGd.startsWith("VNPAY-")
                            || upperMaGd.contains("BANK")
                            || upperMaGd.contains("CK")
                            || upperNote.contains("QR")
                            || upperNote.contains("VNPAY")
                            || upperNote.contains("CHUYEN KHOAN")
                            || upperNote.contains("CHUYỂN KHOẢN");

            if (isCk) {
                pttt = phuongThucThanhToanRepository
                        .findFirstByHinhThucAndTrangThaiTrue(2)
                        .orElse(null);
            } else {
                pttt = phuongThucThanhToanRepository
                        .findFirstByHinhThucAndTrangThaiTrue(1)
                        .orElse(null);
            }
        }

        if (pttt != null) {
            payHis.setPhuongThucThanhToan(pttt);
            payHis.setHinhThucThanhToan(pttt.getTenPhuongThucThanhToan());
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

        if (hd.getTrangThaiDon() == null || hd.getTrangThaiDon() != 0) {
            throw new IllegalArgumentException("Chỉ được hủy hóa đơn nháp (trạng thái 0)");
        }

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

        hoaDonChiTietRepository.deleteByHoaDonId(hoaDonId);

        hd.setTrangThaiDon(TrangThaiDonHang.DA_HUY.getCode());
        hd.setTrangThai(false);
        hd.setNgayCapNhat(LocalDateTime.now());

        NhanVien nv = getCurrentNhanVien();
        hd.setNhanVien(nv != null ? nv : hd.getNhanVien());
        hd.setNguoiCapNhat(nv != null ? nv.getTenNhanVien() : currentUser());
        hoaDonRepository.save(hd);

        LichSuHoaDon ls = new LichSuHoaDon();
        ls.setHoaDon(hd);
        ls.setHanhDong("Hủy hóa đơn nháp");
        ls.setGhiChu(req != null && req.getReason() != null ? req.getReason() : "Đóng tab bán hàng");
        ls.setThoiGian(LocalDateTime.now());
        ls.setTrangThai(true);
        attachHistoryNhanVien(ls, nv);
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
        if (active == null) active = true;

        Specification<HoaDon> spec = HoaDonSpecifications.advanced(
                keyword, trangThaiDon, phanLoai, loaiDon, from, to,
                minTotal, maxTotal, hasVoucher, idNhanVien, active
        );

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
            Integer effectiveStatus = resolveEffectiveTrangThaiDon(hd);
            TrangThaiDonHang st = TrangThaiDonHang.fromCode(effectiveStatus);
            NhanVien nv = hd.getNhanVien();

            String maNhanVien;
            String tenNhanVien;
            String tenChucVu;

            if (nv == null) {
                maNhanVien = Boolean.TRUE.equals(hd.getLoaiDon()) ? "SYSTEM" : "-";
                tenNhanVien = Boolean.TRUE.equals(hd.getLoaiDon()) ? "System" : "-";
                tenChucVu = Boolean.TRUE.equals(hd.getLoaiDon()) ? "Hệ thống" : "-";
            } else {
                maNhanVien = resolveMaNhanVien(nv);
                tenNhanVien = nv.getTenNhanVien() != null && !nv.getTenNhanVien().isBlank()
                        ? nv.getTenNhanVien()
                        : "-";

                tenChucVu = nv.getQuyenHan() != null && nv.getQuyenHan().getTenQuyenHan() != null
                        ? nv.getQuyenHan().getTenQuyenHan()
                        : "-";
            }

            log.info(
                    "[ORDER_LIST_NV] maHoaDon={}, nvId={}, maNhanVien={}, tenNhanVien={}",
                    hd.getMaHoaDon(),
                    nv != null ? nv.getId() : null,
                    maNhanVien,
                    tenNhanVien
            );

            return HoaDonListResponse.builder()
                    .id(hd.getId())
                    .maHoaDon(hd.getMaHoaDon())
                    .trangThai(hd.getTrangThai())
                    .trangThaiDon(effectiveStatus)
                    .tenTrangThaiDon(st != null ? st.getTen() : null)
                    .loaiDon(hd.getLoaiDon())
                    .tongTienSauGiam(hd.getTongTienSauGiam())
                    .tenKhachHang(hd.getTenKhachHang())
                    .soDienThoai(hd.getSoDienThoai())
                    .maNhanVien(maNhanVien)
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
        hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoá đơn"));

        return lichSuHoaDonRepository.findAllByHoaDon_IdOrderByThoiGianDesc(idHoaDon)
                .stream()
                .map(x -> {
                    NhanVien nv = x.getNhanVien();
                    return LichSuHoaDonResponse.builder()
                            .id(x.getId())
                            .hanhDong(x.getHanhDong())
                            .ghiChu(x.getGhiChu())
                            .thoiGian(x.getThoiGian())
                            .maNhanVien(nv == null ? null : nv.getMaNhanVien())
                            .tenNhanVien(nv == null ? null : nv.getTenNhanVien())
                            .build();
                })
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

        if (newSt == null) {
            throw new IllegalArgumentException("Trạng thái mới không hợp lệ");
        }
        if (oldSt == null) {
            throw new IllegalArgumentException("Trạng thái hiện tại không hợp lệ");
        }

        if (oldSt == TrangThaiDonHang.DA_HOAN) {
            throw new IllegalArgumentException("Đơn đã hoàn tiền, không thể đổi trạng thái");
        }

        if (oldSt == TrangThaiDonHang.DA_HUY && newSt != TrangThaiDonHang.DA_HOAN) {
            throw new IllegalArgumentException("Đơn đã huỷ, chỉ có thể xác nhận hoàn tiền");
        }
        if (oldSt == TrangThaiDonHang.YEU_CAU_HUY
                && newSt != TrangThaiDonHang.DA_HUY
                && newSt != TrangThaiDonHang.CHO_XAC_NHAN) {
            throw new IllegalArgumentException("Yêu cầu hủy chỉ có thể xác nhận hủy hoặc từ chối hủy");
        }

        if (shouldConsumeVoucherOnStatusTransition(hd, oldSt, newSt)) {
            consumeVoucherIfNeeded(hd);
        }

        if (shouldRollbackVoucherOnStatusTransition(hd, oldSt, newSt)) {
            rollbackVoucherIfNeeded(hd);
        }

        if (newSt == TrangThaiDonHang.DA_XAC_NHAN
                && oldSt != TrangThaiDonHang.DA_XAC_NHAN) {
            truTonKhoKhiXacNhan(hd);
        }

        if (Boolean.TRUE.equals(hd.getLoaiDon())) {
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
        ls.setHanhDong(newSt.name());
        ls.setGhiChu(
                req.getGhiChu() != null && !req.getGhiChu().isBlank()
                        ? req.getGhiChu()
                        : ("Cập nhật trạng thái: " + oldSt.getTen() + " -> " + newSt.getTen())
        );
        ls.setThoiGian(LocalDateTime.now());
        ls.setTrangThai(true);
        attachHistoryNhanVien(ls, nv);
        lichSuHoaDonRepository.save(ls);

        pushThongBaoTrangThaiDonHangToCustomer(hd, newSt);

        return buildDetail(hd);
    }

    @Override
    @Transactional
    public HoaDonDetailResponse hoanHang(Long idHoaDon, HoaDonReturnRequest req) {
        throw new IllegalArgumentException(
                "Chức năng hoàn đơn đã tắt. Vui lòng huỷ đơn rồi xác nhận hoàn tiền."
        );
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


    private void truTonKhoKhiXacNhan(HoaDon hd) {
        List<HoaDonChiTiet> cts = hoaDonChiTietRepository.findAllByHoaDon_Id(hd.getId());

        if (cts == null || cts.isEmpty()) {
            throw new IllegalArgumentException("Hóa đơn chưa có sản phẩm");
        }

        List<String> errors = new ArrayList<>();

        for (HoaDonChiTiet ct : cts) {
            SanPhamChiTiet spct = ct.getSanPhamChiTiet();
            if (spct == null || spct.getId() == null) continue;

            int soLuongDat = ct.getSoLuong() == null ? 0 : ct.getSoLuong();
            if (soLuongDat <= 0) {
                throw new IllegalArgumentException("Số lượng sản phẩm không hợp lệ");
            }

            int updated = sanPhamChiTietRepository.decreaseStock(spct.getId(), soLuongDat);

            if (updated == 0) {
                SanPhamChiTiet currentSpct = sanPhamChiTietRepository.findById(spct.getId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Không tìm thấy sản phẩm chi tiết id = " + spct.getId()
                        ));

                int tonKho = currentSpct.getSoLuongTon() == null ? 0 : currentSpct.getSoLuongTon();
                String maSp = currentSpct.getMaSanPhamChiTiet();

                errors.add(maSp + " đã hết hoặc không đủ tồn (còn " + tonKho + ", cần " + soLuongDat + ")");
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Không đủ sản phẩm trong kho: " + String.join("; ", errors));
        }
    }

    private BigDecimal getPaidAmount(HoaDon hd) {
        if (hd == null || hd.getId() == null) return BigDecimal.ZERO;

        return lichSuThanhToanRepository.findAllByHoaDonIdFetchPTTT(hd.getId())
                .stream()
                .filter(x -> Boolean.TRUE.equals(x.getTrangThai()))
                .map(x -> x.getSoTien() == null ? BigDecimal.ZERO : x.getSoTien())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void createRefundPaymentHistory(HoaDon hd, BigDecimal refundAmount, String note) {
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) return;

        List<LichSuThanhToan> histories = lichSuThanhToanRepository.findAllByHoaDonIdFetchPTTT(hd.getId());

        LichSuThanhToan lastPositivePayment = histories.stream()
                .filter(x -> Boolean.TRUE.equals(x.getTrangThai()))
                .filter(x -> x.getSoTien() != null && x.getSoTien().compareTo(BigDecimal.ZERO) > 0)
                .max(Comparator.comparing(LichSuThanhToan::getNgayThanhToan))
                .orElse(null);

        LichSuThanhToan refund = new LichSuThanhToan();
        refund.setHoaDon(hd);
        refund.setMaGiaoDich("REFUND-" + hd.getMaHoaDon() + "-" + System.currentTimeMillis());
        refund.setSoTien(refundAmount.negate());
        refund.setNgayThanhToan(LocalDateTime.now());

        if (lastPositivePayment != null) {
            refund.setPhuongThucThanhToan(lastPositivePayment.getPhuongThucThanhToan());
            refund.setHinhThucThanhToan(lastPositivePayment.getHinhThucThanhToan());
        } else {
            refund.setHinhThucThanhToan("HOAN_TIEN");
        }

        refund.setGhiChu(note);
        refund.setTrangThai(true);

        lichSuThanhToanRepository.save(refund);
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
                        .map(img -> img.getMediaAsset() != null && img.getMediaAsset().getSecureUrl() != null
                                ? img.getMediaAsset().getSecureUrl()
                                : img.getTen())
                        .orElse(
                                spct.getMediaPrimary() != null && spct.getMediaPrimary().getSecureUrl() != null
                                        ? spct.getMediaPrimary().getSecureUrl()
                                        : spct.getAnh()
                        );
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

        Integer effectiveStatus = resolveEffectiveTrangThaiDon(hd);
        TrangThaiDonHang st = TrangThaiDonHang.fromCode(effectiveStatus);
        NhanVien nv = hd.getNhanVien();

        String maNhanVien =
                (nv == null ? (Boolean.TRUE.equals(hd.getLoaiDon()) ? "SYSTEM" : null)
                        : nv.getMaNhanVien());

        String tenNhanVien =
                (nv == null ? (Boolean.TRUE.equals(hd.getLoaiDon()) ? "System" : null)
                        : nv.getTenNhanVien());

        return HoaDonDetailResponse.builder()
                .id(hd.getId())
                .maHoaDon(hd.getMaHoaDon())
                .idKhachHang(hd.getKhachHang() == null ? null : hd.getKhachHang().getId())
                .idNhanVien(nv == null ? null : nv.getId())
                .maNhanVien(maNhanVien)
                .tenNhanVien(tenNhanVien)
                .idPhieuGiamGia(hd.getPhieuGiamGia() == null ? null : hd.getPhieuGiamGia().getId())
                .trangThaiDon(effectiveStatus)
                .tenTrangThaiDon(st != null ? st.getTen() : null)
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
            hd.setPhieuGiamGia(null);
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
        hd.setPhieuGiamGia(null);

        if (voucherId != null) {
            PhieuGiamGia pgg = phieuGiamGiaRepository.findById(voucherId).orElse(null);
            LocalDateTime now = LocalDateTime.now();
            if (pgg != null
                    && !Boolean.FALSE.equals(pgg.getTrangThai())
                    && (pgg.getNgayBatDau() == null || !now.isBefore(pgg.getNgayBatDau()))
                    && (pgg.getNgayKetThuc() == null || !now.isAfter(pgg.getNgayKetThuc()))
                    && (pgg.getSoLuong() == null || pgg.getSoLuong() > 0)) {
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

    private boolean shouldConsumeVoucherOnCheckout(HoaDon hd) {
        if (hd == null || hd.getPhieuGiamGia() == null) return false;

        if (Boolean.TRUE.equals(hd.getLoaiDon())) {
            return Objects.equals(
                    hd.getTrangThaiDon(),
                    TrangThaiDonHang.DA_XAC_NHAN.getCode()
            );
        }

        return Objects.equals(
                hd.getTrangThaiDon(),
                TrangThaiDonHang.HOAN_THANH.getCode()
        );
    }

    private boolean shouldConsumeVoucherOnStatusTransition(HoaDon hd, TrangThaiDonHang oldSt, TrangThaiDonHang newSt) {
        if (hd == null || hd.getPhieuGiamGia() == null) return false;
        if (!Boolean.TRUE.equals(hd.getLoaiDon())) return false;
        return oldSt == TrangThaiDonHang.CHO_XAC_NHAN
                && newSt == TrangThaiDonHang.DA_XAC_NHAN;
    }

    private boolean shouldRollbackVoucherOnStatusTransition(HoaDon hd, TrangThaiDonHang oldSt, TrangThaiDonHang newSt) {
        if (hd == null || hd.getPhieuGiamGia() == null) return false;
        return newSt == TrangThaiDonHang.DA_HUY && wasVoucherConsumed(hd, oldSt);
    }

    private boolean wasVoucherConsumed(HoaDon hd, TrangThaiDonHang st) {
        if (hd == null || hd.getPhieuGiamGia() == null || st == null) return false;

        if (Boolean.TRUE.equals(hd.getLoaiDon())) {
            return st == TrangThaiDonHang.DA_XAC_NHAN
                    || st == TrangThaiDonHang.DANG_GIAO
                    || st == TrangThaiDonHang.DA_GIAO
                    || st == TrangThaiDonHang.HOAN_THANH;
        }

        return st == TrangThaiDonHang.HOAN_THANH;
    }

    private void consumeVoucherIfNeeded(HoaDon hd) {
        if (hd == null || hd.getPhieuGiamGia() == null || hd.getPhieuGiamGia().getId() == null) return;

        PhieuGiamGia pgg = phieuGiamGiaRepository.findById(hd.getPhieuGiamGia().getId())
                .orElseThrow(() -> new IllegalArgumentException("Voucher không tồn tại"));

        Integer soLuong = pgg.getSoLuong();
        if (soLuong == null) return;
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Voucher đã hết lượt");
        }

        pgg.setSoLuong(soLuong - 1);
        pgg.setNgayCapNhat(LocalDateTime.now());
        phieuGiamGiaRepository.save(pgg);
    }

    private void rollbackVoucherIfNeeded(HoaDon hd) {
        if (hd == null || hd.getPhieuGiamGia() == null || hd.getPhieuGiamGia().getId() == null) return;

        PhieuGiamGia pgg = phieuGiamGiaRepository.findById(hd.getPhieuGiamGia().getId())
                .orElseThrow(() -> new IllegalArgumentException("Voucher không tồn tại"));

        Integer soLuong = pgg.getSoLuong();
        pgg.setSoLuong(soLuong == null ? 1 : soLuong + 1);
        pgg.setNgayCapNhat(LocalDateTime.now());
        phieuGiamGiaRepository.save(pgg);
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
        return st == TrangThaiDonHang.DA_XAC_NHAN
                || st == TrangThaiDonHang.DANG_GIAO
                || st == TrangThaiDonHang.DA_GIAO
                || st == TrangThaiDonHang.HOAN_THANH;
    }
    private Integer mapHistoryActionToStatusCode(String hanhDong) {
        if (hanhDong == null || hanhDong.isBlank()) return null;

        return switch (hanhDong) {
            case "CHO_XAC_NHAN" -> TrangThaiDonHang.CHO_XAC_NHAN.getCode();
            case "DA_XAC_NHAN", "XAC_NHAN_DON" -> TrangThaiDonHang.DA_XAC_NHAN.getCode();
            case "DANG_XU_LY" -> TrangThaiDonHang.DANG_XU_LY.getCode();
            case "DANG_GIAO" -> TrangThaiDonHang.DANG_GIAO.getCode();
            case "DA_GIAO" -> TrangThaiDonHang.DA_GIAO.getCode();
            case "HOAN_THANH" -> TrangThaiDonHang.HOAN_THANH.getCode();
            case "DA_HUY" -> TrangThaiDonHang.DA_HUY.getCode();
            case "YEU_CAU_HUY", "KHACH_HANG_YEU_CAU_HUY_DON" -> TrangThaiDonHang.YEU_CAU_HUY.getCode();
            case "DA_HOAN", "XAC_NHAN_HOAN_TIEN" -> TrangThaiDonHang.DA_HOAN.getCode();
            default -> null;
        };
    }

    private Integer resolveEffectiveTrangThaiDon(HoaDon hd) {
        if (hd == null || hd.getId() == null) return null;

        Integer raw = hd.getTrangThaiDon();

        Optional<LichSuHoaDon> latestHistory =
                lichSuHoaDonRepository.findTopByHoaDon_IdAndTrangThaiTrueOrderByThoiGianDesc(hd.getId());

        Integer fromHistory = latestHistory
                .map(LichSuHoaDon::getHanhDong)
                .map(this::mapHistoryActionToStatusCode)
                .orElse(null);

        return fromHistory != null ? fromHistory : raw;
    }

    private String resolveMaNhanVien(NhanVien nv) {
        if (nv == null) return "-";

        if (nv.getMaNhanVien() != null && !nv.getMaNhanVien().isBlank()) {
            return nv.getMaNhanVien();
        }

        if (nv.getId() != null) {
            return "NV" + String.format("%03d", nv.getId());
        }

        return "-";
    }
    private void pushThongBaoTrangThaiDonHangToCustomer(HoaDon hd, TrangThaiDonHang newSt) {
        if (hd == null) return;
        if (newSt == null) return;
        if (hd.getKhachHang() == null) return;
        if (hd.getKhachHang().getId() == null) return;

        notificationRealtimeService.pushToUser(
                hd.getKhachHang().getId(),
                NotificationEventResponse.builder()
                        .id("ORDER_STATUS_" + hd.getId() + "_" + System.currentTimeMillis())
                        .title("Đơn hàng " + hd.getMaHoaDon() + " đã chuyển sang trạng thái: " + newSt.getTen())
                        .time("Vừa xong")
                        .link("/my-orders")
                        .type("ORDER_STATUS")
                        .createdAt(java.time.OffsetDateTime.now().toString())
                        .build()
        );
    }
}