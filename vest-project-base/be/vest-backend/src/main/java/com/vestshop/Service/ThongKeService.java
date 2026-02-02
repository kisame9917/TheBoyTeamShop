package com.vestshop.Service;

import com.vestshop.Repository.*;
import com.vestshop.dto.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ThongKeService {

    @Autowired private HoaDonRepository hoaDonRepo;
    @Autowired private HoaDonChiTietRepository hdctRepo;
    @Autowired private SanPhamRepository sanPhamRepo;

    // QUAN TRỌNG: Định nghĩa mã trạng thái đơn hàng thành công ở đây
    // Bạn kiểm tra DB xem trạng thái 'Hoàn thành' là số mấy (thường là 5 hoặc 3)
    private static final int TRANG_THAI_HOAN_THANH = 5;

    public List<DoanhThuResponse> compareQuarterRevenue() {
        List<DoanhThuResponse> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int quarter = (now.getMonthValue() - 1) / 3 + 1;

        LocalDateTime startQ_Now = getStartOfQuarter(currentYear, quarter);
        LocalDateTime endQ_Now = getEndOfQuarter(currentYear, quarter);

        LocalDateTime startQ_Prev = getStartOfQuarter(currentYear - 1, quarter);
        LocalDateTime endQ_Prev = getEndOfQuarter(currentYear - 1, quarter);

        // Truyền tham số trạng thái vào đây
        BigDecimal revNow = hoaDonRepo.sumDoanhThuInRange(startQ_Now, endQ_Now, TRANG_THAI_HOAN_THANH);
        BigDecimal revPrev = hoaDonRepo.sumDoanhThuInRange(startQ_Prev, endQ_Prev, TRANG_THAI_HOAN_THANH);

        result.add(new DoanhThuResponse("Quý " + quarter + "/" + currentYear, revNow));
        result.add(new DoanhThuResponse("Quý " + quarter + "/" + (currentYear - 1), revPrev));

        return result;
    }

    public List<SanPhamThongKeResponse> getTopSelling(LocalDateTime from, LocalDateTime to) {
        // Truyền tham số trạng thái
        List<SanPhamThongKeResponse> list = hdctRepo.findTopSellingProducts(from, to, TRANG_THAI_HOAN_THANH);
        return list.size() > 10 ? list.subList(0, 10) : list;
    }

    public List<SanPhamThongKeResponse> getSlowMoving(LocalDateTime from, LocalDateTime to) {
        // Truyền tham số trạng thái
        return sanPhamRepo.findSlowMovingProducts(from, to, TRANG_THAI_HOAN_THANH);
    }

    public List<TopKhachHangResponse> getTopCustomers(LocalDateTime from, LocalDateTime to) {
        // Truyền tham số trạng thái
        List<TopKhachHangResponse> list = hoaDonRepo.findTopKhachHang(from, to, TRANG_THAI_HOAN_THANH);
        return list.size() > 10 ? list.subList(0, 10) : list;
    }

    private LocalDateTime getStartOfQuarter(int year, int quarter) {
        int month = (quarter - 1) * 3 + 1;
        return LocalDateTime.of(year, month, 1, 0, 0);
    }

    private LocalDateTime getEndOfQuarter(int year, int quarter) {
        int month = (quarter * 3) + 1;
        if (month > 12) return LocalDateTime.of(year + 1, 1, 1, 23, 59, 59).minusDays(1);
        return LocalDateTime.of(year, month, 1, 23, 59, 59).minusDays(1);
    }
}