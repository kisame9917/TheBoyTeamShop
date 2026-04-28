package com.vestshop.Controller;

import com.vestshop.Service.HoaDonService;
import com.vestshop.dto.response.HoaDonDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/app-pos/orders")
@RequiredArgsConstructor
public class AppPosOrderController {

    private final HoaDonService hoaDonService;

    @GetMapping
    public ResponseEntity<List<HoaDonDetailResponse>> getActivePosOrders() {
        return ResponseEntity.ok(hoaDonService.getPosDrafts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HoaDonDetailResponse> getActivePosOrderDetail(@PathVariable Long id) {
        HoaDonDetailResponse detail = hoaDonService.getDetailById(id);

        if (detail == null
                || detail.getTrangThaiDon() == null
                || detail.getTrangThaiDon() != 0
                || !Boolean.TRUE.equals(detail.getTrangThai())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn POS đang mở");
        }

        return ResponseEntity.ok(detail);
    }
}