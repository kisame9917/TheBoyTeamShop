package com.vestshop.Service.impl;

import com.vestshop.Entity.PhieuGiamGia;
import com.vestshop.Repository.PhieuGiamGiaRepository;
import com.vestshop.Service.PhieuGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhieuGiamGiaServiceImpl implements PhieuGiamGiaService {
   @Autowired
    PhieuGiamGiaRepository repo;


    @Override
    public List<PhieuGiamGia> getAll() {
        return repo.findAll();
    }
}
