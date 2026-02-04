package com.vestshop.Controller;

import com.vestshop.Entity.DiaChiKhachHang;
import com.vestshop.Entity.KhachHang;
import com.vestshop.Exception.ApiException;
import com.vestshop.Repository.DiaChiKhachHangRepository;
import com.vestshop.Repository.KhachHangRepository;
import com.vestshop.dto.response.DiaChiKhachHangResponse;
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

    private DiaChiKhachHangResponse map(DiaChiKhachHang d) {
        if (d == null) return null;
        return DiaChiKhachHangResponse.builder()
                .id(d.getId())
                .idKhachHang(d.getKhachHang() != null ? d.getKhachHang().getId() : null)
                .tenNguoiNhan(d.getTenNguoiNhan())
                .soDienThoai(d.getSoDienThoai())
                .diaChiChiTiet(d.getDiaChiChiTiet())
                .phuongXa(d.getPhuongXa())
                .quanHuyen(d.getQuanHuyen())
                .tinhThanh(d.getTinhThanh())
                .quocGia(d.getQuocGia())
                .laMacDinh(d.getLaMacDinh())
                .trangThai(d.getTrangThai())
                .build();
    }

    // ========= LIST (chỉ lấy địa chỉ còn hoạt động) =========
    @GetMapping({"", "/"})
    @Transactional(readOnly = true)
    public List<DiaChiKhachHangResponse> list(@PathVariable Long khId) {
        khachHangRepository.findById(khId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng ID: " + khId));

        return diaChiKhachHangRepository
                .findByKhachHangIdAndTrangThaiTrueOrderByLaMacDinhDescIdDesc(khId)
                .stream().map(this::map).toList();
    }

    // ========= CREATE =========
    @PostMapping({"", "/"})
    @Transactional
    public DiaChiKhachHangResponse create(@PathVariable Long khId, @RequestBody DiaChiKhachHang body) {
        KhachHang kh = khachHangRepository.findById(khId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng ID: " + khId));

        DiaChiKhachHang dc = new DiaChiKhachHang();
        dc.setKhachHang(kh);

        applyBody(dc, body, true);

        if (Boolean.TRUE.equals(dc.getLaMacDinh())) {
            diaChiKhachHangRepository.clearDefaultByKhachHangId(khId);
            dc.setLaMacDinh(true);
        }

        return map(diaChiKhachHangRepository.save(dc));
    }

    // ========= UPDATE (chỉ update địa chỉ còn hoạt động) =========
    @PutMapping({"/{dcId}", "/{dcId}/"})
    @Transactional
    public DiaChiKhachHangResponse update(@PathVariable Long khId,
                                          @PathVariable Long dcId,
                                          @RequestBody DiaChiKhachHang body) {

        DiaChiKhachHang dc = diaChiKhachHangRepository.findByIdAndKhachHangIdAndTrangThaiTrue(dcId, khId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy địa chỉ ID: " + dcId));

        applyBody(dc, body, false);

        if (Boolean.TRUE.equals(body.getLaMacDinh())) {
            diaChiKhachHangRepository.clearDefaultByKhachHangId(khId);
            dc.setLaMacDinh(true);
        }

        return map(diaChiKhachHangRepository.save(dc));
    }

    // ========= DELETE (soft delete) =========
    @DeleteMapping({"/{dcId}", "/{dcId}/"})
    @Transactional
    public void delete(@PathVariable Long khId, @PathVariable Long dcId) {

        DiaChiKhachHang dc = diaChiKhachHangRepository.findByIdAndKhachHangIdAndTrangThaiTrue(dcId, khId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy địa chỉ ID: " + dcId));

        boolean wasDefault = Boolean.TRUE.equals(dc.getLaMacDinh());

        dc.setTrangThai(false);
        dc.setLaMacDinh(false);
        diaChiKhachHangRepository.save(dc);

        // Nếu xóa địa chỉ mặc định → set mặc định mới (lấy địa chỉ mới nhất còn hoạt động)
        if (wasDefault) {
            diaChiKhachHangRepository.findFirstByKhachHangIdAndTrangThaiTrueOrderByIdDesc(khId)
                    .ifPresent(next -> {
                        diaChiKhachHangRepository.clearDefaultByKhachHangId(khId);
                        next.setLaMacDinh(true);
                        diaChiKhachHangRepository.save(next);
                    });
        }
    }

    // ========= SET DEFAULT =========
    @PutMapping({"/{dcId}/mac-dinh", "/{dcId}/mac-dinh/"})
    @Transactional
    public void setDefault(@PathVariable Long khId, @PathVariable Long dcId) {

        DiaChiKhachHang dc = diaChiKhachHangRepository.findByIdAndKhachHangIdAndTrangThaiTrue(dcId, khId)
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

        target.setTrangThai(body.getTrangThai() != null ? body.getTrangThai() : Boolean.TRUE);

        if (isCreate) {
            target.setLaMacDinh(body.getLaMacDinh() != null ? body.getLaMacDinh() : Boolean.FALSE);
        } else if (body.getLaMacDinh() != null) {
            target.setLaMacDinh(body.getLaMacDinh());
        }
    }
}
