package com.vestshop.Service.impl;

import com.vestshop.Entity.PhieuGiamGia;
import com.vestshop.Repository.PhieuGiamGiaRepository;
import com.vestshop.Service.PhieuGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PhieuGiamGiaServiceImpl implements PhieuGiamGiaService {
   @Autowired
    PhieuGiamGiaRepository repo;


    @Override
    public List<PhieuGiamGia> getAll() {
        return repo.findAll();
    }

    @Override
    public Optional<PhieuGiamGia> findbyId(Long id) {
       return repo.findById(id);
    }



    @Override
    public PhieuGiamGia create(PhieuGiamGia phieuGiamGia) {
        return repo.save(phieuGiamGia);
    }

    @Override
    public PhieuGiamGia update(Long id, PhieuGiamGia phieuGiamGia)  {
        PhieuGiamGia pgg = repo.findById(id).orElseThrow(()-> new IllegalArgumentException("khong tim thay id:" + id));
        pgg.setMaGiamGia(phieuGiamGia.getMaGiamGia());
        pgg.setTenGiamGia(phieuGiamGia.getTenGiamGia());
        pgg.setSoLuong(phieuGiamGia.getSoLuong());
        pgg.setLoaiGiam(phieuGiamGia.getLoaiGiam());
        pgg.setNgayBatDau(phieuGiamGia.getNgayBatDau());
        pgg.setNgayKetThuc(phieuGiamGia.getNgayKetThuc());
        pgg.setMoTa(phieuGiamGia.getMoTa());
        pgg.setNgayTao(phieuGiamGia.getNgayTao());
        pgg.setNgayCapNhat(phieuGiamGia.getNgayCapNhat());
        pgg.setGiaTriPhanTram(phieuGiamGia.getGiaTriPhanTram());
        pgg.setGiaTriTienMat(phieuGiamGia.getGiaTriTienMat());
        pgg.setTrangThai(phieuGiamGia.getTrangThai());
        return repo.save(pgg);
    }


}
