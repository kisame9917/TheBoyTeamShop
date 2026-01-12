package com.vestshop.service.impl;

import com.vestshop.domain.entity.*;
import com.vestshop.exception.ApiException;
import com.vestshop.repository.*;
import com.vestshop.service.SanPhamService;
import com.vestshop.web.dto.SanPhamCreateRequest;
import com.vestshop.web.dto.SanPhamResponse;
import com.vestshop.web.mapper.SanPhamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SanPhamServiceImpl implements SanPhamService {

    private final SanPhamRepository sanPhamRepository;
    private final LoaiSanPhamRepository loaiSanPhamRepository;
    private final SoKhuyRepository soKhuyRepository;
    private final ThuongHieuRepository thuongHieuRepository;
    private final KieuTuiRepository kieuTuiRepository;
    private final VeAoRepository veAoRepository;
    private final XeTaRepository xeTaRepository;
    private final XuatXuRepository xuatXuRepository;
    private final FitRepository fitRepository;
    private final ChatLieuRepository chatLieuRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SanPhamResponse> findAll() {
        return sanPhamRepository.findAll().stream().map(SanPhamMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SanPhamResponse findById(Long id) {
        SanPham sp = sanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SanPham not found"));
        return SanPhamMapper.toResponse(sp);
    }

    @Override
    @Transactional
    public SanPhamResponse create(SanPhamCreateRequest r) {
        SanPham sp = new SanPham();
        sp.setMaSanPham(r.getMaSanPham());
        sp.setTenSanPham(r.getTenSanPham());
        sp.setTrangThai(r.getTrangThai());
        sp.setNgayTao(LocalDateTime.now());

        // Use getReferenceById so you only need IDs
        sp.setLoaiSanPham(loaiSanPhamRepository.getReferenceById(r.getLoaiSanPhamId()));
        sp.setSoKhuy(soKhuyRepository.getReferenceById(r.getSoKhuyId()));
        sp.setThuongHieu(thuongHieuRepository.getReferenceById(r.getThuongHieuId()));
        sp.setKieuTui(kieuTuiRepository.getReferenceById(r.getKieuTuiId()));
        sp.setVeAo(veAoRepository.getReferenceById(r.getVeAoId()));
        sp.setXeTa(xeTaRepository.getReferenceById(r.getXeTaId()));
        sp.setXuatXu(xuatXuRepository.getReferenceById(r.getXuatXuId()));
        sp.setFit(fitRepository.getReferenceById(r.getFitId()));
        sp.setChatLieu(chatLieuRepository.getReferenceById(r.getChatLieuId()));

        return SanPhamMapper.toResponse(sanPhamRepository.save(sp));
    }
}
