package com.vestshop.Controller;

import com.vestshop.Entity.VeAo;
import com.vestshop.Service.VeAoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ve-ao")
@RequiredArgsConstructor
public class VeAoController {

    private final VeAoService veAoService;

    @GetMapping
    public ResponseEntity<Page<VeAo>> getAll(Pageable pageable) {
        return ResponseEntity.ok(veAoService.getAll(pageable));
    }

    @GetMapping("/list")
    public ResponseEntity<List<VeAo>> getAllList() {
        return ResponseEntity.ok(veAoService.getAllList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeAo> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(veAoService.getById(id));
    }

    @PostMapping
    public ResponseEntity<VeAo> create(@RequestBody VeAo request) {
        return ResponseEntity.ok(veAoService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VeAo> update(@PathVariable("id") Long id, @RequestBody VeAo request) {
        return ResponseEntity.ok(veAoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        veAoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
