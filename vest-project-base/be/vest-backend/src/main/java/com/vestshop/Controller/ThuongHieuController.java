package com.vestshop.Controller;

import com.vestshop.Entity.ThuongHieu;
import com.vestshop.Service.ThuongHieuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/thuong-hieu")
@RequiredArgsConstructor
public class ThuongHieuController {

    private final ThuongHieuService thuongHieuService;

    @GetMapping
    public ResponseEntity<Page<ThuongHieu>> getAll(Pageable pageable) {
        return ResponseEntity.ok(thuongHieuService.getAll(pageable));
    }

    @GetMapping("/list")
    public ResponseEntity<List<ThuongHieu>> getAllList() {
        return ResponseEntity.ok(thuongHieuService.getAllList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThuongHieu> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(thuongHieuService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ThuongHieu> create(@RequestBody ThuongHieu request) {
        return ResponseEntity.ok(thuongHieuService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ThuongHieu> update(@PathVariable("id") Long id, @RequestBody ThuongHieu request) {
        return ResponseEntity.ok(thuongHieuService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        thuongHieuService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
