package com.vestshop.Controller;

import com.vestshop.Entity.DiaChiKhachHang;
import com.vestshop.Entity.KhachHang;
import com.vestshop.Exception.ApiException;
import com.vestshop.Repository.DiaChiKhachHangRepository;
import com.vestshop.Repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping({"/api/khach-hang/{khId}/dia-chi", "/api/khach-hang/{khId}/dia-chi/"})
public class DiaChiKhachHangController {

    private final KhachHangRepository khachHangRepository;
    private final DiaChiKhachHangRepository diaChiKhachHangRepository;

    // ========= LIST =========
    @GetMapping({"", "/"})
    @Transactional(readOnly = true)
    public List<DiaChiKhachHang> list(@PathVariable Long khId) {
        // đảm bảo KH tồn tại
        khachHangRepository.findById(khId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng ID: " + khId));

        return diaChiKhachHangRepository.findByKhachHangIdOrderByLaMacDinhDescIdDesc(khId);
    }

    // ========= CREATE =========
    @PostMapping({"", "/"})
    @Transactional
    public DiaChiKhachHang create(@PathVariable Long khId, @RequestBody DiaChiKhachHang body) {
        KhachHang kh = khachHangRepository.findById(khId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng ID: " + khId));

        DiaChiKhachHang dc = new DiaChiKhachHang();
        dc.setKhachHang(kh);

        applyBody(dc, body, true);

        // mặc định
        if (Boolean.TRUE.equals(dc.getLaMacDinh())) {
            diaChiKhachHangRepository.clearDefaultByKhachHangId(khId);
            dc.setLaMacDinh(true);
        }

        return diaChiKhachHangRepository.save(dc);
    }

    // ========= UPDATE =========
    // ✅ hỗ trợ cả "/{dcId}" và "/{dcId}/" (fix đúng lỗi ảnh)
    @PutMapping({"/{dcId}", "/{dcId}/"})
    @Transactional
    public DiaChiKhachHang update(@PathVariable Long khId, @PathVariable Long dcId, @RequestBody DiaChiKhachHang body) {
        DiaChiKhachHang dc = diaChiKhachHangRepository.findByIdAndKhachHangId(dcId, khId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy địa chỉ ID: " + dcId));

        applyBody(dc, body, false);

        if (Boolean.TRUE.equals(body.getLaMacDinh())) {
            diaChiKhachHangRepository.clearDefaultByKhachHangId(khId);
            dc.setLaMacDinh(true);
        }

        return diaChiKhachHangRepository.save(dc);
    }

    // ========= DELETE (soft delete) =========
    @DeleteMapping({"/{dcId}", "/{dcId}/"})
    @Transactional
    public void delete(@PathVariable Long khId, @PathVariable Long dcId) {
        DiaChiKhachHang dc = diaChiKhachHangRepository.findByIdAndKhachHangId(dcId, khId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy địa chỉ ID: " + dcId));

        // soft delete để an toàn
        dc.setTrangThai(false);
        dc.setLaMacDinh(false);
        diaChiKhachHangRepository.save(dc);
    }

    // ========= SET DEFAULT =========
    @PutMapping({"/{dcId}/mac-dinh", "/{dcId}/mac-dinh/"})
    @Transactional
    public void setDefault(@PathVariable Long khId, @PathVariable Long dcId) {
        DiaChiKhachHang dc = diaChiKhachHangRepository.findByIdAndKhachHangId(dcId, khId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy địa chỉ ID: " + dcId));

        diaChiKhachHangRepository.clearDefaultByKhachHangId(khId);
        dc.setLaMacDinh(true);
        diaChiKhachHangRepository.save(dc);
    }

    private void applyBody(DiaChiKhachHang target, DiaChiKhachHang body, boolean isCreate) {
        target.setTenNguoiNhan(body.getTenNguoiNhan());
        target.setSoDienThoai(body.getSoDienThoai());
        target.setDiaChiChiTiet(body.getDiaChiChiTiet());
        target.setPhuongXa(body.getPhuongXa());
        target.setQuanHuyen(body.getQuanHuyen());
        target.setTinhThanh(body.getTinhThanh());
        target.setQuocGia(body.getQuocGia() != null ? body.getQuocGia() : "Việt Nam");

        // nếu FE không gửi thì default true
        target.setTrangThai(body.getTrangThai() != null ? body.getTrangThai() : Boolean.TRUE);

        // create: nếu không gửi mặc định thì false
        if (isCreate) {
            target.setLaMacDinh(body.getLaMacDinh() != null ? body.getLaMacDinh() : Boolean.FALSE);
        } else if (body.getLaMacDinh() != null) {
            target.setLaMacDinh(body.getLaMacDinh());
        }
    }
}
