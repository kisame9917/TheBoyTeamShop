package com.vestshop.Service.impl;

import com.vestshop.Entity.PhieuGiamGia;
import com.vestshop.Repository.PhieuGiamGiaRepository;
import com.vestshop.Service.PhieuGiamGiaService;
import com.vestshop.dto.request.PhieuGiamGiaCreateRequest;
import com.vestshop.dto.request.PhieuGiamGiaUpdateRequest;
import com.vestshop.dto.response.GetPhieuGiamGiaDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PhieuGiamGiaServiceImpl implements PhieuGiamGiaService {
   @Autowired
    PhieuGiamGiaRepository repo;


    @Override
    public List<GetPhieuGiamGiaDto> getAll() {

        return repo.findAll().stream().map(
                phieuGiamGia -> new GetPhieuGiamGiaDto(
                        phieuGiamGia.getId(),
                        phieuGiamGia.getMaGiamGia(),
                        phieuGiamGia.getTenGiamGia(),
                        phieuGiamGia.getSoLuong(),
                        phieuGiamGia.getLoaiGiam(),
                        phieuGiamGia.getNgayBatDau(),
                        phieuGiamGia.getNgayKetThuc(),
                        phieuGiamGia.getMoTa(),
                        phieuGiamGia.getNgayTao(),
                        phieuGiamGia.getNgayCapNhat(),
                        phieuGiamGia.getGiaTriPhanTram(),
                        phieuGiamGia.getGiaTriTienMat(),
                        phieuGiamGia.getTrangThai()
                )).collect(Collectors.toList());
    }

    @Override
    public Optional<PhieuGiamGia> findbyId(Long id) {
       return repo.findById(id);
    }



    @Override
    public PhieuGiamGia create(PhieuGiamGiaCreateRequest dto) {
        PhieuGiamGia pgg = new PhieuGiamGia();
        pgg.setMaGiamGia(dto.getMaGiamGia());
        pgg.setTenGiamGia(dto.getTenGiamGia());
        pgg.setSoLuong(dto.getSoLuong());
        pgg.setLoaiGiam(dto.getLoaiGiam());
        pgg.setNgayBatDau(dto.getNgayBatDau());
        pgg.setNgayKetThuc(dto.getNgayKetThuc());
        pgg.setMoTa(dto.getMoTa());
        pgg.setNgayTao(LocalDateTime.now());
        pgg.setGiaTriTienMat(dto.getGiaTriTienMat());
        pgg.setGiaTriPhanTram(dto.getGiaTriPhanTram());
        pgg.setTrangThai(true);
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
        updatepgg.setNgayCapNhat(LocalDateTime.now());
        updatepgg.setGiaTriPhanTram(dto.getGiaTriPhanTram());
        updatepgg.setGiaTriTienMat(dto.getGiaTriTienMat());
//        updatepgg.setTrangThai(dto.getTrangThai());
        return repo.save(updatepgg);
    }

    @Override
    public void delete(Long id) {
        PhieuGiamGia pgg = repo.findById(id).orElseThrow(()-> new RuntimeException("Khong tim thay id:" +id));
        pgg.setTrangThai(false);
        pgg.setNgayCapNhat(LocalDateTime.now());
        repo.save(pgg);
    }


}
