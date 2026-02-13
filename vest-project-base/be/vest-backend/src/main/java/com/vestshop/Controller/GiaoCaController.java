package com.vestshop.Controller;

import com.vestshop.Service.GiaoCaService;
import com.vestshop.common.ApiResponse;
import com.vestshop.dto.request.DongCaRequest;
import com.vestshop.dto.request.MoCaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/giao-ca")
@CrossOrigin("*")
@RequiredArgsConstructor
public class GiaoCaController {

    private final GiaoCaService giaoCaService;

    /**
     * STAFF/ADMIN: Khi đăng nhập gọi endpoint này để biết:
     * - đang có ca mở hay không
     * - nếu có lịch phân công: có được mở ca không (đúng giờ)
     * - nếu không có lịch phân công: cho phép mở ca tự do
     */
    @GetMapping("/check-in")
    public ApiResponse<?> checkIn() {
        try {
            return ApiResponse.ok(giaoCaService.checkIn());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * STAFF/ADMIN: Mở ca
     * - req.moTuDo=true: mở ca tự do
     * - req.moTuDo=false/null: mở theo ca phân công đang diễn ra
     */
    @PostMapping("/mo")
    public ApiResponse<?> moCa(@RequestBody(required = false) MoCaRequest req) {
        try {
            return ApiResponse.ok(giaoCaService.moCa(req));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * STAFF/ADMIN: Lấy ca hiện tại (đang mở)
     */
    @GetMapping("/hien-tai")
    public ApiResponse<?> hienTai() {
        try {
            return ApiResponse.ok(giaoCaService.phienHienTai());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * STAFF/ADMIN: Đóng ca
     */
    @PostMapping("/dong")
    public ApiResponse<?> dongCa(@RequestBody(required = false) DongCaRequest req) {
        try {
            return ApiResponse.ok(giaoCaService.dongCa(req));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
