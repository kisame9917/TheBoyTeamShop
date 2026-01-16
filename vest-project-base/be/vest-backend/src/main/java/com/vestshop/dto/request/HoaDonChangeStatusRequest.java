package com.vestshop.dto.request;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HoaDonChangeStatusRequest {
    private Integer trangThaiDon;
    private String ghiChu;
}
