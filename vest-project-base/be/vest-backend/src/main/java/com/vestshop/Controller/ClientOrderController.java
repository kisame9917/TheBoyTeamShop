package com.vestshop.Controller;

import com.vestshop.Entity.GiaoDichThanhToan;
import com.vestshop.Entity.HoaDon;
import com.vestshop.Entity.KhachHang;
import com.vestshop.Entity.SanPhamChiTiet;
import com.vestshop.Exception.ApiException;
import com.vestshop.Repository.GiaoDichThanhToanRepository;
import com.vestshop.Repository.HoaDonChiTietRepository;
import com.vestshop.Repository.HoaDonRepository;
import com.vestshop.Repository.KhachHangRepository;
import com.vestshop.common.TrangThaiDonHang;
import com.vestshop.dto.response.ClientMyOrderSummaryResponse;
import com.vestshop.dto.response.OnlineOrderLookupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.vestshop.Service.ClientOrderMutationService;
import com.vestshop.dto.request.ClientOrderCancelRequest;
import com.vestshop.dto.request.ClientOrderUpdateShippingRequest;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/client/orders")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientOrderController {

    private final KhachHangRepository khachHangRepository;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final GiaoDichThanhToanRepository giaoDichThanhToanRepository;
    private final ClientOrderMutationService clientOrderMutationService;

    @GetMapping("/my")
    public ResponseEntity<List<ClientMyOrderSummaryResponse>> myOrders(Authentication authentication) {
        KhachHang khachHang = getCurrentCustomer(authentication);

        List<ClientMyOrderSummaryResponse> result = hoaDonRepository
                .findAllByKhachHang_IdAndLoaiDonTrueOrderByNgayTaoDesc(khachHang.getId())
                .stream()
                .map(this::toSummary)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/my/{id}")
    public ResponseEntity<OnlineOrderLookupResponse> myOrderDetail(
            @PathVariable Long id,
            Authentication authentication
    ) {
        KhachHang khachHang = getCurrentCustomer(authentication);

        HoaDon hoaDon = hoaDonRepository
                .findByIdAndKhachHang_IdAndLoaiDonTrue(id, khachHang.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));

        return ResponseEntity.ok(toDetail(hoaDon));
    }
    @PostMapping("/my/{id}/cancel")
    @Transactional
    public ResponseEntity<OnlineOrderLookupResponse> cancelMyOrder(@PathVariable Long id,
                                                                   @RequestBody ClientOrderCancelRequest request,
                                                                   Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Bạn chưa đăng nhập");
        }

        return ResponseEntity.ok(
                clientOrderMutationService.cancelMyOrder(id, authentication.getName(), request)
        );
    }

    @PatchMapping("/my/{id}/shipping-info")
    @Transactional
    public ResponseEntity<OnlineOrderLookupResponse> updateMyOrderShipping(@PathVariable Long id,
                                                                           @RequestBody ClientOrderUpdateShippingRequest request,
                                                                           Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Bạn chưa đăng nhập");
        }

        return ResponseEntity.ok(
                clientOrderMutationService.updateMyOrderShipping(id, authentication.getName(), request)
        );
    }

    private KhachHang getCurrentCustomer(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Bạn chưa đăng nhập");
        }

        return khachHangRepository.findByTaiKhoan(authentication.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng"));
    }

    private ClientMyOrderSummaryResponse toSummary(HoaDon hoaDon) {
        GiaoDichThanhToan gdtt = giaoDichThanhToanRepository
                .findLatestWithPaymentMethod(hoaDon.getId())
                .stream()
                .findFirst()
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

        int tongSanPham = hoaDonChiTietRepository.findAllByHoaDon_Id(hoaDon.getId())
                .stream()
                .mapToInt(x -> x.getSoLuong() == null ? 0 : x.getSoLuong())
                .sum();

        TrangThaiDonHang trangThai = TrangThaiDonHang.fromCode(hoaDon.getTrangThaiDon());

        return ClientMyOrderSummaryResponse.builder()
                .id(hoaDon.getId())
                .maHoaDon(hoaDon.getMaHoaDon())
                .trangThaiDon(hoaDon.getTrangThaiDon())
                .tenTrangThaiDon(trangThai.getTen())
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .tenNguoiNhanHang(hoaDon.getTenNguoiNhanHang())
                .soDienThoaiNhanHang(hoaDon.getSoDienThoaiNhanHang())
                .tongSanPham(tongSanPham)
                .tongTienSauGiam(defaultBigDecimal(hoaDon.getTongTienSauGiam()))
                .ngayTao(hoaDon.getNgayTao())
                .build();
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

    private BigDecimal defaultBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}