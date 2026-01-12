package com.vestshop.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SanPhamCreateRequest {
    @NotNull private Long loaiSanPhamId;
    @NotNull private Long soKhuyId;
    @NotNull private Long thuongHieuId;
    @NotNull private Long kieuTuiId;
    @NotNull private Long veAoId;
    @NotNull private Long xeTaId;
    @NotNull private Long xuatXuId;
    @NotNull private Long fitId;
    @NotNull private Long chatLieuId;

    @NotBlank private String maSanPham;
    @NotBlank private String tenSanPham;

    private Integer trangThai = 1;
}
