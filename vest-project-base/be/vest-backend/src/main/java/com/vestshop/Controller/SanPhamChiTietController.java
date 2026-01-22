package com.vestshop.Controller;

import com.vestshop.Service.SanPhamChiTietService;
import com.vestshop.dto.request.SanPhamChiTietRequest;
import com.vestshop.dto.response.SanPhamChiTietResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@RestController
@RequestMapping("/api/san-pham-chi-tiet")
@RequiredArgsConstructor
public class SanPhamChiTietController {

    private final SanPhamChiTietService service;

    @GetMapping
    public ResponseEntity<Page<SanPhamChiTietResponse>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getAll(PageRequest.of(page, size)));
    }

    @GetMapping("/by-product/{productId}")
    public ResponseEntity<List<SanPhamChiTietResponse>> getByProductId(@PathVariable("productId") Long productId) {
        return ResponseEntity.ok(service.getByProductId(productId));
    }

    @PostMapping
    public ResponseEntity<SanPhamChiTietResponse> create(@RequestBody SanPhamChiTietRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SanPhamChiTietResponse> update(@PathVariable("id") Long id, @RequestBody SanPhamChiTietRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
