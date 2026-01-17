package com.vestshop.Controller;

import com.vestshop.Service.NhanVienService;
import com.vestshop.dto.request.NhanVienRequest;
import com.vestshop.dto.response.NhanVienResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nhan-vien")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NhanVienController {

    private final NhanVienService nhanVienService;

    @GetMapping
    public ResponseEntity<List<NhanVienResponse>> getAll() {
        return ResponseEntity.ok(nhanVienService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NhanVienResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(nhanVienService.getById(id));
    }

    @PostMapping
    public ResponseEntity<NhanVienResponse> create(@Valid @RequestBody NhanVienRequest request) {
        return new ResponseEntity<>(nhanVienService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NhanVienResponse> update(@PathVariable Long id, @Valid @RequestBody NhanVienRequest request) {
        return ResponseEntity.ok(nhanVienService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        nhanVienService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

