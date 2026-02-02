package com.vestshop.Controller;

import com.vestshop.Service.ThongKeService;
import com.vestshop.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/thong-ke")
@CrossOrigin("*")
public class ThongKeController {

    @Autowired
    private ThongKeService thongKeService;

    @GetMapping("/so-sanh-quy")
    public ApiResponse<?> compareQuarter() {
        // ✅ Dùng hàm ok() có sẵn của bạn, nó sẽ tự set success=true, message="OK", data=...
        return ApiResponse.ok(thongKeService.compareQuarterRevenue());
    }

    @GetMapping("/top-ban-chay")
    public ApiResponse<?> getTopSelling(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDateTime start = (from != null) ? from.atStartOfDay() : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = (to != null) ? to.atTime(LocalTime.MAX) : LocalDateTime.now();

        return ApiResponse.ok(thongKeService.getTopSelling(start, end));
    }

    @GetMapping("/ban-cham")
    public ApiResponse<?> getSlowMoving(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDateTime start = (from != null) ? from.atStartOfDay() : LocalDate.now().minusMonths(3).atStartOfDay();
        LocalDateTime end = (to != null) ? to.atTime(LocalTime.MAX) : LocalDateTime.now();

        return ApiResponse.ok(thongKeService.getSlowMoving(start, end));
    }

    @GetMapping("/khach-hang-vip")
    public ApiResponse<?> getTopCustomers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDateTime start = (from != null) ? from.atStartOfDay() : LocalDate.now().withDayOfYear(1).atStartOfDay();
        LocalDateTime end = (to != null) ? to.atTime(LocalTime.MAX) : LocalDateTime.now();

        return ApiResponse.ok(thongKeService.getTopCustomers(start, end));
    }
}