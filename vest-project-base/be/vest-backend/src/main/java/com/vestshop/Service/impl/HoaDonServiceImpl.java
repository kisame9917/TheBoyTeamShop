package com.vestshop.Service.impl;

import com.vestshop.Entity.*;
import com.vestshop.Repository.*;
import com.vestshop.Service.HoaDonService;
import com.vestshop.common.TrangThaiDonHang;
import com.vestshop.dto.request.HoaDonChangeStatusRequest;
import com.vestshop.dto.request.HoaDonReturnRequest;
import com.vestshop.dto.response.*;
import com.vestshop.spec.HoaDonSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
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
    private final AnhChiTietSanPhamRepository anhChiTietSanPhamRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final LichSuThanhToanRepository lichSuThanhToanRepository;
    private final GiaoDichThanhToanRepository giaoDichThanhToanRepository;

    // TODO: nếu bạn có Security/JWT thì thay bằng user hiện tại
    private String currentUser() {
        return "system";
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
        return lichSuHoaDonRepository.findAllByHoaDon_IdOrderByThoiGianDesc(idHoaDon)
                .stream()
                .map(x -> LichSuHoaDonResponse.builder()
                        .id(x.getId())
                        .hanhDong(x.getHanhDong())
                        .ghiChu(x.getGhiChu())
                        .thoiGian(x.getThoiGian())
                        .build())
                .collect(Collectors.toList());
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

        return HoaDonDetailResponse.builder()
                .id(hd.getId())
                .maHoaDon(hd.getMaHoaDon())
                .idKhachHang(hd.getKhachHang() == null ? null : hd.getKhachHang().getId())
                .idNhanVien(hd.getNhanVien() == null ? null : hd.getNhanVien().getId())
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
