package com.vestshop.Service.impl;

import com.vestshop.Entity.*;
import com.vestshop.Repository.*;
import com.vestshop.Service.HoaDonService;
import com.vestshop.common.TrangThaiDonHang;
import com.vestshop.dto.request.BanHangRequest;
import com.vestshop.dto.request.HoaDonChangeStatusRequest;
import com.vestshop.dto.request.HoaDonReturnRequest;
import com.vestshop.dto.request.TaoHoaDonChoXacNhanRequest;
import com.vestshop.dto.response.*;
import com.vestshop.spec.HoaDonSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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


    @Override
    @Transactional
    public HoaDonDetailResponse createPos(BanHangRequest req) {

        // 1) Load SPCT + check tồn kho
        Map<Long, Integer> buyMap = new HashMap<>();
        for (var it : req.getItems()) {
            buyMap.merge(it.getIdSanPhamChiTiet(), it.getSoLuong(), Integer::sum);
        }

        List<SanPhamChiTiet> spcts = sanPhamChiTietRepository.findAllById(buyMap.keySet());
        if (spcts.size() != buyMap.size()) {
            throw new IllegalArgumentException("Có sản phẩm chi tiết không tồn tại");
        }

        for (SanPhamChiTiet spct : spcts) {
            int need = buyMap.get(spct.getId());
            int stock = spct.getSoLuongTon() == null ? 0 : spct.getSoLuongTon();
            if (need > stock) {
                throw new IllegalArgumentException("Vượt tồn kho: " + spct.getMaSanPhamChiTiet());
            }
        }

        // 2) Tính tiền gốc
        BigDecimal tongTien = BigDecimal.ZERO;
        for (SanPhamChiTiet spct : spcts) {
            int sl = buyMap.get(spct.getId());
            tongTien = tongTien.add(spct.getDonGia().multiply(BigDecimal.valueOf(sl)));
        }

        // 3) Tính giảm giá (voucher hoặc % thủ công)
        BigDecimal tongTienGiam = BigDecimal.ZERO;
        PhieuGiamGia pgg = null;

        if (req.getIdPhieuGiamGia() != null) {
            pgg = phieuGiamGiaRepository.findById(req.getIdPhieuGiamGia())
                    .orElseThrow(() -> new IllegalArgumentException("Phiếu giảm giá không tồn tại"));

            if (Boolean.FALSE.equals(pgg.getTrangThai())) throw new IllegalArgumentException("Voucher không hoạt động");
            if (pgg.getSoLuong() != null) {
                if (pgg.getSoLuong() <= 0) throw new IllegalArgumentException("Voucher đã hết lượt");
                pgg.setSoLuong(pgg.getSoLuong() - 1);
                phieuGiamGiaRepository.save(pgg);
            }
            if (pgg.getDonHangToiThieu() != null && tongTien.compareTo(pgg.getDonHangToiThieu()) < 0)
                throw new IllegalArgumentException("Chưa đạt đơn tối thiểu để dùng voucher");


// ✅ Nếu có % thì dùng %, không phụ thuộc loaiGiam
            BigDecimal pt = pgg.getGiaTriPhanTram();
            if (pt != null && pt.compareTo(BigDecimal.ZERO) > 0) {
                // pt = 0.4 => 40% ; pt = 40 => 40%
                if (pt.compareTo(BigDecimal.ONE) <= 0) {
                    tongTienGiam = tongTien.multiply(pt);
                } else {
                    tongTienGiam = tongTien.multiply(pt).divide(BigDecimal.valueOf(100));
                }

                if (pgg.getGiaTriGiamToiDa() != null && tongTienGiam.compareTo(pgg.getGiaTriGiamToiDa()) > 0) {
                    tongTienGiam = pgg.getGiaTriGiamToiDa();
                }
            } else {
                // ✅ Không có % thì dùng tiền mặt
                BigDecimal tm = pgg.getGiaTriTienMat();
                tongTienGiam = (tm == null) ? BigDecimal.ZERO : tm;
            }

            if (tongTienGiam.compareTo(tongTien) > 0) tongTienGiam = tongTien;

            // trừ lượt voucher
            pgg.setSoLuong(pgg.getSoLuong() - 1);
            phieuGiamGiaRepository.save(pgg);

        } else {
            int percent = req.getGiamThuCongPercent() == null ? 0 : req.getGiamThuCongPercent();
            if (percent < 0) percent = 0;
            if (percent > 100) percent = 100;
            tongTienGiam = tongTien.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100));
        }

        BigDecimal tongTienSauGiam = tongTien.subtract(tongTienGiam);
        if (tongTienSauGiam.compareTo(BigDecimal.ZERO) < 0) tongTienSauGiam = BigDecimal.ZERO;

        // ✅ 3.5) CHECK THANH TOÁN (paid phải >= tổng phải trả)
        BigDecimal paid = req.getPaid() == null ? BigDecimal.ZERO : req.getPaid();
        if (paid.compareTo(tongTienSauGiam) < 0) {
            throw new IllegalArgumentException("Khách thanh toán chưa đủ");
        }
        BigDecimal tienThua = paid.subtract(tongTienSauGiam);
        if (tienThua.compareTo(BigDecimal.ZERO) < 0) tienThua = BigDecimal.ZERO;
        System.out.println("tongTien=" + tongTien
                + " | tongTienGiam=" + tongTienGiam
                + " | tongTienSauGiam=" + tongTienSauGiam
                + " | paid=" + paid
                + " | pggId=" + (pgg == null ? null : pgg.getId())
                + " | loaiGiam=" + (pgg == null ? null : pgg.getLoaiGiam())
                + " | pt=" + (pgg == null ? null : pgg.getGiaTriPhanTram())
                + " | tm=" + (pgg == null ? null : pgg.getGiaTriTienMat())
                + " | max=" + (pgg == null ? null : pgg.getGiaTriGiamToiDa()));
        // 4) Tạo HoaDon
        HoaDon hd = new HoaDon();
        hd.setMaHoaDon(req.getMaHoaDon() == null ? ("HD" + System.currentTimeMillis()) : req.getMaHoaDon());
        hd.setLoaiDon(req.getLoaiDon() == null ? false : req.getLoaiDon());
        hd.setPhiVanChuyen(req.getPhiVanChuyen());

        hd.setTrangThaiDon(TrangThaiDonHang.HOAN_THANH.getCode());
        hd.setTongTien(tongTien);
        hd.setTongTienGiam(tongTienGiam);
        hd.setTongTienSauGiam(tongTienSauGiam);

        hd.setTenKhachHang(req.getTenKhachHang());
        hd.setSoDienThoai(req.getSoDienThoai());
        hd.setEmailKhachHang(req.getEmailKhachHang());
        hd.setDiaChiKhachHang(req.getDiaChiKhachHang());
        hd.setGhiChu(req.getGhiChu());

        hd.setNgayTao(LocalDateTime.now());
        hd.setTrangThai(true);

        if (req.getIdKhachHang() != null) {
            hd.setKhachHang(khachHangRepository.findById(req.getIdKhachHang()).orElse(null));
        }
        if (pgg != null) hd.setPhieuGiamGia(pgg);
        NhanVien nv = getCurrentNhanVien();
        hd.setNhanVien(nv);
        hd.setNguoiTao(nv != null ? nv.getTenNhanVien() : currentUser());
        hd = hoaDonRepository.save(hd);

        // 5) Tạo chi tiết + trừ kho
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

        // 6) Lịch sử hoá đơn
        LichSuHoaDon ls = new LichSuHoaDon();
        ls.setHoaDon(hd);
        ls.setHanhDong("Tạo đơn tại quầy");
        ls.setGhiChu("POS checkout");
        ls.setThoiGian(LocalDateTime.now());
        ls.setTrangThai(true);
        lichSuHoaDonRepository.save(ls);
// 7) Lịch sử thanh toán (POS: thanh toán thành công ngay)
        LichSuThanhToan payHis = new LichSuThanhToan();
        payHis.setHoaDon(hd);

// mã giao dịch: lấy từ req nếu có, không có thì tự sinh
        String maGd = (req.getMaGiaoDich() == null || req.getMaGiaoDich().isBlank())
                ? ("POS-" + hd.getMaHoaDon() + "-" + System.currentTimeMillis())
                : req.getMaGiaoDich().trim();
        payHis.setMaGiaoDich(maGd);

// Số tiền ghi vào lịch sử nên = tổng tiền sau giảm (để UI hiển thị đúng “đã thanh toán”)
        payHis.setSoTien(tongTienSauGiam);
        payHis.setNgayThanhToan(LocalDateTime.now());

// Hình thức thanh toán
        String hinhThuc = "TIEN_MAT";
        if (req.getIdPhuongThucThanhToan() != null) {
            PhuongThucThanhToan pttt = phuongThucThanhToanRepository
                    .findById(req.getIdPhuongThucThanhToan())
                    .orElse(null);
            if (pttt != null && pttt.getTenPhuongThucThanhToan() != null) {
                hinhThuc = pttt.getTenPhuongThucThanhToan();
            }
        }
        payHis.setHinhThucThanhToan(hinhThuc);

// Ghi chú thanh toán (kèm khách đưa/tiền thừa cho dễ check)
        String note = (req.getGhiChuThanhToan() != null && !req.getGhiChuThanhToan().isBlank())
                ? req.getGhiChuThanhToan()
                : (req.getGhiChu() != null ? req.getGhiChu() : "POS checkout");
        payHis.setGhiChu(note + " | Khách đưa: " + paid + " | Tiền thừa: " + tienThua);

        payHis.setTrangThai(true);
        lichSuThanhToanRepository.saveAndFlush(payHis);
        return buildDetail(hd);
    }

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
        hd.setNguoiTao("system");
        hd.setTrangThai(true);

        hd = hoaDonRepository.save(hd);

        return new TaohoadonResponse(hd.getId(), hd.getMaHoaDon(), hd.getTrangThaiDon());
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
        Specification<HoaDon> spec = HoaDonSpecifications.advanced(
                keyword, trangThaiDon, phanLoai, loaiDon, from, to,
                minTotal, maxTotal, hasVoucher, idNhanVien, active
        );

        Page<HoaDon> page = hoaDonRepository.findAll(spec, pageable);

        return page.map(hd -> {
            TrangThaiDonHang st = TrangThaiDonHang.fromCode(hd.getTrangThaiDon());
            return HoaDonListResponse.builder()
                    .id(hd.getId())
                    .maHoaDon(hd.getMaHoaDon())
                    .trangThaiDon(hd.getTrangThaiDon())
                    .tenTrangThaiDon(st.getTen())
                    .loaiDon(hd.getLoaiDon())
                    .tongTienSauGiam(hd.getTongTienSauGiam())
                    .tenKhachHang(hd.getTenKhachHang())
                    .soDienThoai(hd.getSoDienThoai())
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
        return lichSuThanhToanRepository.findAllByHoaDon_IdOrderByNgayThanhToanDesc(idHoaDon)
                .stream()
                .map(x -> LichSuThanhToanResponse.builder()
                        .id(x.getId())
                        .maGiaoDich(x.getMaGiaoDich())
                        .soTien(x.getSoTien())
                        .ngayThanhToan(x.getNgayThanhToan())
                        .hinhThucThanhToan(x.getHinhThucThanhToan())
                        .ghiChu(x.getGhiChu())
                        .build())
                .collect(Collectors.toList());
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

        // Rule tối thiểu (bạn có thể siết thêm):
        if (oldSt == TrangThaiDonHang.DA_HUY) {
            throw new IllegalArgumentException("Đơn đã huỷ, không thể đổi trạng thái");
        }
        if (oldSt == TrangThaiDonHang.DA_HOAN) {
            throw new IllegalArgumentException("Đơn đã hoàn, không thể đổi trạng thái");
        }

        hd.setTrangThaiDon(newSt.getCode());
        hd.setNgayCapNhat(LocalDateTime.now());
        hd.setNguoiCapNhat(currentUser());
        hoaDonRepository.save(hd);

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

        // hoàn kho từ chi tiết
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
            }
            sanPhamChiTietRepository.saveAll(spcts);
        }

        hd.setTrangThaiDon(TrangThaiDonHang.DA_HOAN.getCode());
        hd.setNgayCapNhat(LocalDateTime.now());
        hd.setNguoiCapNhat(currentUser());
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
                        .findTop1BySanPhamChiTiet_IdAndTrangThaiTrueOrderByIdDesc(spct.getId())
                        .map(AnhChiTietSanPham::getTen)
                        .orElse(null);
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
        return HoaDonDetailResponse.builder()
                .id(hd.getId())
                .maHoaDon(hd.getMaHoaDon())
                .idKhachHang(hd.getKhachHang() == null ? null : hd.getKhachHang().getId())
                .idNhanVien(nv == null ? null : nv.getId())
                .maNhanVien(nv == null ? null : nv.getMaNhanVien())
                .tenNhanVien(nv == null ? null : nv.getTenNhanVien())

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
                .build();
    }
}
