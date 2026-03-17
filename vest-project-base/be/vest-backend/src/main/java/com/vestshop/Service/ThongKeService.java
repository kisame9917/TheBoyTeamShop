package com.vestshop.Service;

import com.vestshop.Repository.HoaDonChiTietRepository;
import com.vestshop.Repository.HoaDonRepository;
import com.vestshop.Repository.SanPhamRepository;
import com.vestshop.common.TrangThaiDonHang;
import com.vestshop.dto.response.DoanhThuResponse;
import com.vestshop.dto.response.SanPhamThongKeResponse;
import com.vestshop.dto.response.ThongKeDonHangResponse;
import com.vestshop.dto.response.ThongKeTongQuanCardResponse;
import com.vestshop.dto.response.ThongKeTongQuanResponse;
import com.vestshop.dto.response.TopKhachHangResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ThongKeService {

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    private static final int TRANG_THAI_HOAN_THANH = TrangThaiDonHang.HOAN_THANH.getCode();
    private static final int TRANG_THAI_DA_HUY = TrangThaiDonHang.DA_HUY.getCode();

    // "Xử lý" = tất cả trạng thái chưa kết thúc
    private static final List<Integer> TRANG_THAI_KET_THUC = Arrays.asList(
            TrangThaiDonHang.HOAN_THANH.getCode(),
            TrangThaiDonHang.DA_HUY.getCode(),
            TrangThaiDonHang.DA_HOAN.getCode()
    );

    // =========================================================
    // TỔNG QUAN ĐẦU TRANG
    // =========================================================

    public ThongKeTongQuanResponse getTongQuanDauTrang() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        LocalDateTime startToday = today.atStartOfDay();
        LocalDateTime startWeek = today.minusDays(today.getDayOfWeek().getValue() - 1L).atStartOfDay();
        LocalDateTime startMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime startYear = today.withDayOfYear(1).atStartOfDay();

        return ThongKeTongQuanResponse.builder()
                .homNay(buildTongQuanCard("Hôm nay", startToday, now))
                .tuanNay(buildTongQuanCard("Tuần này", startWeek, now))
                .thangNay(buildTongQuanCard("Tháng này", startMonth, now))
                .namNay(buildTongQuanCard("Năm nay", startYear, now))
                .build();
    }

    private ThongKeTongQuanCardResponse buildTongQuanCard(String nhan, LocalDateTime from, LocalDateTime to) {
        BigDecimal doanhThu = defaultBigDecimal(
                hoaDonRepository.sumDoanhThuInRange(from, to, TRANG_THAI_HOAN_THANH)
        );

        Long sanPhamDaBan = defaultLong(
                hoaDonChiTietRepository.sumSoldQuantityInRangeV2(from, to, TRANG_THAI_HOAN_THANH)
        );

        Long donHang = defaultLong(
                hoaDonRepository.countAllDonHangInRange(from, to)
        );

        Long hoanThanh = defaultLong(
                hoaDonRepository.countDonHangByStatusInRange(from, to, TRANG_THAI_HOAN_THANH)
        );

        Long huy = defaultLong(
                hoaDonRepository.countDonHangByStatusInRange(from, to, TRANG_THAI_DA_HUY)
        );

        Long xuLy = defaultLong(
                hoaDonRepository.countDonHangNotInStatusesInRange(from, to, TRANG_THAI_KET_THUC)
        );

        return ThongKeTongQuanCardResponse.builder()
                .nhan(nhan)
                .doanhThu(doanhThu)
                .sanPhamDaBan(sanPhamDaBan)
                .donHang(donHang)
                .hoanThanh(hoanThanh)
                .huy(huy)
                .xuLy(xuLy)
                .build();
    }

    private BigDecimal defaultBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private Long defaultLong(Long value) {
        return value != null ? value : 0L;
    }

    // =========================================================
    // DOANH THU
    // =========================================================

    public List<DoanhThuResponse> getDoanhThu(String type, Integer month, Integer quarter, Integer year) {
        if (year == null) {
            throw new IllegalArgumentException("Thiếu tham số year");
        }

        List<DoanhThuResponse> responses = new ArrayList<>();

        if ("THANG".equalsIgnoreCase(type)) {
            if (month == null) {
                throw new IllegalArgumentException("Thiếu tham số month cho type=THANG");
            }

            List<Object[]> rawData = hoaDonRepository.getDoanhThuNgayInMonth(month, year, TRANG_THAI_HOAN_THANH);

            Map<Integer, BigDecimal> mapData = rawData.stream().collect(Collectors.toMap(
                    row -> ((Number) row[0]).intValue(),
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

            int startMonth = (quarter - 1) * 3 + 1;
            int endMonth = startMonth + 2;

            List<Object[]> rawData = hoaDonRepository.getDoanhThuThangInQuarter(
                    year, startMonth, endMonth, TRANG_THAI_HOAN_THANH
            );

            Map<Integer, BigDecimal> mapData = rawData.stream().collect(Collectors.toMap(
                    row -> ((Number) row[0]).intValue(),
                    row -> row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO,
                    (a, b) -> b
            ));

            for (int m = startMonth; m <= endMonth; m++) {
                responses.add(new DoanhThuResponse("T" + m, mapData.getOrDefault(m, BigDecimal.ZERO)));
            }
            return responses;
        }

        if ("NAM".equalsIgnoreCase(type)) {
            List<Object[]> rawData = hoaDonRepository.getDoanhThuThangInYear(year, TRANG_THAI_HOAN_THANH);

            Map<Integer, BigDecimal> mapData = rawData.stream().collect(Collectors.toMap(
                    row -> ((Number) row[0]).intValue(),
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

    // =========================================================
    // THỐNG KÊ ĐƠN HÀNG
    // =========================================================

    public List<ThongKeDonHangResponse> getThongKeDonHang(Integer month, Integer year) {
        List<Object[]> results = hoaDonRepository.getThongKeDonHang(month, year);

        Map<Integer, Long> map = results.stream().collect(Collectors.toMap(
                row -> ((Number) row[0]).intValue(),
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

    public List<ThongKeDonHangResponse> getThongKeDonHangRange(LocalDateTime from, LocalDateTime to) {
        List<Object[]> results = hoaDonRepository.countDonHangByTrangThaiInRange(from, to);

        Map<Integer, Long> map = results.stream().collect(Collectors.toMap(
                row -> ((Number) row[0]).intValue(),
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
            case DANG_GIAO, DA_XAC_NHAN -> "primary";
            case DA_GIAO -> "secondary";
            default -> "secondary";
        };
    }

    // =========================================================
    // TOP BÁN CHẠY / BÁN CHẬM / KHÁCH VIP
    // =========================================================

    public List<SanPhamThongKeResponse> getTopSelling(LocalDateTime from, LocalDateTime to) {
        List<SanPhamThongKeResponse> list =
                hoaDonChiTietRepository.findTopSellingProducts(from, to, TRANG_THAI_HOAN_THANH);
        return list.size() > 10 ? list.subList(0, 10) : list;
    }

    public List<SanPhamThongKeResponse> getSlowMoving(LocalDateTime from, LocalDateTime to) {
        return sanPhamRepository.findSlowMovingProducts(from, to, TRANG_THAI_HOAN_THANH);
    }

    public List<TopKhachHangResponse> getTopCustomers(LocalDateTime from, LocalDateTime to) {
        List<TopKhachHangResponse> list =
                hoaDonRepository.findTopKhachHang(from, to, TRANG_THAI_HOAN_THANH);
        return list.size() > 10 ? list.subList(0, 10) : list;
    }

    // =========================================================
    // SO SÁNH QUÝ
    // =========================================================

    public List<DoanhThuResponse> compareQuarterRevenue() {
        List<DoanhThuResponse> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int quarter = (now.getMonthValue() - 1) / 3 + 1;

        LocalDateTime startQNow = getStartOfQuarter(currentYear, quarter);
        LocalDateTime endQNow = getEndOfQuarter(currentYear, quarter);

        LocalDateTime startQPrev = getStartOfQuarter(currentYear - 1, quarter);
        LocalDateTime endQPrev = getEndOfQuarter(currentYear - 1, quarter);

        BigDecimal revNow = defaultBigDecimal(
                hoaDonRepository.sumDoanhThuInRange(startQNow, endQNow, TRANG_THAI_HOAN_THANH)
        );
        BigDecimal revPrev = defaultBigDecimal(
                hoaDonRepository.sumDoanhThuInRange(startQPrev, endQPrev, TRANG_THAI_HOAN_THANH)
        );

        result.add(new DoanhThuResponse("Quý " + quarter + "/" + currentYear, revNow));
        result.add(new DoanhThuResponse("Quý " + quarter + "/" + (currentYear - 1), revPrev));
        return result;
    }

    private LocalDateTime getStartOfQuarter(int year, int quarter) {
        int month = (quarter - 1) * 3 + 1;
        return LocalDateTime.of(year, month, 1, 0, 0);
    }

    private LocalDateTime getEndOfQuarter(int year, int quarter) {
        int nextQuarterMonth = quarter * 3 + 1;
        if (nextQuarterMonth > 12) {
            return LocalDateTime.of(year + 1, 1, 1, 0, 0).minusNanos(1);
        }
        return LocalDateTime.of(year, nextQuarterMonth, 1, 0, 0).minusNanos(1);
    }
}