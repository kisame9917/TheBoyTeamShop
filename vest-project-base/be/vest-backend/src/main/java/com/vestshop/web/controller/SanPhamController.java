package com.vestshop.web.controller;

import com.vestshop.common.ApiResponse;
import com.vestshop.service.SanPhamService;
import com.vestshop.web.dto.SanPhamCreateRequest;
import com.vestshop.web.dto.SanPhamResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
public class SanPhamController {

    private final SanPhamService sanPhamService;

    @GetMapping
    public ApiResponse<List<SanPhamResponse>> list() {
        return ApiResponse.ok(sanPhamService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<SanPhamResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(sanPhamService.findById(id));
    }

    @PostMapping
    public ApiResponse<SanPhamResponse> create(@Valid @RequestBody SanPhamCreateRequest request) {
        return ApiResponse.ok(sanPhamService.create(request));
    }
}
