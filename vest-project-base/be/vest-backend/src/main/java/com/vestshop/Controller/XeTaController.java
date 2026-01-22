package com.vestshop.Controller;

import com.vestshop.Entity.XeTa;
import com.vestshop.Service.XeTaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/xe-ta")
@RequiredArgsConstructor
public class XeTaController {

    private final XeTaService xeTaService;

    @GetMapping
    public ResponseEntity<Page<XeTa>> getAll(Pageable pageable) {
        return ResponseEntity.ok(xeTaService.getAll(pageable));
    }

    @GetMapping("/list")
    public ResponseEntity<List<XeTa>> getAllList() {
        return ResponseEntity.ok(xeTaService.getAllList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<XeTa> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(xeTaService.getById(id));
    }

    @PostMapping
    public ResponseEntity<XeTa> create(@RequestBody XeTa request) {
        return ResponseEntity.ok(xeTaService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<XeTa> update(@PathVariable("id") Long id, @RequestBody XeTa request) {
        return ResponseEntity.ok(xeTaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        xeTaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
