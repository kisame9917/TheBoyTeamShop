package com.vestshop.web.mapper;

import com.vestshop.domain.entity.SanPham;
import com.vestshop.web.dto.SanPhamResponse;

public final class SanPhamMapper {
    private SanPhamMapper() {}

    public static SanPhamResponse toResponse(SanPham e) {
        return SanPhamResponse.builder()
                .id(e.getId())
                .maSanPham(e.getMaSanPham())
                .tenSanPham(e.getTenSanPham())
                .trangThai(e.getTrangThai())
                .ngayTao(e.getNgayTao())
                .ngayCapNhat(e.getNgayCapNhat())

                .loaiSanPhamId(e.getLoaiSanPham() != null ? e.getLoaiSanPham().getId() : null)
                .soKhuyId(e.getSoKhuy() != null ? e.getSoKhuy().getId() : null)
                .thuongHieuId(e.getThuongHieu() != null ? e.getThuongHieu().getId() : null)
                .kieuTuiId(e.getKieuTui() != null ? e.getKieuTui().getId() : null)
                .veAoId(e.getVeAo() != null ? e.getVeAo().getId() : null)
                .xeTaId(e.getXeTa() != null ? e.getXeTa().getId() : null)
                .xuatXuId(e.getXuatXu() != null ? e.getXuatXu().getId() : null)
                .fitId(e.getFit() != null ? e.getFit().getId() : null)
                .chatLieuId(e.getChatLieu() != null ? e.getChatLieu().getId() : null)
                .build();
    }
}
