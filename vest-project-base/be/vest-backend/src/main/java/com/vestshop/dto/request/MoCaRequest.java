package com.vestshop.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MoCaRequest {
    /**
     * Nếu mở theo lịch phân công: có thể bỏ trống, hệ thống tự xác định ca hiện tại.
     * (Từ phiên bản mới: không hỗ trợ mở ca tự do.)
     */
    private Long idCaLamViec;

    /**
     * Mặc định: hôm nay
     */
    private LocalDate ngayLamViec;

    private BigDecimal tienMatDauCa;

    /**
     * NEW: Tiền tài khoản đầu ca (bắt buộc phải khớp tiền tài khoản thực tế của ca trước)
     */
    private BigDecimal tienTaiKhoanDauCa;

    /**
     * Ghi chú (FE mới sẽ không dùng, nhưng giữ để tương thích)
     */
    private String ghiChu;

    /**
     * Deprecated: hệ thống KHÔNG hỗ trợ mở ca tự do nữa. Nếu gửi true sẽ bị từ chối.
     */
    private Boolean moTuDo;
}
