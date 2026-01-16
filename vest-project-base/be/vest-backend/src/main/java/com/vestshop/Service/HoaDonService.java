package com.vestshop.Service;

import com.vestshop.dto.request.HoaDonChangeStatusRequest;
import com.vestshop.dto.request.HoaDonReturnRequest;
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
}
