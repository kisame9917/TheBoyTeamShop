package com.vestshop.Service.impl;

import com.vestshop.Entity.PhieuGiamGia;
import com.vestshop.Entity.PhieuGiamGiaCaNhan;
import com.vestshop.Repository.PhieuGiamGiaCaNhanRepository;
import com.vestshop.Repository.PhieuGiamGiaRepository;
import com.vestshop.Service.PhieuGiamGiaCaNhanService;
import com.vestshop.dto.response.PhieuGiamGiaCaNhanResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PhieuGiamGiaCaNhanServiceImpl implements PhieuGiamGiaCaNhanService {
    @Autowired
    private PhieuGiamGiaRepository pggRepo;

    @Autowired
    private PhieuGiamGiaCaNhanRepository cnRepo;


    @Override
    public List<PhieuGiamGiaCaNhanResponse> getKhachHangNhanPhieu(Long pggId) {
        return cnRepo.findDsKhachHangNhanPhieu(pggId);
    }
}
