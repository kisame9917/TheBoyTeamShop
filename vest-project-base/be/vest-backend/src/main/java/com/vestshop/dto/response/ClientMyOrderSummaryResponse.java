package com.vestshop.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientMyOrderSummaryResponse {
    private Long id;
    private String maHoaDon;

    private Integer trangThaiDon;
    private String tenTrangThaiDon;

    private String paymentMethod;
    private String paymentStatus;

    private String tenNguoiNhanHang;
    private String soDienThoaiNhanHang;

    private Integer tongSanPham;
    private BigDecimal tongTienSauGiam;

    private LocalDateTime ngayTao;
}