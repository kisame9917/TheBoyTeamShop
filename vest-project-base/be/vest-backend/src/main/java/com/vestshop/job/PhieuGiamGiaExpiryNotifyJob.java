package com.vestshop.job;

import com.vestshop.Entity.PhieuGiamGia;
import com.vestshop.Repository.PhieuGiamGiaRepository;
import com.vestshop.Service.NotificationRealtimeService;
import com.vestshop.dto.response.NotificationEventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class PhieuGiamGiaExpiryNotifyJob {

    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final NotificationRealtimeService notificationRealtimeService;

    // tránh bắn lặp trong cùng 1 ngày
    private final Map<String, LocalDate> sentCache = new ConcurrentHashMap<>();

    private static final DateTimeFormatter VN_DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Bangkok")
    public void notifyVoucherExpiringSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endWindow = now.plusDays(1); // sắp hết hạn trong 24h tới

        for (PhieuGiamGia pgg : phieuGiamGiaRepository.findAll()) {
            if (!Boolean.TRUE.equals(pgg.getTrangThai())) continue;
            if (pgg.getNgayKetThuc() == null) continue;

            LocalDateTime endTime = pgg.getNgayKetThuc();

            // chỉ lấy phiếu còn hiệu lực và sắp hết hạn
            if (endTime.isBefore(now) || endTime.isAfter(endWindow)) continue;

            String dedupeKey = "PGG_EXP_" + pgg.getId() + "_" + now.toLocalDate();
            if (sentCache.containsKey(dedupeKey)) continue;

            String title;
            if (endTime.toLocalDate().isEqual(now.toLocalDate())) {
                title = "Phiếu " + pgg.getMaGiamGia() + " sẽ hết hạn hôm nay";
            } else {
                title = "Phiếu " + pgg.getMaGiamGia() + " sắp hết hạn";
            }

            notificationRealtimeService.pushToRole(
                    "ADMIN",
                    NotificationEventResponse.builder()
                            .id(String.valueOf(System.currentTimeMillis()))
                            .title(title)
                            .time("Vừa xong")
                            .link("/admin/vouchers")
                            .type("VOUCHER_EXPIRING")
                            .createdAt(LocalDateTime.now().toString())
                            .build()
            );

            sentCache.put(dedupeKey, now.toLocalDate());
        }
    }
}