package com.vestshop.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ThongKeDonHangResponse {
    private Integer code;       // 0..n theo TrangThaiDonHang
    private String trangThai;   // enum name: HOAN_THANH...
    private String tenTrangThai;// tiếng Việt
    private Long soLuong;       // count
    private String kieuDang;    // success/warning/danger/...
}
