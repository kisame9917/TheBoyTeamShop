package com.vestshop.Controller;

import com.vestshop.Entity.XuatXu;
import com.vestshop.Service.XuatXuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/xuat-xu")
@RequiredArgsConstructor
public class XuatXuController {

    private final XuatXuService xuatXuService;

    @GetMapping
    public ResponseEntity<Page<XuatXu>> getAll(Pageable pageable) {
        return ResponseEntity.ok(xuatXuService.getAll(pageable));
    }

    @GetMapping("/list")
    public ResponseEntity<List<XuatXu>> getAllList() {
        return ResponseEntity.ok(xuatXuService.getAllList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<XuatXu> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(xuatXuService.getById(id));
    }

    @PostMapping
    public ResponseEntity<XuatXu> create(@RequestBody XuatXu request) {
        return ResponseEntity.ok(xuatXuService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<XuatXu> update(@PathVariable("id") Long id, @RequestBody XuatXu request) {
        return ResponseEntity.ok(xuatXuService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        xuatXuService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
