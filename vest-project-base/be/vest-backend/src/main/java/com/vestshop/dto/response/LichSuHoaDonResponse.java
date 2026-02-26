package com.vestshop.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LichSuHoaDonResponse {
    private Long id;
    private String hanhDong;
    private String ghiChu;
    private LocalDateTime thoiGian;

    // ✅ thêm 2 field này
    private String maNhanVien;
    private String tenNhanVien;
}