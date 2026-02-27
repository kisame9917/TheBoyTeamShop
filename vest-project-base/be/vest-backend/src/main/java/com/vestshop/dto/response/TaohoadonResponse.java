package com.vestshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaohoadonResponse {
    private Long id;
    private String maHoaDon;
    private Integer trangThaiDon;
}
