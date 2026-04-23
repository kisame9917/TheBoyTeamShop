package com.vestshop.Service;

import com.vestshop.dto.request.*;
import com.vestshop.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface HoaDonService {

    Page<HoaDonListResponse> search(
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
    );

    HoaDonDetailResponse getDetailById(Long id);
    HoaDonDetailResponse getDetailByMaHoaDon(String maHoaDon);

    List<LichSuHoaDonResponse> getLichSuHoaDon(Long idHoaDon);
    List<LichSuThanhToanResponse> getLichSuThanhToan(Long idHoaDon);
    List<GiaoDichThanhToanResponse> getGiaoDichThanhToan(Long idHoaDon);

    HoaDonDetailResponse changeStatus(Long idHoaDon, HoaDonChangeStatusRequest req);
    HoaDonDetailResponse hoanHang(Long idHoaDon, HoaDonReturnRequest req);
//    HoaDonDetailResponse createPos(BanHangRequest req);
    TaohoadonResponse createDraft(TaoHoaDonChoXacNhanRequest req);
    HoaDonDetailResponse checkoutDraft(Long hoaDonId, BanHangRequest req);
    void cancelDraft(Long hoaDonId, CancelDraftRequest req);
    List<HoaDonDetailResponse> getPosDrafts();
    HoaDonDetailResponse syncPosDraft(Long hoaDonId, PosDraftSyncRequest req);
    HoaDonDetailResponse confirmRefund(Long hoaDonId, RefundConfirmRequest request);
    PosQrInitResponse initPosQrPayment(Long hoaDonId, PosQrInitRequest req);
    PosQrConfirmResponse confirmPosQrPayment(Long hoaDonId, PosQrConfirmRequest req);
    PosQrStatusResponse getPosQrPaymentStatus(Long hoaDonId, String requestCode);

}
