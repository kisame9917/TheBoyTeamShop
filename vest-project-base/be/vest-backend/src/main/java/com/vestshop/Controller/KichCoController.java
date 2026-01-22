package com.vestshop.Controller;

import com.vestshop.Entity.KichCo;
import com.vestshop.Service.KichCoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/kich-co")
@RequiredArgsConstructor
public class KichCoController {

    private final KichCoService kichCoService;

    @GetMapping
    public ResponseEntity<Page<KichCo>> getAll(Pageable pageable) {
        return ResponseEntity.ok(kichCoService.getAll(pageable));
    }

    @GetMapping("/list")
    public ResponseEntity<List<KichCo>> getAllList() {
        return ResponseEntity.ok(kichCoService.getAllList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KichCo> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(kichCoService.getById(id));
    }

    @PostMapping
    public ResponseEntity<KichCo> create(@RequestBody KichCo request) {
        return ResponseEntity.ok(kichCoService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<KichCo> update(@PathVariable("id") Long id, @RequestBody KichCo request) {
        return ResponseEntity.ok(kichCoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        kichCoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
