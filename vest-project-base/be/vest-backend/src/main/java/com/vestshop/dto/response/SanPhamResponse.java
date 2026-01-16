package com.vestshop.dto.response;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamResponse {
    private Long id;

    private Long chatLieuId;
    private Long loaiSanPhamId;
    private Long thuongHieuId;
    private Long soKhuyId;
    private Long kieuTuiId;
    private Long veAoId;
    private Long xeTaId;
    private Long xuatXuId;
    private Long fitId;

    private String maSanPham;
    private String tenSanPham;

    private String tenLoaiSanPham;
    private String tenThuongHieu;

    private Integer soLuongTon;
    private java.math.BigDecimal giaMin;
    private java.math.BigDecimal giaMax;

    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    private Boolean trangThai;
}
