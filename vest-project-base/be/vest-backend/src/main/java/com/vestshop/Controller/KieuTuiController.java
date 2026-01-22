package com.vestshop.Controller;

import com.vestshop.Entity.KieuTui;
import com.vestshop.Service.KieuTuiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/kieu-tui")
@RequiredArgsConstructor
public class KieuTuiController {

    private final KieuTuiService kieuTuiService;

    @GetMapping
    public ResponseEntity<Page<KieuTui>> getAll(Pageable pageable) {
        return ResponseEntity.ok(kieuTuiService.getAll(pageable));
    }

    @GetMapping("/list")
    public ResponseEntity<List<KieuTui>> getAllList() {
        return ResponseEntity.ok(kieuTuiService.getAllList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KieuTui> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(kieuTuiService.getById(id));
    }

    @PostMapping
    public ResponseEntity<KieuTui> create(@RequestBody KieuTui request) {
        return ResponseEntity.ok(kieuTuiService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<KieuTui> update(@PathVariable("id") Long id, @RequestBody KieuTui request) {
        return ResponseEntity.ok(kieuTuiService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        kieuTuiService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
