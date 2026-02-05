package com.vestshop.Service;

import com.vestshop.Repository.HoaDonChiTietRepository;
import com.vestshop.Repository.HoaDonRepository;
import com.vestshop.Repository.SanPhamRepository;
import com.vestshop.common.TrangThaiDonHang;
import com.vestshop.dto.response.DoanhThuResponse;
import com.vestshop.dto.response.SanPhamThongKeResponse;
import com.vestshop.dto.response.ThongKeDonHangResponse;
import com.vestshop.dto.response.TopKhachHangResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ThongKeService {

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private HoaDonChiTietRepository hdctRepo;

    @Autowired
    private SanPhamRepository sanPhamRepo;

    // ✅ Chuẩn hoá: HOAN_THANH = 4 theo enum, tránh nhầm DA_HUY = 5
    private static final int TRANG_THAI_HOAN_THANH = TrangThaiDonHang.HOAN_THANH.getCode();

    // =================================================================================
    // DOANH THU (Line Chart)
    // =================================================================================

    /**
     * type:
     * - THANG: theo ngày trong tháng
     * - QUY  : theo tháng THỰC TẾ trong quý (VD: Q3 -> T7,T8,T9)
     * - NAM  : theo tháng trong năm
     */
    public List<DoanhThuResponse> getDoanhThu(String type, Integer month, Integer quarter, Integer year) {
        if (year == null) throw new IllegalArgumentException("Thiếu tham số year");

        List<DoanhThuResponse> responses = new ArrayList<>();

        if ("THANG".equalsIgnoreCase(type)) {
            if (month == null) throw new IllegalArgumentException("Thiếu tham số month cho type=THANG");

            List<Object[]> rawData = hoaDonRepository.getDoanhThuNgayInMonth(month, year, TRANG_THAI_HOAN_THANH);

            Map<Integer, BigDecimal> mapData = rawData.stream().collect(Collectors.toMap(
                    row -> (Integer) row[0],
                    row -> row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO,
                    (a, b) -> b
            ));

            int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
            for (int d = 1; d <= daysInMonth; d++) {
                responses.add(new DoanhThuResponse(String.valueOf(d), mapData.getOrDefault(d, BigDecimal.ZERO)));
            }
            return responses;
        }

        if ("QUY".equalsIgnoreCase(type)) {
            if (quarter == null || quarter < 1 || quarter > 4) {
                throw new IllegalArgumentException("Thiếu/không hợp lệ tham số quarter (1..4) cho type=QUY");
            }

            int startMonth = (quarter - 1) * 3 + 1;   // 1,4,7,10
            int endMonth = startMonth + 2;            // 3,6,9,12

            List<Object[]> rawData = hoaDonRepository.getDoanhThuThangInQuarter(
                    year, startMonth, endMonth, TRANG_THAI_HOAN_THANH
            );

            Map<Integer, BigDecimal> mapData = rawData.stream().collect(Collectors.toMap(
                    row -> (Integer) row[0],
                    row -> row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO,
                    (a, b) -> b
            ));

            // ✅ Theo yêu cầu: hiển thị theo THÁNG THỰC TẾ của quý
            for (int m = startMonth; m <= endMonth; m++) {
                responses.add(new DoanhThuResponse("T" + m, mapData.getOrDefault(m, BigDecimal.ZERO)));
            }
            return responses;
        }

        if ("NAM".equalsIgnoreCase(type)) {
            List<Object[]> rawData = hoaDonRepository.getDoanhThuThangInYear(year, TRANG_THAI_HOAN_THANH);

            Map<Integer, BigDecimal> mapData = rawData.stream().collect(Collectors.toMap(
                    row -> (Integer) row[0],
                    row -> row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO,
                    (a, b) -> b
            ));

            for (int m = 1; m <= 12; m++) {
                responses.add(new DoanhThuResponse("T" + m, mapData.getOrDefault(m, BigDecimal.ZERO)));
            }
            return responses;
        }

        throw new IllegalArgumentException("type không hợp lệ: " + type + " (THANG|QUY|NAM)");
    }

    // =================================================================================
    // THỐNG KÊ ĐƠN HÀNG (Widget theo trạng thái)
    // =================================================================================

    public List<ThongKeDonHangResponse> getThongKeDonHang(Integer month, Integer year) {
        List<Object[]> results = hoaDonRepository.getThongKeDonHang(month, year);

        Map<Integer, Long> map = results.stream().collect(Collectors.toMap(
                row -> (Integer) row[0],
                row -> row[1] != null ? ((Number) row[1]).longValue() : 0L,
                Long::sum
        ));

        // ✅ Always return đủ trạng thái để FE render đúng
        List<ThongKeDonHangResponse> out = new ArrayList<>();
        for (TrangThaiDonHang st : TrangThaiDonHang.values()) {
            long count = map.getOrDefault(st.getCode(), 0L);

            out.add(ThongKeDonHangResponse.builder()
                    .code(st.getCode())
                    .trangThai(st.name())
                    .tenTrangThai(st.getTen())
                    .soLuong(count)
                    .kieuDang(getStyleForStatus(st))
                    .build());
        }
        return out;
    }

    public List<ThongKeDonHangResponse> getThongKeDonHangRange(LocalDateTime from, LocalDateTime to) {
        List<Object[]> results = hoaDonRepository.countDonHangByTrangThaiInRange(from, to);

        Map<Integer, Long> map = results.stream().collect(Collectors.toMap(
                row -> (Integer) row[0],
                row -> row[1] != null ? ((Number) row[1]).longValue() : 0L,
                Long::sum
        ));

        List<ThongKeDonHangResponse> out = new ArrayList<>();
        for (TrangThaiDonHang st : TrangThaiDonHang.values()) {
            long count = map.getOrDefault(st.getCode(), 0L);

            out.add(ThongKeDonHangResponse.builder()
                    .code(st.getCode())
                    .trangThai(st.name())
                    .tenTrangThai(st.getTen())
                    .soLuong(count)
                    .kieuDang(getStyleForStatus(st))
                    .build());
        }
        return out;
    }

    private String getStyleForStatus(TrangThaiDonHang st) {
        return switch (st) {
            case HOAN_THANH -> "success";
            case DA_HUY -> "danger";
            case CHO_XAC_NHAN, YEU_CAU_HOAN -> "warning";
            case DANG_XU_LY, DA_HOAN -> "info";
            case DANG_GIAO -> "primary";
            case DA_GIAO -> "secondary";
        };
    }

    // =================================================================================
    // TOP BÁN CHẠY / BÁN CHẬM / VIP
    // =================================================================================

    public List<SanPhamThongKeResponse> getTopSelling(LocalDateTime from, LocalDateTime to) {
        List<SanPhamThongKeResponse> list = hdctRepo.findTopSellingProducts(from, to, TRANG_THAI_HOAN_THANH);
        return list.size() > 10 ? list.subList(0, 10) : list;
    }

    public List<SanPhamThongKeResponse> getSlowMoving(LocalDateTime from, LocalDateTime to) {
        return sanPhamRepo.findSlowMovingProducts(from, to, TRANG_THAI_HOAN_THANH);
    }

    public List<TopKhachHangResponse> getTopCustomers(LocalDateTime from, LocalDateTime to) {
        List<TopKhachHangResponse> list = hoaDonRepository.findTopKhachHang(from, to, TRANG_THAI_HOAN_THANH);
        return list.size() > 10 ? list.subList(0, 10) : list;
    }

    // =================================================================================
    // API CŨ: so sánh doanh thu quý hiện tại vs cùng quý năm trước (giữ lại nếu cần)
    // =================================================================================

    public List<DoanhThuResponse> compareQuarterRevenue() {
        List<DoanhThuResponse> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int quarter = (now.getMonthValue() - 1) / 3 + 1;

        LocalDateTime startQNow = getStartOfQuarter(currentYear, quarter);
        LocalDateTime endQNow = getEndOfQuarter(currentYear, quarter);

        LocalDateTime startQPrev = getStartOfQuarter(currentYear - 1, quarter);
        LocalDateTime endQPrev = getEndOfQuarter(currentYear - 1, quarter);

        BigDecimal revNow = hoaDonRepository.sumDoanhThuInRange(startQNow, endQNow, TRANG_THAI_HOAN_THANH);
        BigDecimal revPrev = hoaDonRepository.sumDoanhThuInRange(startQPrev, endQPrev, TRANG_THAI_HOAN_THANH);

        revNow = (revNow == null) ? BigDecimal.ZERO : revNow;
        revPrev = (revPrev == null) ? BigDecimal.ZERO : revPrev;

        result.add(new DoanhThuResponse("Quý " + quarter + "/" + currentYear, revNow));
        result.add(new DoanhThuResponse("Quý " + quarter + "/" + (currentYear - 1), revPrev));
        return result;
    }

    private LocalDateTime getStartOfQuarter(int year, int quarter) {
        int month = (quarter - 1) * 3 + 1;
        return LocalDateTime.of(year, month, 1, 0, 0);
    }

    private LocalDateTime getEndOfQuarter(int year, int quarter) {
        int nextQuarterMonth = quarter * 3 + 1; // 4,7,10,13
        if (nextQuarterMonth > 12) {
            // cuối năm
            return LocalDateTime.of(year + 1, 1, 1, 0, 0).minusNanos(1);
        }
        return LocalDateTime.of(year, nextQuarterMonth, 1, 0, 0).minusNanos(1);
    }
}
