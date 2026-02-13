package com.vestshop.Controller;

import com.vestshop.Service.GiaoCaService;
import com.vestshop.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/giao-ca/admin")
@CrossOrigin("*")
@RequiredArgsConstructor
public class GiaoCaAdminController {

    private final GiaoCaService giaoCaService;

    /**
     * ADMIN: Danh sách doanh thu ca (mới nhất lên trên)
     * query:
     *  - keyword: tìm theo ca / nhân viên / mã phiên
     *  - idCa, idNv
     *  - fromDate, toDate (ngày làm việc)
     *  - trangThai: 1-đang mở,2-đã đóng
     */
    @GetMapping("/phien-ca")
    public ApiResponse<?> phienCa(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long idCa,
            @RequestParam(required = false) Long idNv,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Pageable pageable
    ) {
        try {
            return ApiResponse.ok(giaoCaService.adminSearch(keyword, idCa, idNv, fromDate, toDate, trangThai, pageable));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
