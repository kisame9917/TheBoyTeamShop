package com.vestshop.job;
import com.vestshop.Entity.HoaDon;
import com.vestshop.Entity.HoaDonChiTiet;
import com.vestshop.Entity.SanPhamChiTiet;
import com.vestshop.Repository.HoaDonChiTietRepository;
import com.vestshop.Repository.HoaDonRepository;
import com.vestshop.Repository.SanPhamChiTietRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DailyCartResetJob {
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    // 00:00 mỗi ngày theo giờ VN/Thai
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Bangkok")
    @Transactional
    public void resetDraftOrdersAtMidnight() {
        LocalDateTime cutoff = LocalDate.now(ZoneId.of("Asia/Bangkok")).atStartOfDay();

        // trạng thái giỏ hàng/nháp của bạn đang dùng = 0
        int statusDraft = 0;

        // 1) lấy hóa đơn nháp trước cutoff
        List<Long> hoaDonIds = hoaDonRepository.findIdsPendingOlderThan(statusDraft, cutoff);
        if (hoaDonIds.isEmpty()) return;

        // 2) lấy tất cả hdct thuộc các hóa đơn đó
        List<HoaDonChiTiet> items = hoaDonChiTietRepository.findAllByHoaDonIds(hoaDonIds);

        // 3) hoàn tồn: soLuongTon += hdct.soLuong
        for (HoaDonChiTiet it : items) {
            SanPhamChiTiet spct = it.getSanPhamChiTiet();
            Integer qty = it.getSoLuong();
            if (spct != null && qty != null && qty > 0) {
                Integer current = spct.getSoLuongTon() == null ? 0 : spct.getSoLuongTon();
                spct.setSoLuongTon(current + qty);
                sanPhamChiTietRepository.save(spct);
            }
        }

        // 4) xoá chi tiết trước rồi xoá hóa đơn
        hoaDonChiTietRepository.deleteAllByHoaDonIds(hoaDonIds);
        hoaDonRepository.deleteAllByIdInBatch(hoaDonIds);
    }

}
