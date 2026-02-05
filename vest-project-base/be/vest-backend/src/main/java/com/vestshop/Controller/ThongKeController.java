package com.vestshop.Controller;

import com.vestshop.Service.ThongKeService;
import com.vestshop.common.ApiResponse;
import com.vestshop.dto.response.DoanhThuResponse;
import com.vestshop.dto.response.ThongKeDonHangResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/thong-ke")
@CrossOrigin("*")
public class ThongKeController {

    @Autowired
    private ThongKeService thongKeService;

    /**
     * Doanh thu theo:
     * - THANG: trả về list theo NGÀY trong tháng
     * - QUY  : trả về list theo THÁNG THỰC TẾ của quý (VD: T7, T8, T9)
     * - NAM  : trả về list theo THÁNG trong năm (T1..T12)
     *
     * Params:
     * - type: THANG | QUY | NAM
     * - month: required nếu THANG
     * - quarter: required nếu QUY (1..4)
     * - year: required
     */
    @GetMapping("/doanh-thu")
    public ApiResponse<List<DoanhThuResponse>> getDoanhThu(
            @RequestParam String type,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer quarter,
            @RequestParam Integer year
    ) {
        return ApiResponse.ok(thongKeService.getDoanhThu(type, month, quarter, year));
    }

    /**
     * Thống kê đơn hàng theo trạng thái (theo tháng/năm)
     */
    @GetMapping("/don-hang")
    public ApiResponse<List<ThongKeDonHangResponse>> getThongKeDonHang(
            @RequestParam(required = false) Integer month,
            @RequestParam Integer year
    ) {
        return ApiResponse.ok(thongKeService.getThongKeDonHang(month, year));
    }

    /**
     * Thống kê đơn hàng theo trạng thái trong khoảng ngày (filter from/to ở FE sẽ dùng cái này)
     */
    @GetMapping("/don-hang-range")
    public ApiResponse<List<ThongKeDonHangResponse>> getThongKeDonHangRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        return ApiResponse.ok(thongKeService.getThongKeDonHangRange(start, end));
    }

    /**
     * Giữ lại API cũ (nếu bạn còn dùng): so sánh doanh thu quý hiện tại với cùng quý năm trước
     */
    @GetMapping("/so-sanh-quy")
    public ApiResponse<?> compareQuarter() {
        return ApiResponse.ok(thongKeService.compareQuarterRevenue());
    }

    @GetMapping("/top-ban-chay")
    public ApiResponse<?> getTopSelling(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDateTime start = (from != null) ? from.atStartOfDay() : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = (to != null) ? to.atTime(LocalTime.MAX) : LocalDateTime.now();
        return ApiResponse.ok(thongKeService.getTopSelling(start, end));
    }

    @GetMapping("/ban-cham")
    public ApiResponse<?> getSlowMoving(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDateTime start = (from != null) ? from.atStartOfDay() : LocalDate.now().minusMonths(3).atStartOfDay();
        LocalDateTime end = (to != null) ? to.atTime(LocalTime.MAX) : LocalDateTime.now();
        return ApiResponse.ok(thongKeService.getSlowMoving(start, end));
    }

    @GetMapping("/khach-hang-vip")
    public ApiResponse<?> getTopCustomers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDateTime start = (from != null) ? from.atStartOfDay() : LocalDate.now().withDayOfYear(1).atStartOfDay();
        LocalDateTime end = (to != null) ? to.atTime(LocalTime.MAX) : LocalDateTime.now();
        return ApiResponse.ok(thongKeService.getTopCustomers(start, end));
    }
}
