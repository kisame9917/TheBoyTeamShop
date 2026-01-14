package com.vestshop.Service.impl;

import com.vestshop.Entity.*;
import com.vestshop.Repository.*;
import com.vestshop.Service.HoaDonService;
import com.vestshop.dto.request.HoaDonCreateRequest;
import com.vestshop.dto.request.HoaDonUpdateRequest;
import com.vestshop.dto.response.HoaDonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HoaDonServiceImpl implements HoaDonService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;

    @Override
    @Transactional
    public HoaDonResponse create(HoaDonCreateRequest req) {
        Map<Long, Integer> merged = mergeItems(req.getItems());

        KhachHang kh = req.getIdKhachHang() == null ? null :
                khachHangRepository.findById(req.getIdKhachHang())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng"));

        NhanVien nv = req.getIdNhanVien() == null ? null :
                nhanVienRepository.findById(req.getIdNhanVien())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên"));

        PhieuGiamGia pgg = req.getIdPhieuGiamGia() == null ? null :
                phieuGiamGiaRepository.findById(req.getIdPhieuGiamGia())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu giảm giá"));

        HoaDon hd = HoaDon.builder()
                .maHoaDon(generateMaHoaDon())
                .khachHang(kh)
                .nhanVien(nv)
                .phieuGiamGia(pgg)
                .loaiDon(req.getLoaiDon())
                .phiVanChuyen(nvl(req.getPhiVanChuyen()))
                .tenKhachHang(req.getTenKhachHang())
                .soDienThoai(req.getSoDienThoai())
                .diaChiKhachHang(req.getDiaChiKhachHang())
                .emailKhachHang(req.getEmailKhachHang())
                .qrCode(req.getQrCode())
                .ghiChu(req.getGhiChu())
                .tongTien(BigDecimal.ZERO)
                .tongTienGiam(BigDecimal.ZERO)
                .tongTienSauGiam(BigDecimal.ZERO)
                .trangThai(true)
                .ngayTao(LocalDateTime.now())
                .build();

        HoaDon saved = hoaDonRepository.save(hd);

        List<HoaDonChiTiet> chiTiets = buildChiTiet(saved, merged);
        hoaDonChiTietRepository.saveAll(chiTiets);

        recalcTotals(saved, chiTiets, pgg);
        HoaDon saved2 = hoaDonRepository.save(saved);

        return toResponse(saved2, chiTiets);
    }

    @Override
    @Transactional(readOnly = true)
    public HoaDonResponse getById(Long id) {
        HoaDon hd = hoaDonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoá đơn"));
        List<HoaDonChiTiet> cts = hoaDonChiTietRepository.findAllByHoaDon_Id(id);
        return toResponse(hd, cts);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HoaDonResponse> getPage(Boolean trangThai, Pageable pageable) {
        Page<HoaDon> page = trangThai == null
                ? hoaDonRepository.findAll(pageable)
                : hoaDonRepository.findByTrangThai(trangThai, pageable);

        return page.map(hd -> {
            List<HoaDonChiTiet> cts = hoaDonChiTietRepository.findAllByHoaDon_Id(hd.getId());
            return toResponse(hd, cts);
        });
    }

    @Override
    @Transactional
    public HoaDonResponse update(Long id, HoaDonUpdateRequest req) {
        HoaDon hd = hoaDonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoá đơn"));

        KhachHang kh = req.getIdKhachHang() == null ? null :
                khachHangRepository.findById(req.getIdKhachHang())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng"));

        NhanVien nv = req.getIdNhanVien() == null ? null :
                nhanVienRepository.findById(req.getIdNhanVien())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên"));

        PhieuGiamGia pgg = req.getIdPhieuGiamGia() == null ? null :
                phieuGiamGiaRepository.findById(req.getIdPhieuGiamGia())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu giảm giá"));

        hd.setKhachHang(kh);
        hd.setNhanVien(nv);
        hd.setPhieuGiamGia(pgg);

        hd.setLoaiDon(req.getLoaiDon());
        hd.setPhiVanChuyen(nvl(req.getPhiVanChuyen()));

        hd.setTenKhachHang(req.getTenKhachHang());
        hd.setSoDienThoai(req.getSoDienThoai());
        hd.setDiaChiKhachHang(req.getDiaChiKhachHang());
        hd.setEmailKhachHang(req.getEmailKhachHang());
        hd.setQrCode(req.getQrCode());
        hd.setGhiChu(req.getGhiChu());

        hd.setNgayCapNhat(LocalDateTime.now());

        List<HoaDonChiTiet> cts;
        if (req.getItems() != null) {
            Map<Long, Integer> merged = mergeItems(req.getItems());
            hoaDonChiTietRepository.deleteByHoaDonId(id);
            hoaDonRepository.flush();
            cts = buildChiTiet(hd, merged);
            hoaDonChiTietRepository.saveAll(cts);
        } else {
            cts = hoaDonChiTietRepository.findAllByHoaDon_Id(id);
        }

        recalcTotals(hd, cts, pgg);
        HoaDon saved = hoaDonRepository.save(hd);

        return toResponse(saved, cts);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        HoaDon hd = hoaDonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoá đơn"));
        hd.setTrangThai(false);
        hd.setNgayCapNhat(LocalDateTime.now());
        hoaDonRepository.save(hd);
    }

    private Map<Long, Integer> mergeItems(List<? extends Object> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Danh sách sản phẩm không được trống");
        }

        Map<Long, Integer> merged = new HashMap<>();

        for (Object obj : items) {
            Long idSpct;
            Integer soLuong;

            if (obj instanceof HoaDonCreateRequest.Item it) {
                idSpct = it.getIdSanPhamChiTiet();
                soLuong = it.getSoLuong();
            } else if (obj instanceof HoaDonUpdateRequest.Item it) {
                idSpct = it.getIdSanPhamChiTiet();
                soLuong = it.getSoLuong();
            } else {
                throw new IllegalArgumentException("Item không hợp lệ");
            }

            if (idSpct == null) throw new IllegalArgumentException("Thiếu idSanPhamChiTiet");
            if (soLuong == null || soLuong <= 0) throw new IllegalArgumentException("Số lượng phải > 0");

            merged.merge(idSpct, soLuong, Integer::sum);
        }

        return merged;
    }

    private List<HoaDonChiTiet> buildChiTiet(HoaDon hoaDon, Map<Long, Integer> merged) {
        List<HoaDonChiTiet> list = new ArrayList<>();

        for (Map.Entry<Long, Integer> e : merged.entrySet()) {
            SanPhamChiTiet spct = sanPhamChiTietRepository.findById(e.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy SPCT id=" + e.getKey()));

            HoaDonChiTiet ct = new HoaDonChiTiet();
            ct.setHoaDon(hoaDon);
            ct.setSanPhamChiTiet(spct);
            ct.setSoLuong(e.getValue());
            ct.setNgayTao(LocalDateTime.now());
            ct.setTrangThai(true);

            list.add(ct);
        }

        return list;
    }

    private void recalcTotals(HoaDon hoaDon, List<HoaDonChiTiet> cts, PhieuGiamGia pgg) {
        BigDecimal tongTien = BigDecimal.ZERO;

        for (HoaDonChiTiet ct : cts) {
            BigDecimal donGia = ct.getSanPhamChiTiet() != null ? ct.getSanPhamChiTiet().getDonGia() : BigDecimal.ZERO;
            if (donGia == null) donGia = BigDecimal.ZERO;
            tongTien = tongTien.add(donGia.multiply(BigDecimal.valueOf(ct.getSoLuong())));
        }

        BigDecimal tongGiam = calcDiscount(pgg, tongTien);
        BigDecimal tongSau = tongTien.subtract(tongGiam).add(nvl(hoaDon.getPhiVanChuyen()));
        if (tongSau.compareTo(BigDecimal.ZERO) < 0) tongSau = BigDecimal.ZERO;

        hoaDon.setTongTien(tongTien);
        hoaDon.setTongTienGiam(tongGiam);
        hoaDon.setTongTienSauGiam(tongSau);
    }

    private BigDecimal calcDiscount(PhieuGiamGia pgg, BigDecimal tongTien) {
        if (pgg == null) return BigDecimal.ZERO;
        if (Boolean.FALSE.equals(pgg.getTrangThai())) return BigDecimal.ZERO;

        Boolean loai = pgg.getLoaiGiam();
        BigDecimal giam = BigDecimal.ZERO;

        if (Boolean.TRUE.equals(loai) && pgg.getGiaTriPhanTram() != null) {
            giam = tongTien.multiply(pgg.getGiaTriPhanTram()).divide(BigDecimal.valueOf(100));
        } else if (Boolean.FALSE.equals(loai) && pgg.getGiaTriTienMat() != null) {
            giam = pgg.getGiaTriTienMat();
        }

        if (giam.compareTo(tongTien) > 0) giam = tongTien;
        if (giam.compareTo(BigDecimal.ZERO) < 0) giam = BigDecimal.ZERO;

        return giam;
    }

    private HoaDonResponse toResponse(HoaDon hd, List<HoaDonChiTiet> cts) {
        List<HoaDonResponse.Item> items = cts.stream().map(ct -> {
            BigDecimal donGia = ct.getSanPhamChiTiet() != null ? ct.getSanPhamChiTiet().getDonGia() : BigDecimal.ZERO;
            if (donGia == null) donGia = BigDecimal.ZERO;
            BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(ct.getSoLuong()));

            return HoaDonResponse.Item.builder()
                    .idSanPhamChiTiet(ct.getSanPhamChiTiet().getId())
                    .soLuong(ct.getSoLuong())
                    .donGia(donGia)
                    .thanhTien(thanhTien)
                    .build();
        }).collect(Collectors.toList());

        return HoaDonResponse.builder()
                .id(hd.getId())
                .maHoaDon(hd.getMaHoaDon())
                .idKhachHang(hd.getKhachHang() == null ? null : hd.getKhachHang().getId())
                .idNhanVien(hd.getNhanVien() == null ? null : hd.getNhanVien().getId())
                .idPhieuGiamGia(hd.getPhieuGiamGia() == null ? null : hd.getPhieuGiamGia().getId())
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
                .build();
    }

    private String generateMaHoaDon() {
        String base = "HD" + java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .format(LocalDateTime.now());
        for (int i = 0; i < 5; i++) {
            String code = base + String.format("%04d", new Random().nextInt(10000));
            if (!hoaDonRepository.existsByMaHoaDon(code)) return code;
        }
        return base + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
