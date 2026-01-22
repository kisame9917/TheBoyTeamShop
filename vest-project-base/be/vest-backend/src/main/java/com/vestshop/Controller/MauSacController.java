package com.vestshop.Controller;

import com.vestshop.Entity.MauSac;
import com.vestshop.Service.MauSacService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/mau-sac")
@RequiredArgsConstructor
public class MauSacController {

    private final MauSacService mauSacService;

    @GetMapping
    public ResponseEntity<Page<MauSac>> getAll(Pageable pageable) {
        return ResponseEntity.ok(mauSacService.getAll(pageable));
    }

    @GetMapping("/list")
    public ResponseEntity<List<MauSac>> getAllList() {
        return ResponseEntity.ok(mauSacService.getAllList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MauSac> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(mauSacService.getById(id));
    }

    @PostMapping
    public ResponseEntity<MauSac> create(@RequestBody MauSac request) {
        return ResponseEntity.ok(mauSacService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MauSac> update(@PathVariable("id") Long id, @RequestBody MauSac request) {
        return ResponseEntity.ok(mauSacService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        mauSacService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
