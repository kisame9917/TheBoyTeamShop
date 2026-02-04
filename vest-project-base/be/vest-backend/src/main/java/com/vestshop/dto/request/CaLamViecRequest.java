package com.vestshop.dto.request;

import lombok.Data;
import java.time.LocalTime;

@Data
public class CaLamViecRequest {
    private String tenCa;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;
    private String moTa;
    private Integer trangThai;
}