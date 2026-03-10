package com.vestshop.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamChiTietRequest {
    private Long idSanPham;
    private Long idKichCo;
    private Long idMauSac;
    private Integer soLuongTon;
    private BigDecimal donGia;
    private String ghiChu;
    private Boolean trangThai;
    private String anh;
    private Long mediaPrimaryId;
    private List<Long> galleryMediaIds;
    private String chatLieu;
}
