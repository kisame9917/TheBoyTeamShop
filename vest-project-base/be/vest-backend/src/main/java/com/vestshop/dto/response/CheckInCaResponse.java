package com.vestshop.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInCaResponse {
    private PhienCaResponse phienDangMo;

    private Long idNhanVien;
    private String maNhanVien;
    private String tenNhanVien;
    private String anhDaiDienNhanVien;

    private Boolean coLichPhanCong;

    private Long idCaLamViec;
    private String tenCa;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;
    private LocalDate ngayLamViec;

    /** true -> enable button "Xác nhận vào ca" */
    private Boolean duocMoCa;

    /**
     * Deprecated: để tương thích phiên bản cũ. Từ phiên bản mới hệ thống KHÔNG hỗ trợ mở ca tự do.
     */
    private Boolean duocMoCaTuDo;

    private LocalDateTime serverNow;

    /** nếu chưa tới ca: số giây còn lại tới giờ bắt đầu (để FE đếm ngược) */
    private Long secondsToStart;

    /** nếu đang trong khung ca: số giây còn lại tới giờ kết thúc khung ca */
    private Long secondsToEnd;

    /**
     * NEW: khung ca liên (gộp nhiều ca liền nhau nếu end == start)
     */
    private LocalDateTime blockStartTime;
    private LocalDateTime blockEndTime;

    /**
     * NEW: tiền tài khoản đầu ca expected = tiền tài khoản thực tế ca trước (đã đóng).
     * Nếu không có ca trước => 0
     */
    /**
     * Tiền mặt đầu ca expected = tiền mặt thực tế ca trước đã đóng.
     * Nếu không có ca trước => 0.
     */
    private BigDecimal expectedTienMatDauCa;

    /**
     * Tiền tài khoản đầu ca expected = tiền tài khoản thực tế ca trước đã đóng.
     * Nếu không có ca trước => 0.
     */
    private BigDecimal expectedTienTaiKhoanDauCa;

    private String message;
}
