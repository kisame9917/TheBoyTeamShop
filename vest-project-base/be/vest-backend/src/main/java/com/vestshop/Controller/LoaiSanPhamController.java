package com.vestshop.Controller;

import com.vestshop.Entity.LoaiSanPham;
import com.vestshop.Service.LoaiSanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/loai-san-pham")
@RequiredArgsConstructor
public class LoaiSanPhamController {

    private final LoaiSanPhamService loaiSanPhamService;

    @GetMapping
    public ResponseEntity<Page<LoaiSanPham>> getAll(Pageable pageable) {
        return ResponseEntity.ok(loaiSanPhamService.getAll(pageable));
    }

    @GetMapping("/list")
    public ResponseEntity<List<LoaiSanPham>> getAllList() {
        return ResponseEntity.ok(loaiSanPhamService.getAllList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoaiSanPham> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(loaiSanPhamService.getById(id));
    }

    @PostMapping
    public ResponseEntity<LoaiSanPham> create(@RequestBody LoaiSanPham request) {
        return ResponseEntity.ok(loaiSanPhamService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoaiSanPham> update(@PathVariable("id") Long id, @RequestBody LoaiSanPham request) {
        return ResponseEntity.ok(loaiSanPhamService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        loaiSanPhamService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
