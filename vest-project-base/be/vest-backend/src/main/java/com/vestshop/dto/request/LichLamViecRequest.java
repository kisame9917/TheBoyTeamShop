package com.vestshop.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class LichLamViecRequest {
    private Long idCaLamViec;
    private Long idNhanVien;
    private LocalDate ngayLamViec;
    private String ghiChu;
}