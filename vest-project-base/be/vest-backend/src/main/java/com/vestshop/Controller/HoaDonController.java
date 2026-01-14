package com.vestshop.Controller;

import com.vestshop.Service.HoaDonService;
import com.vestshop.dto.request.HoaDonCreateRequest;
import com.vestshop.dto.request.HoaDonUpdateRequest;
import com.vestshop.dto.response.HoaDonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hoa-don")
@RequiredArgsConstructor
public class HoaDonController {

    private final HoaDonService hoaDonService;

    @PostMapping
    public ResponseEntity<HoaDonResponse> create(@RequestBody HoaDonCreateRequest req) {
        return ResponseEntity.ok(hoaDonService.create(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HoaDonResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(hoaDonService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<HoaDonResponse>> getPage(
            @RequestParam(required = false) Boolean trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = "asc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(hoaDonService.getPage(trangThai, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HoaDonResponse> update(@PathVariable Long id, @RequestBody HoaDonUpdateRequest req) {
        return ResponseEntity.ok(hoaDonService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        hoaDonService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
