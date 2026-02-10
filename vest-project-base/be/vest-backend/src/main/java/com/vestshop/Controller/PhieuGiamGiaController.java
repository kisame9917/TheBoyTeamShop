package com.vestshop.Controller;

import com.vestshop.Entity.PhieuGiamGia;
import com.vestshop.Repository.PhieuGiamGiaCaNhanRepository;
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

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/pgg")
public class PhieuGiamGiaController {

    @Autowired
    private PhieuGiamGiaCaNhanRepository cnRepo;

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

    @GetMapping("/khach-hang-with-stats")
    public List<PhieuGiamGiaCaNhanProjection> allKhWithStats(
            @RequestParam(required = false) Boolean includeShip
    ) {
        boolean incShip = (includeShip == null) ? true : includeShip;

        YearMonth ym = YearMonth.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to = ym.plusMonths(1).atDay(1).atStartOfDay();

        return cnRepo.findAllKhachHangWithStats(from, to, incShip);
    }

    // ✅ THÊM: DS KH đã nhận phiếu (để FE fill checkbox)
    @GetMapping("/{id}/khach-hang")
    public List<PhieuGiamGiaCaNhanProjection> getKhachHangNhanPhieu(
            @PathVariable("id") Long id,
            @RequestParam(required = false) Boolean includeShip
    ) {
        return phieuGiamGiaCaNhanService.getKhachHangNhanPhieu(id, includeShip);
    }

    // ✅ THÊM: Lưu danh sách KH nhận phiếu khi update (gọi đúng service bạn đã viết)
    @PutMapping("/{id}/khach-hang")
    public ResponseEntity<?> updateKhachHangNhanPhieu(
            @PathVariable("id") Long id,
            @RequestBody UpdateKhachHangNhanPhieuRequest req
    ) throws Exception {
        service.updateKhachHangNhanPhieu(id, req);
        return ResponseEntity.ok().build();
    }
}
