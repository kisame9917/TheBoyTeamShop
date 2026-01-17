package com.vestshop.Controller;

import com.vestshop.Service.KhachHangService;
import com.vestshop.dto.request.KhachHangRequest;
import com.vestshop.dto.response.KhachHangResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/khach-hang")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class KhachHangController {

    private final KhachHangService khachHangService;

    @GetMapping
    public ResponseEntity<List<KhachHangResponse>> getAll() {
        return ResponseEntity.ok(khachHangService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KhachHangResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(khachHangService.getById(id));
    }

    @PostMapping
    public ResponseEntity<KhachHangResponse> create(@Valid @RequestBody KhachHangRequest request) {
        return new ResponseEntity<>(khachHangService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<KhachHangResponse> update(@PathVariable Long id, @Valid @RequestBody KhachHangRequest request) {
        return ResponseEntity.ok(khachHangService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        khachHangService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
