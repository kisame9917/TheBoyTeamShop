package com.vestshop.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamRequest {
    private Long chatLieuId;
    @NotNull private Long loaiSanPhamId;
    @NotNull private Long thuongHieuId;
    @NotNull private Long soKhuyId;
    @NotNull private Long kieuTuiId;
    @NotNull private Long veAoId;
    @NotNull private Long xeTaId;
    @NotNull private Long xuatXuId;
    @NotNull private Long fitId;
    
    private String anh;
    private String chatLieu;

    @NotBlank
    @Size(max = 80)
    private String maSanPham;

    @NotBlank
    @Size(max = 255)
    private String tenSanPham;

    @NotNull
    private Boolean trangThai;

    private String moTa;
    // Fields for default SanPhamChiTiet (Optional now)
    private Long mauSacId;
    private Long kichCoId;
    private java.math.BigDecimal donGia;
    private Integer soLuongTon;

    private java.util.List<SanPhamChiTietRequest> variants;
}
