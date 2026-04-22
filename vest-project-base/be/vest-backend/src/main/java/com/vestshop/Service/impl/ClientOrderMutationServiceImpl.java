package com.vestshop.Service.impl;

import com.vestshop.Entity.*;
import com.vestshop.Exception.ApiException;
import com.vestshop.Repository.GiaoDichThanhToanRepository;
import com.vestshop.Repository.HoaDonChiTietRepository;
import com.vestshop.Repository.HoaDonRepository;
import com.vestshop.Repository.KhachHangRepository;
import com.vestshop.Repository.LichSuHoaDonRepository;
import com.vestshop.Repository.SanPhamChiTietRepository;
import com.vestshop.Service.ClientOrderMutationService;
import com.vestshop.common.TrangThaiDonHang;
import com.vestshop.dto.request.ClientOrderCancelRequest;
import com.vestshop.dto.request.ClientOrderUpdateItemsRequest;
import com.vestshop.dto.request.ClientOrderUpdateShippingRequest;
import com.vestshop.dto.response.OnlineOrderLookupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientOrderMutationServiceImpl implements ClientOrderMutationService {

    private final KhachHangRepository khachHangRepository;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final GiaoDichThanhToanRepository giaoDichThanhToanRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    @Override
    public OnlineOrderLookupResponse cancelMyOrder(Long orderId, String principal, ClientOrderCancelRequest request) {
        KhachHang khachHang = getCurrentCustomer(principal);

        HoaDon hoaDon = hoaDonRepository
                .findByIdAndKhachHang_IdAndLoaiDonTrue(orderId, khachHang.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));

        if (hoaDon.getTrangThaiDon() == null
                || hoaDon.getTrangThaiDon() != TrangThaiDonHang.CHO_XAC_NHAN.getCode()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Chỉ được hủy đơn ở trạng thái chờ xác nhận");
        }

        hoaDon.setTrangThaiDon(TrangThaiDonHang.YEU_CAU_HUY.getCode());
        hoaDon.setNgayCapNhat(LocalDateTime.now());
        hoaDon.setNguoiCapNhat(resolveCustomerName(khachHang));

        hoaDonRepository.save(hoaDon);

        LichSuHoaDon ls = new LichSuHoaDon();
        ls.setHoaDon(hoaDon);
        ls.setHanhDong("KHACH_HANG_YEU_CAU_HUY_DON");
        ls.setGhiChu(buildCancelNote(khachHang, request));
        ls.setThoiGian(LocalDateTime.now());
        ls.setTrangThai(true);
        lichSuHoaDonRepository.save(ls);

        return toDetail(hoaDon);
    }

    @Override
    public OnlineOrderLookupResponse updateMyOrderShipping(Long orderId, String principal, ClientOrderUpdateShippingRequest request) {
        KhachHang khachHang = getCurrentCustomer(principal);

        HoaDon hoaDon = hoaDonRepository
                .findByIdAndKhachHang_IdAndLoaiDonTrue(orderId, khachHang.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));

        if (hoaDon.getTrangThaiDon() == null
                || hoaDon.getTrangThaiDon() != TrangThaiDonHang.CHO_XAC_NHAN.getCode()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Đơn này không còn cho phép sửa thông tin nhận hàng");
        }

        String paymentMethod = resolvePaymentMethod(hoaDon);

        hoaDon.setTenNguoiNhanHang(trim(request.getTenNguoiNhanHang()));
        hoaDon.setSoDienThoaiNhanHang(trim(request.getSoDienThoaiNhanHang()));
        hoaDon.setGhiChu(trim(request.getGhiChu()));

        // COD: cho sửa full địa chỉ
        if ("COD".equalsIgnoreCase(paymentMethod)) {
            hoaDon.setTinhThanhNhanHang(trim(request.getTinhThanhNhanHang()));
            hoaDon.setQuanHuyenNhanHang(trim(request.getQuanHuyenNhanHang()));
            hoaDon.setPhuongXaNhanHang(trim(request.getPhuongXaNhanHang()));
            hoaDon.setDiaChiNhanHangChiTiet(trim(request.getDiaChiNhanHangChiTiet()));
        }

        // QR / CK: tạm thời chỉ cho sửa tên, sđt, ghi chú
        hoaDon.setNgayCapNhat(LocalDateTime.now());
        hoaDon.setNguoiCapNhat(resolveCustomerName(khachHang));

        hoaDonRepository.save(hoaDon);

        LichSuHoaDon ls = new LichSuHoaDon();
        ls.setHoaDon(hoaDon);
        ls.setHanhDong("KHACH_HANG_SUA_GIAO_HANG");
        ls.setGhiChu(buildShippingNote(khachHang, paymentMethod));
        ls.setThoiGian(LocalDateTime.now());
        ls.setTrangThai(true);
        lichSuHoaDonRepository.save(ls);

        return toDetail(hoaDon);
    }


    @Override
    public OnlineOrderLookupResponse updateMyOrderItems(Long orderId, String principal, ClientOrderUpdateItemsRequest request) {
        KhachHang khachHang = getCurrentCustomer(principal);

        HoaDon hoaDon = hoaDonRepository
                .findByIdAndKhachHang_IdAndLoaiDonTrue(orderId, khachHang.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));

        if (hoaDon.getTrangThaiDon() == null
                || hoaDon.getTrangThaiDon() != TrangThaiDonHang.CHO_XAC_NHAN.getCode()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Đơn này không còn cho phép sửa sản phẩm");
        }

        String paymentMethod = resolvePaymentMethod(hoaDon);
        if (!"COD".equalsIgnoreCase(paymentMethod)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Chỉ đơn COD mới được sửa sản phẩm");
        }

        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Đơn hàng phải còn ít nhất 1 sản phẩm");
        }

        List<HoaDonChiTiet> currentItems = hoaDonChiTietRepository.findAllByHoaDon_Id(hoaDon.getId());
        if (currentItems.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Đơn hàng không có sản phẩm để cập nhật");
        }

        java.util.Map<Long, HoaDonChiTiet> currentBySpctId = currentItems.stream()
                .filter(x -> x.getSanPhamChiTiet() != null && x.getSanPhamChiTiet().getId() != null)
                .collect(java.util.stream.Collectors.toMap(x -> x.getSanPhamChiTiet().getId(), x -> x, (a, b) -> a));

        java.util.Map<Long, Integer> requested = new java.util.LinkedHashMap<>();
        for (ClientOrderUpdateItemsRequest.Item item : request.getItems()) {
            if (item == null || item.getIdSanPhamChiTiet() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Thiếu sản phẩm cần cập nhật");
            }
            int soLuong = item.getSoLuong() == null ? 0 : item.getSoLuong();
            if (soLuong <= 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Số lượng sản phẩm phải lớn hơn 0");
            }
            if (!currentBySpctId.containsKey(item.getIdSanPhamChiTiet())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Chỉ được sửa sản phẩm đang có trong đơn hàng");
            }
            requested.merge(item.getIdSanPhamChiTiet(), soLuong, Integer::sum);
        }

        if (requested.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Đơn hàng phải còn ít nhất 1 sản phẩm");
        }

        List<SanPhamChiTiet> spcts = sanPhamChiTietRepository.findAllById(requested.keySet());
        if (spcts.size() != requested.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Có sản phẩm không tồn tại");
        }

        java.util.Map<Long, SanPhamChiTiet> spctMap = spcts.stream()
                .collect(java.util.stream.Collectors.toMap(SanPhamChiTiet::getId, x -> x));

        java.math.BigDecimal tongTien = java.math.BigDecimal.ZERO;
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        for (java.util.Map.Entry<Long, Integer> en : requested.entrySet()) {
            SanPhamChiTiet spct = spctMap.get(en.getKey());
            int soLuong = en.getValue();

            int tonKho = spct.getSoLuongTon() == null ? 0 : spct.getSoLuongTon();
            if (!Boolean.TRUE.equals(spct.getTrangThai())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Sản phẩm " + spct.getMaSanPhamChiTiet() + " hiện không khả dụng");
            }
            if (tonKho < soLuong) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Sản phẩm " + spct.getMaSanPhamChiTiet() + " không đủ tồn kho");
            }

            HoaDonChiTiet ct = currentBySpctId.get(en.getKey());
            ct.setSoLuong(soLuong);
            ct.setNgaySua(now);
            ct.setNguoiSua(resolveCustomerName(khachHang));
            ct.setTrangThai(true);

            java.math.BigDecimal donGia = spct.getDonGia() == null ? java.math.BigDecimal.ZERO : spct.getDonGia();
            tongTien = tongTien.add(donGia.multiply(java.math.BigDecimal.valueOf(soLuong)));
        }

        // Xóa các dòng sản phẩm bị remove khỏi đơn
        java.util.List<HoaDonChiTiet> removed = currentItems.stream()
                .filter(x -> x.getSanPhamChiTiet() != null && x.getSanPhamChiTiet().getId() != null)
                .filter(x -> !requested.containsKey(x.getSanPhamChiTiet().getId()))
                .toList();
        if (removed.size() == currentItems.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Đơn hàng phải còn ít nhất 1 sản phẩm");
        }
        if (!removed.isEmpty()) {
            hoaDonChiTietRepository.deleteAll(removed);
        }

        hoaDonChiTietRepository.saveAll(currentItems.stream()
                .filter(x -> x.getSanPhamChiTiet() != null && x.getSanPhamChiTiet().getId() != null)
                .filter(x -> requested.containsKey(x.getSanPhamChiTiet().getId()))
                .toList());

        java.math.BigDecimal tongTienGiam = recalculateDiscount(hoaDon, tongTien);
        java.math.BigDecimal tongTienSauGiam = tongTien
                .subtract(tongTienGiam)
                .add(defaultBigDecimal(hoaDon.getPhiVanChuyen()));
        if (tongTienSauGiam.compareTo(java.math.BigDecimal.ZERO) < 0) {
            tongTienSauGiam = java.math.BigDecimal.ZERO;
        }

        hoaDon.setTongTien(tongTien);
        hoaDon.setTongTienGiam(tongTienGiam);
        hoaDon.setTongTienSauGiam(tongTienSauGiam);
        hoaDon.setNgayCapNhat(now);
        hoaDon.setNguoiCapNhat(resolveCustomerName(khachHang));
        hoaDonRepository.save(hoaDon);

        LichSuHoaDon ls = new LichSuHoaDon();
        ls.setHoaDon(hoaDon);
        ls.setHanhDong("KHACH_HANG_SUA_SAN_PHAM");
        ls.setGhiChu("Khách hàng " + resolveCustomerName(khachHang) + " cập nhật sản phẩm trong đơn hàng");
        ls.setThoiGian(now);
        ls.setTrangThai(true);
        lichSuHoaDonRepository.save(ls);

        return toDetail(hoaDon);
    }

    private java.math.BigDecimal recalculateDiscount(HoaDon hoaDon, java.math.BigDecimal tongTien) {
        if (tongTien == null || tongTien.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            hoaDon.setPhieuGiamGia(null);
            return java.math.BigDecimal.ZERO;
        }

        if (hoaDon.getPhieuGiamGia() == null) {
            return java.math.BigDecimal.ZERO;
        }

        com.vestshop.Entity.PhieuGiamGia pgg = hoaDon.getPhieuGiamGia();
        if (Boolean.FALSE.equals(pgg.getTrangThai())) {
            hoaDon.setPhieuGiamGia(null);
            return java.math.BigDecimal.ZERO;
        }

        java.math.BigDecimal min = pgg.getDonHangToiThieu() == null ? java.math.BigDecimal.ZERO : pgg.getDonHangToiThieu();
        if (tongTien.compareTo(min) < 0) {
            hoaDon.setPhieuGiamGia(null);
            return java.math.BigDecimal.ZERO;
        }

        java.math.BigDecimal tongTienGiam = java.math.BigDecimal.ZERO;
        java.math.BigDecimal pt = pgg.getGiaTriPhanTram();
        if (pt != null && pt.compareTo(java.math.BigDecimal.ZERO) > 0) {
            if (pt.compareTo(java.math.BigDecimal.ONE) <= 0) tongTienGiam = tongTien.multiply(pt);
            else tongTienGiam = tongTien.multiply(pt).divide(java.math.BigDecimal.valueOf(100));

            java.math.BigDecimal cap = pgg.getGiaTriGiamToiDa();
            if (cap != null && cap.compareTo(java.math.BigDecimal.ZERO) > 0 && tongTienGiam.compareTo(cap) > 0) {
                tongTienGiam = cap;
            }
        } else {
            java.math.BigDecimal tm = pgg.getGiaTriTienMat();
            tongTienGiam = tm == null ? java.math.BigDecimal.ZERO : tm;
        }

        if (tongTienGiam.compareTo(tongTien) > 0) tongTienGiam = tongTien;
        return tongTienGiam;
    }

    private KhachHang getCurrentCustomer(String principal) {
        if (principal == null || principal.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Bạn chưa đăng nhập");
        }

        return khachHangRepository.findByTaiKhoanIgnoreCase(principal)
                .or(() -> khachHangRepository.findByEmailIgnoreCase(principal))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng"));
    }

    private String resolvePaymentMethod(HoaDon hoaDon) {
        GiaoDichThanhToan gdtt = giaoDichThanhToanRepository
                .findFirstByHoaDon_IdOrderByIdDesc(hoaDon.getId())
                .orElse(null);

        if (gdtt == null) {
            return "COD";
        }

        if (gdtt.getPhuongThucThanhToan() != null
                && gdtt.getPhuongThucThanhToan().getTenPhuongThucThanhToan() != null
                && !gdtt.getPhuongThucThanhToan().getTenPhuongThucThanhToan().isBlank()) {
            return gdtt.getPhuongThucThanhToan().getTenPhuongThucThanhToan();
        }

        return "QR";
    }

    private String buildCancelNote(KhachHang khachHang, ClientOrderCancelRequest request) {
        String lyDo = request != null && request.getLyDo() != null ? request.getLyDo().trim() : "";
        String ghiChu = request != null && request.getGhiChu() != null ? request.getGhiChu().trim() : "";

        StringBuilder sb = new StringBuilder("Khách hàng ");
        sb.append(resolveCustomerName(khachHang));
        sb.append(" hủy đơn");

        if (!lyDo.isBlank()) {
            sb.append(" | Lý do: ").append(lyDo);
        }
        if (!ghiChu.isBlank()) {
            sb.append(" | Ghi chú: ").append(ghiChu);
        }

        return sb.toString();
    }

    private String buildShippingNote(KhachHang khachHang, String paymentMethod) {
        return "Khách hàng "
                + resolveCustomerName(khachHang)
                + " cập nhật thông tin nhận hàng"
                + " | PTTT: " + paymentMethod;
    }

    private String resolveCustomerName(KhachHang khachHang) {
        if (khachHang == null) {
            return "Khách hàng";
        }
        if (khachHang.getTenKhachHang() != null && !khachHang.getTenKhachHang().isBlank()) {
            return khachHang.getTenKhachHang();
        }
        return khachHang.getTaiKhoan();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private OnlineOrderLookupResponse toDetail(HoaDon hoaDon) {
        List<OnlineOrderLookupResponse.Item> items = hoaDonChiTietRepository
                .findAllByHoaDon_Id(hoaDon.getId())
                .stream()
                .map(ct -> {
                    SanPhamChiTiet spct = ct.getSanPhamChiTiet();

                    BigDecimal donGia = (spct != null && spct.getDonGia() != null)
                            ? spct.getDonGia()
                            : BigDecimal.ZERO;

                    Integer soLuong = ct.getSoLuong() == null ? 0 : ct.getSoLuong();
                    BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(soLuong));

                    String tenSanPham = (spct != null && spct.getSanPham() != null)
                            ? spct.getSanPham().getTenSanPham()
                            : null;

                    String mauSac = (spct != null && spct.getMauSac() != null)
                            ? spct.getMauSac().getTen()
                            : null;

                    String kichCo = (spct != null && spct.getKichCo() != null)
                            ? spct.getKichCo().getSoSize()
                            : null;

                    String anh = null;
                    if (spct != null) {
                        anh = spct.getMediaPrimary() != null && spct.getMediaPrimary().getSecureUrl() != null
                                ? spct.getMediaPrimary().getSecureUrl()
                                : spct.getAnh();
                    }

                    return OnlineOrderLookupResponse.Item.builder()
                            .idHoaDonChiTiet(ct.getId())
                            .idSanPhamChiTiet(spct == null ? null : spct.getId())
                            .maSanPhamChiTiet(spct == null ? null : spct.getMaSanPhamChiTiet())
                            .tenSanPham(tenSanPham)
                            .mauSac(mauSac)
                            .kichCo(kichCo)
                            .soLuong(soLuong)
                            .donGia(donGia)
                            .thanhTien(thanhTien)
                            .anhDaiDien(anh)
                            .build();
                })
                .toList();

        GiaoDichThanhToan gdtt = giaoDichThanhToanRepository
                .findFirstByHoaDon_IdOrderByIdDesc(hoaDon.getId())
                .orElse(null);

        String paymentMethod = "COD";
        String paymentStatus = "UNPAID";

        if (gdtt != null) {
            if (gdtt.getPhuongThucThanhToan() != null
                    && gdtt.getPhuongThucThanhToan().getTenPhuongThucThanhToan() != null
                    && !gdtt.getPhuongThucThanhToan().getTenPhuongThucThanhToan().isBlank()) {
                paymentMethod = gdtt.getPhuongThucThanhToan().getTenPhuongThucThanhToan();
            } else {
                paymentMethod = "QR";
            }

            paymentStatus = Boolean.TRUE.equals(gdtt.getTrangThai()) ? "PAID" : "PENDING";
        }

        TrangThaiDonHang trangThai = TrangThaiDonHang.fromCode(hoaDon.getTrangThaiDon());
        String tenTrangThai = trangThai != null ? trangThai.getTen() : "Không xác định";

        return OnlineOrderLookupResponse.builder()
                .id(hoaDon.getId())
                .maHoaDon(hoaDon.getMaHoaDon())
                .trangThaiDon(hoaDon.getTrangThaiDon())
                .tenTrangThaiDon(tenTrangThai)
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .tenKhachHang(hoaDon.getTenKhachHang())
                .soDienThoai(hoaDon.getSoDienThoai())
                .tenNguoiNhanHang(hoaDon.getTenNguoiNhanHang())
                .soDienThoaiNhanHang(hoaDon.getSoDienThoaiNhanHang())
                .tinhThanhNhanHang(hoaDon.getTinhThanhNhanHang())
                .quanHuyenNhanHang(hoaDon.getQuanHuyenNhanHang())
                .phuongXaNhanHang(hoaDon.getPhuongXaNhanHang())
                .diaChiNhanHangChiTiet(hoaDon.getDiaChiNhanHangChiTiet())
                .phiVanChuyen(defaultBigDecimal(hoaDon.getPhiVanChuyen()))
                .tongTien(defaultBigDecimal(hoaDon.getTongTien()))
                .tongTienGiam(defaultBigDecimal(hoaDon.getTongTienGiam()))
                .tongTienSauGiam(defaultBigDecimal(hoaDon.getTongTienSauGiam()))
                .ghiChu(hoaDon.getGhiChu())
                .ngayTao(hoaDon.getNgayTao())
                .items(items)
                .build();
    }

    private BigDecimal defaultBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}