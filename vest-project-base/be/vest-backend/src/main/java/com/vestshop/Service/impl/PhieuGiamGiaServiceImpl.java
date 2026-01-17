package com.vestshop.Service.impl;

import com.vestshop.Entity.PhieuGiamGia;
import com.vestshop.Repository.PhieuGiamGiaCaNhanRepository;
import com.vestshop.Repository.PhieuGiamGiaRepository;
import com.vestshop.Service.PhieuGiamGiaService;
import com.vestshop.dto.request.PhieuGiamGiaCreateRequest;
import com.vestshop.dto.request.PhieuGiamGiaUpdateRequest;
import com.vestshop.dto.response.PhieuGiamGiaDetailResponse;
import com.vestshop.dto.response.PhieuGiamGiaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PhieuGiamGiaServiceImpl implements PhieuGiamGiaService {
   @Autowired
    PhieuGiamGiaRepository repo;


    @Override
    public List<PhieuGiamGiaResponse> getAll() {

        return repo.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getTrangThai()))
                .map(p -> new PhieuGiamGiaResponse(
                        p.getId(),
                        p.getMaGiamGia(),
                        p.getTenGiamGia(),
                        p.getSoLuong(),
                        p.getNgayBatDau(),
                        p.getNgayKetThuc(),
                        p.getTrangThai(),
                        p.getLoaiGiam(),
                        p.getGiaTriPhanTram(),
                        p.getGiaTriTienMat(),
                        Boolean.TRUE.equals(p.getLoaiPhieu()) ? "CA_NHAN" : "CONG_KHAI"
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PhieuGiamGia> findbyId(Long id) {
       return repo.findById(id);
    }

    @Override
    public PhieuGiamGiaDetailResponse detail(Long id) {
        PhieuGiamGia p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy PGG id=" + id));
        return new PhieuGiamGiaDetailResponse(
                p.getId(),
                p.getMaGiamGia(),
                p.getTenGiamGia(),
                p.getSoLuong(),
                p.getLoaiGiam(),
                p.getGiaTriPhanTram(),
                p.getGiaTriTienMat(),
                p.getMoTa(),
                p.getNgayBatDau(),
                p.getNgayKetThuc(),
                p.getNgayTao(),
                p.getNgayCapNhat(),
                p.getTrangThai(),
                p.getDonHangToiThieu(),
                p.getGiaTriGiamToiDa(),
                p.getLoaiPhieu()
        );
    }


    @Override
    public PhieuGiamGia create(PhieuGiamGiaCreateRequest dto) {
        PhieuGiamGia pgg = new PhieuGiamGia();
        pgg.setMaGiamGia(generateUniqueMaGiamGia());
        pgg.setTenGiamGia(dto.getTenGiamGia());
        pgg.setSoLuong(dto.getSoLuong());
        pgg.setLoaiGiam(dto.getLoaiGiam());
        pgg.setNgayBatDau(dto.getNgayBatDau());
        pgg.setNgayKetThuc(dto.getNgayKetThuc());
        pgg.setMoTa(dto.getMoTa());
        pgg.setNgayTao(LocalDate.now());
        pgg.setTrangThai(true);
        pgg.setDonHangToiThieu(dto.getDonHangToiThieu());
        pgg.setGiaTriGiamToiDa(dto.getGiaTriGiamToiDa());
        if (Boolean.TRUE.equals(dto.getLoaiGiam())) {

            pgg.setGiaTriPhanTram(dto.getGiaTriPhanTram());
            pgg.setGiaTriGiamToiDa(dto.getGiaTriGiamToiDa());
            pgg.setGiaTriTienMat(null);
        } else {

            pgg.setGiaTriTienMat(dto.getGiaTriTienMat());
            pgg.setGiaTriPhanTram(null);
            pgg.setGiaTriGiamToiDa(null);
        }
        pgg.setLoaiPhieu(dto.getLoaiPhieu());
        return repo.save(pgg);
    }

    @Override
    public PhieuGiamGia update(Long id, PhieuGiamGiaUpdateRequest dto)  {
        Optional<PhieuGiamGia> pgg = repo.findById(id);
        if(!pgg.isPresent()){
            throw new RuntimeException("Khong tim thay id: " + id);
        }
        PhieuGiamGia updatepgg = pgg.get();
        updatepgg.setTenGiamGia(dto.getTenGiamGia());
        updatepgg.setSoLuong(dto.getSoLuong());
        updatepgg.setLoaiGiam(dto.getLoaiGiam());
        updatepgg.setNgayBatDau(dto.getNgayBatDau());
        updatepgg.setNgayKetThuc(dto.getNgayKetThuc());
        updatepgg.setMoTa(dto.getMoTa());
        updatepgg.setNgayCapNhat(LocalDate.now());
        updatepgg.setGiaTriPhanTram(dto.getGiaTriPhanTram());
        updatepgg.setGiaTriTienMat(dto.getGiaTriTienMat());
//        updatepgg.setTrangThai(dto.getTrangThai());
        return repo.save(updatepgg);
    }

    @Override
    public void delete(Long id) {
        PhieuGiamGia pgg = repo.findById(id).orElseThrow(()-> new RuntimeException("Khong tim thay id:" +id));
        pgg.setTrangThai(false);
        pgg.setNgayCapNhat(LocalDate.now());
        repo.save(pgg);
    }



    private String generateUniqueMaGiamGia() {
        // ví dụ: VCH-8K2P9A
        for (int i = 0; i < 50; i++) {
            String code = "VC" + randomAlphaNum(6);
            if (!repo.existsByMaGiamGia(code)) return code;
        }
        throw new RuntimeException("Không thể tạo mã giảm giá");
    }
    private String randomAlphaNum(int len) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(len);
        java.util.concurrent.ThreadLocalRandom r = java.util.concurrent.ThreadLocalRandom.current();
        for (int i = 0; i < len; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
        return sb.toString();
    }

}
