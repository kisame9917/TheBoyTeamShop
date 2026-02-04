package com.vestshop.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class LichLamViecResponse {
    private Long id;

    // Thông tin nhân viên
    private Long idNhanVien;
    private String tenNhanVien;
    private String maNhanVien;

    // Thông tin ca
    private Long idCaLamViec;
    private String tenCa;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;

    private LocalDate ngayLamViec;
    private Integer trangThai;
    private String ghiChu;
}