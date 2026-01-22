package com.vestshop.Controller;

import com.vestshop.Entity.Fit;
import com.vestshop.Service.FitService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/fit")
@RequiredArgsConstructor
public class FitController {

    private final FitService fitService;

    @GetMapping
    public ResponseEntity<Page<Fit>> getAll(Pageable pageable) {
        return ResponseEntity.ok(fitService.getAll(pageable));
    }

    @GetMapping("/list")
    public ResponseEntity<List<Fit>> getAllList() {
        return ResponseEntity.ok(fitService.getAllList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fit> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(fitService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Fit> create(@RequestBody Fit request) {
        return ResponseEntity.ok(fitService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Fit> update(@PathVariable("id") Long id, @RequestBody Fit request) {
        return ResponseEntity.ok(fitService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        fitService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
