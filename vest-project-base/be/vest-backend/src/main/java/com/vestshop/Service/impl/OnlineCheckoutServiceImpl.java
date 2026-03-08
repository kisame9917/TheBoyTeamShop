package com.vestshop.Service.impl;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import com.vestshop.Entity.GiaoDichThanhToan;
import com.vestshop.Entity.HoaDon;
import com.vestshop.Entity.HoaDonChiTiet;
import com.vestshop.Entity.KhachHang;
import com.vestshop.Entity.LichSuThanhToan;
import com.vestshop.Entity.PhieuGiamGia;
import com.vestshop.Entity.PhuongThucThanhToan;
import com.vestshop.Entity.SanPhamChiTiet;
import com.vestshop.Exception.BadRequestException;
import com.vestshop.Repository.GiaoDichThanhToanRepository;
import com.vestshop.Repository.HoaDonChiTietRepository;
import com.vestshop.Repository.HoaDonRepository;
import com.vestshop.Repository.KhachHangRepository;
import com.vestshop.Repository.LichSuThanhToanRepository;
import com.vestshop.Repository.PhieuGiamGiaRepository;
import com.vestshop.Repository.PhuongThucThanhToanRepository;
import com.vestshop.Repository.SanPhamChiTietRepository;
import com.vestshop.Service.OnlineCheckoutService;
import com.vestshop.dto.request.ConfirmPaymentRequest;
import com.vestshop.dto.request.OnlineCheckoutItemRequest;
import com.vestshop.dto.request.OnlineCheckoutRequest;
import com.vestshop.dto.response.ApiMessageResponse;
import com.vestshop.dto.response.OnlineCheckoutResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;

@Service
public class OnlineCheckoutServiceImpl implements OnlineCheckoutService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final GiaoDichThanhToanRepository giaoDichThanhToanRepository;
    private final LichSuThanhToanRepository lichSuThanhToanRepository;
    private final KhachHangRepository khachHangRepository;
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final PhuongThucThanhToanRepository phuongThucThanhToanRepository;

    public OnlineCheckoutServiceImpl(HoaDonRepository hoaDonRepository,
                                     HoaDonChiTietRepository hoaDonChiTietRepository,
                                     SanPhamChiTietRepository sanPhamChiTietRepository,
                                     GiaoDichThanhToanRepository giaoDichThanhToanRepository,
                                     LichSuThanhToanRepository lichSuThanhToanRepository,
                                     KhachHangRepository khachHangRepository,
                                     PhieuGiamGiaRepository phieuGiamGiaRepository,
                                     PhuongThucThanhToanRepository phuongThucThanhToanRepository) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.sanPhamChiTietRepository = sanPhamChiTietRepository;
        this.giaoDichThanhToanRepository = giaoDichThanhToanRepository;
        this.lichSuThanhToanRepository = lichSuThanhToanRepository;
        this.khachHangRepository = khachHangRepository;
        this.phieuGiamGiaRepository = phieuGiamGiaRepository;
        this.phuongThucThanhToanRepository = phuongThucThanhToanRepository;
    }

    @Override
    @Transactional
    public OnlineCheckoutResponse checkout(OnlineCheckoutRequest request) {
        validateRequest(request);

        String paymentMethod = normalizePaymentMethod(request.getPaymentMethod());

        KhachHang khachHang = null;
        if (request.getIdKhachHang() != null) {
            khachHang = khachHangRepository.findById(request.getIdKhachHang())
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy khách hàng"));
        }

        PhieuGiamGia phieuGiamGia = null;
        if (request.getIdPhieuGiamGia() != null) {
            phieuGiamGia = phieuGiamGiaRepository.findById(request.getIdPhieuGiamGia())
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy phiếu giảm giá"));
        }

        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon(generateMaHoaDon(request.getMaHoaDon()));
        hoaDon.setLoaiDon(Boolean.TRUE);
        hoaDon.setTrangThai(true);

        hoaDon.setKhachHang(khachHang);
        hoaDon.setNhanVien(null);
        hoaDon.setPhieuGiamGia(phieuGiamGia);

        hoaDon.setPhiVanChuyen(defaultBigDecimal(request.getPhiVanChuyen()));
        hoaDon.setTongTien(defaultBigDecimal(request.getTongTien()));
        hoaDon.setTongTienGiam(defaultBigDecimal(request.getTongTienGiam()));
        hoaDon.setTongTienSauGiam(defaultBigDecimal(request.getTongTienSauGiam()));

        hoaDon.setTenKhachHang(trim(request.getTenKhachHang()));
        hoaDon.setSoDienThoai(trim(request.getSoDienThoai()));
        hoaDon.setEmailKhachHang(trim(request.getEmailKhachHang()));
        hoaDon.setDiaChiKhachHang(trim(request.getDiaChiKhachHang()));
        hoaDon.setGhiChu(trim(request.getGhiChu()));

        hoaDon.setTenNguoiNhanHang(trim(request.getTenNguoiNhanHang()));
        hoaDon.setSoDienThoaiNhanHang(trim(request.getSoDienThoaiNhanHang()));
        hoaDon.setDiaChiNhanHangChiTiet(trim(request.getDiaChiNhanHangChiTiet()));
        hoaDon.setPhuongXaNhanHang(trim(request.getPhuongXaNhanHang()));
        hoaDon.setQuanHuyenNhanHang(trim(request.getQuanHuyenNhanHang()));
        hoaDon.setTinhThanhNhanHang(trim(request.getTinhThanhNhanHang()));

        hoaDon.setNgayTao(LocalDateTime.now());

        if ("COD".equals(paymentMethod)) {
            hoaDon.setTrangThaiDon(0);
        } else {
            hoaDon.setTrangThaiDon(1);
        }

        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

        for (OnlineCheckoutItemRequest item : request.getItems()) {
            SanPhamChiTiet spct = sanPhamChiTietRepository.findById(item.getIdSanPhamChiTiet())
                    .orElseThrow(() -> new BadRequestException(
                            "Không tìm thấy sản phẩm chi tiết id = " + item.getIdSanPhamChiTiet()
                    ));

            int soLuongDat = Objects.requireNonNullElse(item.getSoLuong(), 0);
            int tonKho = Objects.requireNonNullElse(spct.getSoLuongTon(), 0);

            if (soLuongDat <= 0) {
                throw new BadRequestException("Số lượng sản phẩm không hợp lệ");
            }

            if (tonKho < soLuongDat) {
                throw new BadRequestException("Sản phẩm không đủ tồn kho: " + spct.getMaSanPhamChiTiet());
            }

            // Trừ tồn kho ngay khi tạo đơn
            spct.setSoLuongTon(tonKho - soLuongDat);
            spct.setNgayCapNhat(LocalDateTime.now());
            sanPhamChiTietRepository.save(spct);

            HoaDonChiTiet ct = new HoaDonChiTiet();
            ct.setHoaDon(savedHoaDon);
            ct.setSanPhamChiTiet(spct);
            ct.setSoLuong(soLuongDat);
            ct.setNgayTao(LocalDateTime.now());
            ct.setTrangThai(true);

            hoaDonChiTietRepository.save(ct);
        }

        OnlineCheckoutResponse response = new OnlineCheckoutResponse();
        response.setSuccess(true);
        response.setOrderId(savedHoaDon.getId());
        response.setMaHoaDon(savedHoaDon.getMaHoaDon());
        response.setTrangThaiDon(savedHoaDon.getTrangThaiDon());
        response.setPaymentMethod(paymentMethod);

        if ("COD".equals(paymentMethod)) {
            response.setMessage("Đặt hàng thành công");
            response.setPaymentStatus("UNPAID");
        } else {
            createPendingQrPayment(savedHoaDon, request);

            response.setMessage("Tạo đơn hàng QR thành công");
            response.setPaymentStatus("PENDING");
            response.setBankName("Techcombank");
            response.setBankAccountName("LE QUANG HUY");
            response.setBankAccountNo("19039138168012");
            response.setTransferContent("VEST " + savedHoaDon.getMaHoaDon());
            response.setAmount(savedHoaDon.getTongTienSauGiam());
            response.setQrImageUrl("/images/static-qr-techcombank.png");
        }

        return response;
    }

    @Override
    @Transactional
    public ApiMessageResponse confirmQrPayment(Long orderId, ConfirmPaymentRequest request) {
        HoaDon hoaDon = hoaDonRepository.findById(orderId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy hóa đơn"));

        GiaoDichThanhToan gdtt = giaoDichThanhToanRepository
                .findFirstByHoaDon_IdOrderByIdDesc(orderId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy giao dịch thanh toán đang chờ"));

        if (Boolean.TRUE.equals(gdtt.getTrangThai())) {
            return new ApiMessageResponse(true, "Đơn hàng đã được xác nhận thanh toán trước đó");
        }

        BigDecimal soTien = request.getSoTien() != null
                ? request.getSoTien()
                : hoaDon.getTongTienSauGiam();

        gdtt.setSoTien(soTien);
        gdtt.setMaGiaoDich(trim(request.getMaGiaoDich()));
        gdtt.setDuLieuPhanHoi(trim(request.getGhiChu()));
        gdtt.setTrangThai(true);
        gdtt.setThoiGianCapNhat(LocalDateTime.now());

        giaoDichThanhToanRepository.save(gdtt);

        LichSuThanhToan lichSu = new LichSuThanhToan();
        lichSu.setHoaDon(hoaDon);
        lichSu.setMaGiaoDich(trim(request.getMaGiaoDich()));
        lichSu.setSoTien(soTien);
        lichSu.setNgayThanhToan(LocalDateTime.now());
        lichSu.setHinhThucThanhToan("QR");
        lichSu.setGhiChu(trim(request.getGhiChu()));
        lichSu.setTrangThai(true);

        lichSuThanhToanRepository.save(lichSu);

        hoaDon.setTrangThaiDon(0);
        hoaDonRepository.save(hoaDon);

        return new ApiMessageResponse(true, "Xác nhận thanh toán QR thành công");
    }

    private void createPendingQrPayment(HoaDon hoaDon, OnlineCheckoutRequest request) {
        PhuongThucThanhToan phuongThucThanhToan = phuongThucThanhToanRepository
                .findFirstByMaPhuongThucThanhToanIgnoreCaseAndTrangThaiTrue("QR")
                .orElseGet(() -> phuongThucThanhToanRepository
                        .findFirstByMaPhuongThucThanhToanIgnoreCaseAndTrangThaiTrue("CK")
                        .orElseGet(() -> phuongThucThanhToanRepository
                                .findFirstByHinhThucAndTrangThaiTrue(2)
                                .orElseThrow(() -> new BadRequestException("Không tìm thấy phương thức thanh toán QR/Chuyển khoản"))));

        GiaoDichThanhToan gdtt = new GiaoDichThanhToan();
        gdtt.setHoaDon(hoaDon);
        gdtt.setPhuongThucThanhToan(phuongThucThanhToan);
        gdtt.setSoTien(hoaDon.getTongTienSauGiam());
        gdtt.setMaGiaoDich(trim(request.getMaGiaoDich()));
        gdtt.setGhiChu(trim(request.getGhiChuThanhToan()));
        gdtt.setThoiGianTao(LocalDateTime.now());
        gdtt.setThoiGianCapNhat(LocalDateTime.now());
        gdtt.setTrangThai(false);

        giaoDichThanhToanRepository.save(gdtt);
    }

    private void validateRequest(OnlineCheckoutRequest request) {
        if (request == null) {
            throw new BadRequestException("Request không được để trống");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Giỏ hàng trống");
        }

        if (isBlank(request.getTenKhachHang())) {
            throw new BadRequestException("Tên khách hàng không được để trống");
        }

        if (isBlank(request.getSoDienThoai())) {
            throw new BadRequestException("Số điện thoại không được để trống");
        }

        if (Boolean.TRUE.equals(request.getLoaiDon())) {
            if (isBlank(request.getTenNguoiNhanHang())) {
                throw new BadRequestException("Tên người nhận không được để trống");
            }

            if (isBlank(request.getSoDienThoaiNhanHang())) {
                throw new BadRequestException("SĐT người nhận không được để trống");
            }

            if (isBlank(request.getTinhThanhNhanHang())) {
                throw new BadRequestException("Tỉnh/Thành không được để trống");
            }

            if (isBlank(request.getPhuongXaNhanHang())) {
                throw new BadRequestException("Phường/Xã không được để trống");
            }

            if (isBlank(request.getDiaChiNhanHangChiTiet())) {
                throw new BadRequestException("Địa chỉ nhận hàng chi tiết không được để trống");
            }
        }

        for (OnlineCheckoutItemRequest item : request.getItems()) {
            if (item.getIdSanPhamChiTiet() == null) {
                throw new BadRequestException("Thiếu idSanPhamChiTiet");
            }

            if (item.getSoLuong() == null || item.getSoLuong() <= 0) {
                throw new BadRequestException("Số lượng sản phẩm không hợp lệ");
            }
        }
    }

    private String normalizePaymentMethod(String paymentMethod) {
        String value = trim(paymentMethod);
        if (value == null || value.isEmpty()) {
            return "COD";
        }

        value = value.toUpperCase();

        if ("COD".equals(value)) {
            return "COD";
        }

        if ("QR".equals(value) || "BANK".equals(value) || "BANKING".equals(value)) {
            return "QR";
        }

        return value;
    }

    private BigDecimal defaultBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String generateMaHoaDon(String maHoaDon) {
        if (!isBlank(maHoaDon)) {
            return maHoaDon.trim();
        }

        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        return "HD" + datePart + random5Digits();
    }

    private String random5Digits() {
        int value = ThreadLocalRandom.current().nextInt(10000, 100000);
        return String.valueOf(value);
    }
}