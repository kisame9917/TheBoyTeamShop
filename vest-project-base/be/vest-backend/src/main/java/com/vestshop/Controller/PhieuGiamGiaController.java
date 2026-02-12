package com.vestshop.Controller;

import com.vestshop.Entity.PhieuGiamGia;
import com.vestshop.Service.PhieuGiamGiaCaNhanService;
import com.vestshop.Service.PhieuGiamGiaService;
import com.vestshop.dto.request.PhieuGiamGiaCreateRequest;
import com.vestshop.dto.request.PhieuGiamGiaUpdateRequest;
import com.vestshop.dto.request.UpdateKhachHangNhanPhieuRequest;
import com.vestshop.dto.response.PhieuGiamGiaCaNhanProjection;
import com.vestshop.dto.response.PhieuGiamGiaDetailResponse;
import com.vestshop.dto.response.PhieuGiamGiaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pgg")
public class PhieuGiamGiaController {

    @Autowired
    private PhieuGiamGiaService service;

    @Autowired
    private PhieuGiamGiaCaNhanService phieuGiamGiaCaNhanService;

    @GetMapping
    public List<PhieuGiamGiaResponse> getAll() {
        return service.getAll();
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody PhieuGiamGiaCreateRequest pgg) throws Exception {
        PhieuGiamGia saved = service.create(pgg);
        return ResponseEntity.ok(saved.getId());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PhieuGiamGiaUpdateRequest pgg) throws Exception {
        service.update(id, pgg);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhieuGiamGiaDetailResponse> detail(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.detail(id));
    }

    @PutMapping("/start/{id}")
    public ResponseEntity<?> startNow(@PathVariable Long id) throws Exception {
        service.startpgg(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/end-pgg/{id}")
    public ResponseEntity<?> endpgg(@PathVariable("id") Long id) throws Exception {
        service.endpgg(id);
        return ResponseEntity.ok().build();
    }

    /**
     * ✅ DS KH để chọn khi tạo PGG cá nhân + stats theo MONTH/YEAR/RANGE
     *
     * FE gửi:
     *  - statsMode=MONTH&month=YYYY-MM
     *  - statsMode=YEAR&year=YYYY
     *  - statsMode=RANGE&from=YYYY-MM-DD&to=YYYY-MM-DD
     */
    @GetMapping("/khach-hang-with-stats")
    public List<PhieuGiamGiaCaNhanProjection> allKhWithStats(
            @RequestParam(required = false) Boolean includeShip,
            @RequestParam(required = false) String statsMode,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        return phieuGiamGiaCaNhanService.getAllKhachHangWithStats(
                includeShip, statsMode, month, year, from, to
        );
    }

    /**
     * ✅ DS KH đã nhận phiếu theo pggId + stats theo MONTH/YEAR/RANGE
     */
    @GetMapping("/{id}/khach-hang")
    public List<PhieuGiamGiaCaNhanProjection> getKhachHangNhanPhieu(
            @PathVariable("id") Long id,
            @RequestParam(required = false) Boolean includeShip,
            @RequestParam(required = false) String statsMode,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        return phieuGiamGiaCaNhanService.getKhachHangNhanPhieu(
                id, includeShip, statsMode, month, year, from, to
        );
    }

    @PutMapping("/{id}/khach-hang")
    public ResponseEntity<?> updateKhachHangNhanPhieu(
            @PathVariable("id") Long id,
            @RequestBody UpdateKhachHangNhanPhieuRequest req
    ) throws Exception {
        service.updateKhachHangNhanPhieu(id, req);
        return ResponseEntity.ok().build();
    }
}
