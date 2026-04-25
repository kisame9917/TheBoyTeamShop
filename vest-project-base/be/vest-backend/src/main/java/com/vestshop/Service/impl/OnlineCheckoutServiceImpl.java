package com.vestshop.Service.impl;

import com.vestshop.Entity.*;
import com.vestshop.Exception.BadRequestException;
import com.vestshop.Repository.*;
import com.vestshop.Service.EmailService;
import com.vestshop.Service.OnlineCheckoutService;
import com.vestshop.common.TrangThaiDonHang;
import com.vestshop.dto.request.ConfirmPaymentRequest;
import com.vestshop.dto.request.OnlineCheckoutItemRequest;
import com.vestshop.dto.request.OnlineCheckoutRequest;
import com.vestshop.dto.response.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
@Slf4j
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
    private final EmailService emailService;
    @Value("${vnpay.tmn-code}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnpHashSecret;

    @Value("${vnpay.pay-url}")
    private String vnpPayUrl;

    @Value("${vnpay.return-url}")
    private String vnpReturnUrl;
    public OnlineCheckoutServiceImpl(HoaDonRepository hoaDonRepository,
                                     HoaDonChiTietRepository hoaDonChiTietRepository,
                                     SanPhamChiTietRepository sanPhamChiTietRepository,
                                     GiaoDichThanhToanRepository giaoDichThanhToanRepository,
                                     LichSuThanhToanRepository lichSuThanhToanRepository,
                                     KhachHangRepository khachHangRepository,
                                     PhieuGiamGiaRepository phieuGiamGiaRepository,
                                     PhuongThucThanhToanRepository phuongThucThanhToanRepository,
                                     EmailService emailService) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.sanPhamChiTietRepository = sanPhamChiTietRepository;
        this.giaoDichThanhToanRepository = giaoDichThanhToanRepository;
        this.lichSuThanhToanRepository = lichSuThanhToanRepository;
        this.khachHangRepository = khachHangRepository;
        this.phieuGiamGiaRepository = phieuGiamGiaRepository;
        this.phuongThucThanhToanRepository = phuongThucThanhToanRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public OnlineCheckoutResponse checkout(OnlineCheckoutRequest request, Authentication authentication) {
        validateRequest(request);

        String paymentMethod = normalizePaymentMethod(request.getPaymentMethod());

        KhachHang khachHang = resolveCheckoutCustomer(authentication, request);

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

            // Chỉ kiểm tra tồn tại thời điểm đặt, KHÔNG trừ kho ở đây
            if (tonKho < soLuongDat) {
                throw new BadRequestException("Sản phẩm không đủ tồn kho: " + spct.getMaSanPhamChiTiet());
            }

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
        } else if ("QR".equals(paymentMethod) || "BANK_QR".equals(paymentMethod)) {
            createPendingQrPayment(savedHoaDon, request);

            response.setMessage("Tạo đơn hàng QR thành công");
            response.setPaymentStatus("PENDING");
            response.setBankName("Techcombank");
            response.setBankAccountName("LE QUANG HUY");
            response.setBankAccountNo("19039138168012");
            response.setTransferContent("VEST " + savedHoaDon.getMaHoaDon());
            response.setAmount(savedHoaDon.getTongTienSauGiam());
            response.setQrImageUrl("/images/static-qr-techcombank.png");
        } else if ("VNPAY".equals(paymentMethod) || "MOMO".equals(paymentMethod) || "CARD".equals(paymentMethod)) {
            createPendingQrPayment(savedHoaDon, request);

            response.setMessage("Tạo đơn hàng thanh toán online thành công");
            response.setPaymentStatus("PENDING");
            response.setAmount(savedHoaDon.getTongTienSauGiam());
            response.setPaymentUrl(
                    "/mock-payment"
                            + "?orderId=" + savedHoaDon.getId()
                            + "&method=" + paymentMethod
                            + "&amount=" + savedHoaDon.getTongTienSauGiam()
                            + "&maHoaDon=" + savedHoaDon.getMaHoaDon()
            );
        } else {
            createPendingQrPayment(savedHoaDon, request);

            response.setMessage("Tạo đơn hàng thanh toán online thành công");
            response.setPaymentStatus("PENDING");
            response.setAmount(savedHoaDon.getTongTienSauGiam());
            response.setPaymentUrl(
                    "http://localhost:5173/mock-payment"
                            + "?orderId=" + savedHoaDon.getId()
                            + "&method=" + paymentMethod
                            + "&amount=" + savedHoaDon.getTongTienSauGiam()
                            + "&maHoaDon=" + savedHoaDon.getMaHoaDon()
            );
        }

        if ("COD".equals(paymentMethod)) {
            sendOrderConfirmationEmailIfPossible(savedHoaDon);
        }
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public OnlineOrderLookupResponse lookupOrder(String maHoaDon, String soDienThoai) {
        String ma = trim(maHoaDon);
        String phone = normalizePhone(soDienThoai);

        if (isBlank(ma)) {
            throw new BadRequestException("Mã đơn hàng không được để trống");
        }

        if (isBlank(phone)) {
            throw new BadRequestException("Số điện thoại không được để trống");
        }

        HoaDon hoaDon = hoaDonRepository.findFirstByMaHoaDonIgnoreCase(ma)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy đơn hàng"));

        if (!Boolean.TRUE.equals(hoaDon.getLoaiDon())) {
            throw new BadRequestException("Không tìm thấy đơn hàng");
        }

        String phoneOrder = normalizePhone(hoaDon.getSoDienThoai());
        String phoneReceiver = normalizePhone(hoaDon.getSoDienThoaiNhanHang());

        boolean matched = phone.equals(phoneOrder) || phone.equals(phoneReceiver);
        if (!matched) {
            throw new BadRequestException("Mã đơn hàng hoặc số điện thoại không đúng");
        }

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
                    && gdtt.getPhuongThucThanhToan().getTenPhuongThucThanhToan() != null) {
                paymentMethod = gdtt.getPhuongThucThanhToan().getTenPhuongThucThanhToan();
            } else {
                paymentMethod = "QR";
            }

            paymentStatus = Boolean.TRUE.equals(gdtt.getTrangThai()) ? "PAID" : "PENDING";
        }

        TrangThaiDonHang trangThai = TrangThaiDonHang.fromCode(hoaDon.getTrangThaiDon());

        return OnlineOrderLookupResponse.builder()
                .id(hoaDon.getId())
                .maHoaDon(hoaDon.getMaHoaDon())
                .trangThaiDon(hoaDon.getTrangThaiDon())
                .tenTrangThaiDon(trangThai.getTen())
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

        PhuongThucThanhToan pttt = gdtt.getPhuongThucThanhToan();
        if (pttt == null) {
            pttt = phuongThucThanhToanRepository
                    .findFirstByMaPhuongThucThanhToanIgnoreCaseAndTrangThaiTrue("QR")
                    .orElseGet(() -> phuongThucThanhToanRepository
                            .findFirstByMaPhuongThucThanhToanIgnoreCaseAndTrangThaiTrue("CK")
                            .orElseGet(() -> phuongThucThanhToanRepository
                                    .findFirstByHinhThucAndTrangThaiTrue(2)
                                    .orElse(null)));
        }

        LichSuThanhToan lichSu = new LichSuThanhToan();
        lichSu.setHoaDon(hoaDon);
        lichSu.setMaGiaoDich(trim(request.getMaGiaoDich()));
        lichSu.setSoTien(soTien);
        lichSu.setNgayThanhToan(LocalDateTime.now());

        if (pttt != null) {
            lichSu.setPhuongThucThanhToan(pttt);
            lichSu.setHinhThucThanhToan(pttt.getTenPhuongThucThanhToan());
        } else {
            lichSu.setHinhThucThanhToan("QR");
        }

        lichSu.setGhiChu(trim(request.getGhiChu()));
        lichSu.setTrangThai(true);
        lichSuThanhToanRepository.save(lichSu);

        hoaDon.setTrangThaiDon(0);
        hoaDonRepository.save(hoaDon);
        sendOrderConfirmationEmailIfPossible(hoaDon);

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

        if ("QR".equals(value) || "BANK".equals(value) || "BANKING".equals(value) || "BANK_QR".equals(value)) {
            return "QR";
        }

        if ("VNPAY".equals(value)) {
            return "VNPAY";
        }

        if ("MOMO".equals(value)) {
            return "MOMO";
        }

        if ("CARD".equals(value) || "CREDIT_CARD".equals(value) || "CARD_CREDIT".equals(value)) {
            return "CARD";
        }

        return value;
    }
    private String normalizePhone(String value) {
        if (value == null) return null;
        return value.replaceAll("[^0-9]", "");
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
    private HoaDonDetailResponse buildMailDetail(HoaDon hoaDon) {
        List<HoaDonDetailResponse.Item> items = hoaDonChiTietRepository
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
                            : "Sản phẩm";

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
                                : null;
                    }

                    return HoaDonDetailResponse.Item.builder()
                            .idSanPhamChiTiet(spct != null ? spct.getId() : null)
                            .maSanPhamChiTiet(spct != null ? spct.getMaSanPhamChiTiet() : null)
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

        return HoaDonDetailResponse.builder()
                .id(hoaDon.getId())
                .maHoaDon(hoaDon.getMaHoaDon())
                .trangThaiDon(hoaDon.getTrangThaiDon())
                .tenTrangThaiDon(hoaDon.getTrangThaiDon() != null && hoaDon.getTrangThaiDon() == 0
                        ? "Chờ xác nhận"
                        : "Đang xử lý")
                .loaiDon(hoaDon.getLoaiDon())
                .phiVanChuyen(defaultBigDecimal(hoaDon.getPhiVanChuyen()))
                .tongTien(defaultBigDecimal(hoaDon.getTongTien()))
                .tongTienGiam(defaultBigDecimal(hoaDon.getTongTienGiam()))
                .tongTienSauGiam(defaultBigDecimal(hoaDon.getTongTienSauGiam()))
                .tenKhachHang(hoaDon.getTenKhachHang())
                .soDienThoai(hoaDon.getSoDienThoai())
                .diaChiKhachHang(hoaDon.getDiaChiKhachHang())
                .emailKhachHang(hoaDon.getEmailKhachHang())
                .tenNguoiNhanHang(hoaDon.getTenNguoiNhanHang())
                .soDienThoaiNhanHang(hoaDon.getSoDienThoaiNhanHang())
                .tinhThanhNhanHang(hoaDon.getTinhThanhNhanHang())
                .quanHuyenNhanHang(hoaDon.getQuanHuyenNhanHang())
                .phuongXaNhanHang(hoaDon.getPhuongXaNhanHang())
                .diaChiNhanHangChiTiet(hoaDon.getDiaChiNhanHangChiTiet())
                .ghiChu(hoaDon.getGhiChu())
                .ngayTao(hoaDon.getNgayTao())
                .items(items)
                .build();
    }
    private void sendOrderConfirmationEmailIfPossible(HoaDon hoaDon) {
        String shippingEmail = trim(hoaDon.getEmailKhachHang());
        if (isBlank(shippingEmail)) return;

        try {
            HoaDonDetailResponse detail = buildMailDetail(hoaDon);

            String shippingRecipientName = !isBlank(hoaDon.getTenNguoiNhanHang())
                    ? hoaDon.getTenNguoiNhanHang().trim()
                    : (!isBlank(hoaDon.getTenKhachHang()) ? hoaDon.getTenKhachHang().trim() : "Quý khách");

            emailService.sendShippingOrderConfirmation(
                    shippingEmail,
                    shippingRecipientName,
                    detail
            );
        } catch (Exception ex) {
            log.warn("[MAIL] Không gửi được email xác nhận đơn online {} tới {}: {}",
                    hoaDon.getMaHoaDon(), hoaDon.getEmailKhachHang(), ex.getMessage(), ex);
        }
    }
    private KhachHang resolveCheckoutCustomer(Authentication authentication, OnlineCheckoutRequest request) {
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getName() != null
                && !"anonymousUser".equalsIgnoreCase(authentication.getName())) {

            String principal = authentication.getName().trim();

            return khachHangRepository.findByTaiKhoanIgnoreCase(principal)
                    .or(() -> khachHangRepository.findByEmailIgnoreCase(principal))
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy khách hàng từ phiên đăng nhập"));
        }

        if (request.getIdKhachHang() != null) {
            return khachHangRepository.findById(request.getIdKhachHang())
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy khách hàng"));
        }

        return null;
    }
    @Override
    @Transactional(readOnly = true)
    public OnlinePaymentStatusResponse getPaymentStatus(Long orderId) {
        HoaDon hoaDon = hoaDonRepository.findById(orderId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy hóa đơn"));

        GiaoDichThanhToan gdtt = giaoDichThanhToanRepository
                .findFirstByHoaDon_IdOrderByIdDesc(orderId)
                .orElse(null);

        boolean paid = gdtt != null && Boolean.TRUE.equals(gdtt.getTrangThai());

        String paymentStatus;
        if (paid) {
            paymentStatus = "PAID";
        } else if (gdtt != null) {
            paymentStatus = "PENDING";
        } else {
            paymentStatus = "UNPAID";
        }

        return OnlinePaymentStatusResponse.builder()
                .orderId(hoaDon.getId())
                .paymentStatus(paymentStatus)
                .paid(paid)
                .message(paid ? "Thanh toán thành công" : "Chưa thanh toán")
                .maGiaoDich(gdtt != null ? gdtt.getMaGiaoDich() : null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OnlineCheckoutResponse createVnpayPaymentUrl(Long orderId) {
        HoaDon hoaDon = hoaDonRepository.findById(orderId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy hóa đơn"));

        BigDecimal amount = defaultBigDecimal(hoaDon.getTongTienSauGiam());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Số tiền thanh toán không hợp lệ");
        }

        String paymentUrl = buildVnpayPaymentUrl(hoaDon);

        OnlineCheckoutResponse response = new OnlineCheckoutResponse();
        response.setSuccess(true);
        response.setMessage("Tạo link VNPAY thành công");
        response.setOrderId(hoaDon.getId());
        response.setMaHoaDon(hoaDon.getMaHoaDon());
        response.setTrangThaiDon(hoaDon.getTrangThaiDon());
        response.setPaymentMethod("VNPAY");
        response.setPaymentStatus("PENDING");
        response.setAmount(amount);
        response.setPaymentUrl(paymentUrl);

        return response;
    }

    private String buildQuery(Map<String, String> params, boolean encodeValue) {
        return params.entrySet().stream()
                .filter(e -> StringUtils.hasText(e.getValue()))
                .map(e -> e.getKey() + "=" + (encodeValue ? urlEncode(e.getValue()) : e.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }


    private String buildVnpayPaymentUrl(HoaDon hoaDon) {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        Calendar calendar = Calendar.getInstance(timeZone);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(timeZone);

        String createDate = formatter.format(calendar.getTime());
        calendar.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(calendar.getTime());

        long amount = defaultBigDecimal(hoaDon.getTongTienSauGiam()).longValue() * 100L;

        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", String.valueOf(hoaDon.getId()));
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang " + hoaDon.getMaHoaDon());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnpReturnUrl);
        vnpParams.put("vnp_IpAddr", "127.0.0.1");
        vnpParams.put("vnp_CreateDate", createDate);
        vnpParams.put("vnp_ExpireDate", expireDate);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();

            if (fieldValue != null && !fieldValue.isEmpty()) {
                String encodedFieldName = URLEncoder.encode(fieldName, StandardCharsets.US_ASCII);
                String encodedFieldValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII);

                if (hashData.length() > 0) {
                    hashData.append('&');
                    query.append('&');
                }

                hashData.append(encodedFieldName).append('=').append(encodedFieldValue);
                query.append(encodedFieldName).append('=').append(encodedFieldValue);
            }
        }

        String secureHash = hmacSHA512(vnpHashSecret, hashData.toString());

        return vnpPayUrl + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA512"
            );
            mac.init(secretKeySpec);
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                hash.append(String.format("%02x", b & 0xff));
            }
            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo chữ ký VNPAY", e);
        }
    }
}