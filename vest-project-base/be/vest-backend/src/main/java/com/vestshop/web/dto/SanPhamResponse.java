package com.vestshop.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SanPhamResponse {
    private Long id;
    private String maSanPham;
    private String tenSanPham;
    private Integer trangThai;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    private Long loaiSanPhamId;
    private Long soKhuyId;
    private Long thuongHieuId;
    private Long kieuTuiId;
    private Long veAoId;
    private Long xeTaId;
    private Long xuatXuId;
    private Long fitId;
    private Long chatLieuId;
}
