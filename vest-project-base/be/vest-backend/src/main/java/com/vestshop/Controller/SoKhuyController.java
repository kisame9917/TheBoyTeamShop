package com.vestshop.Controller;

import com.vestshop.Entity.SoKhuy;
import com.vestshop.Service.SoKhuyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/so-khuy")
@RequiredArgsConstructor
public class SoKhuyController {

    private final SoKhuyService soKhuyService;

    @GetMapping
    public ResponseEntity<Page<SoKhuy>> getAll(Pageable pageable) {
        return ResponseEntity.ok(soKhuyService.getAll(pageable));
    }

    @GetMapping("/list")
    public ResponseEntity<List<SoKhuy>> getAllList() {
        return ResponseEntity.ok(soKhuyService.getAllList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SoKhuy> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(soKhuyService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SoKhuy> create(@RequestBody SoKhuy request) {
        return ResponseEntity.ok(soKhuyService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SoKhuy> update(@PathVariable("id") Long id, @RequestBody SoKhuy request) {
        return ResponseEntity.ok(soKhuyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        soKhuyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
