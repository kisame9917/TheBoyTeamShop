package com.vestshop.Service.impl;

import com.vestshop.Repository.PhieuGiamGiaCaNhanRepository;
import com.vestshop.Service.PhieuGiamGiaCaNhanService;
import com.vestshop.dto.response.PhieuGiamGiaCaNhanProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

@Service
public class PhieuGiamGiaCaNhanServiceImpl implements PhieuGiamGiaCaNhanService {

    @Autowired
    private PhieuGiamGiaCaNhanRepository cnRepo;

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    /**
     * Danh sách KH đã nhận phiếu (theo PGG) + stats (lọc theo statsMode)
     *
     * FE gửi:
     * - statsMode=MONTH&month=YYYY-MM
     * - statsMode=YEAR&year=YYYY
     * - statsMode=RANGE&from=YYYY-MM-DD&to=YYYY-MM-DD
     *
     * Query dùng:
     * >= fromDate và < toDate  (exclusive end)
     */
    @Override
    public List<PhieuGiamGiaCaNhanProjection> getKhachHangNhanPhieu(
            Long pggId,
            Boolean includeShip,
            String statsMode,
            String month,
            Integer year,
            String from,
            String to
    ) {
        boolean incShip = (includeShip == null) ? true : includeShip;

        LocalDateTime[] range = buildRange(statsMode, month, year, from, to);
        LocalDateTime fromDate = range[0];
        LocalDateTime toDate = range[1];

        return cnRepo.findDsKhachHangNhanPhieuWithStats(pggId, fromDate, toDate, incShip);
    }

    /**
     * Danh sách KH để CHỌN khi tạo PGG cá nhân + stats (lọc theo statsMode)
     */
    @Override
    public List<PhieuGiamGiaCaNhanProjection> getAllKhachHangWithStats(
            Boolean includeShip,
            String statsMode,
            String month,
            Integer year,
            String from,
            String to
    ) {
        boolean incShip = (includeShip == null) ? true : includeShip;

        LocalDateTime[] range = buildRange(statsMode, month, year, from, to);
        LocalDateTime fromDate = range[0];
        LocalDateTime toDate = range[1];

        return cnRepo.findAllKhachHangWithStats(fromDate, toDate, incShip);
    }

    /**
     * Build khoảng thời gian theo mode:
     * - MONTH: [YYYY-MM-01 00:00:00, nextMonth-01 00:00:00)
     * - YEAR : [YYYY-01-01 00:00:00, (YYYY+1)-01-01 00:00:00)
     * - RANGE: [from 00:00:00, (to+1day) 00:00:00)
     */
    private LocalDateTime[] buildRange(String statsMode, String month, Integer year, String from, String to) {
        String mode = (statsMode == null || statsMode.isBlank()) ? "MONTH" : statsMode.trim().toUpperCase();

        LocalDateTime fromDate;
        LocalDateTime toDate;

        switch (mode) {
            case "YEAR" -> {
                int y = (year != null) ? year : Year.now(ZONE).getValue();
                fromDate = LocalDate.of(y, 1, 1).atStartOfDay();
                toDate = LocalDate.of(y + 1, 1, 1).atStartOfDay(); // exclusive end
            }
            case "RANGE" -> {
                if (from == null || from.isBlank() || to == null || to.isBlank()) {
                    // fallback: 30 ngày gần nhất
                    LocalDate d2 = LocalDate.now(ZONE);
                    LocalDate d1 = d2.minusDays(30);
                    fromDate = d1.atStartOfDay();
                    toDate = d2.plusDays(1).atStartOfDay(); // exclusive
                } else {
                    LocalDate f = LocalDate.parse(from.trim()); // YYYY-MM-DD
                    LocalDate t = LocalDate.parse(to.trim());   // YYYY-MM-DD
                    fromDate = f.atStartOfDay();
                    toDate = t.plusDays(1).atStartOfDay(); // exclusive
                }
            }
            case "MONTH" -> {
                YearMonth ym;
                if (month != null && !month.isBlank()) {
                    ym = YearMonth.parse(month.trim()); // YYYY-MM
                } else {
                    ym = YearMonth.now(ZONE);
                }
                fromDate = ym.atDay(1).atStartOfDay();
                toDate = ym.plusMonths(1).atDay(1).atStartOfDay(); // exclusive
            }
            default -> throw new IllegalArgumentException("statsMode không hợp lệ: " + statsMode);
        }

        return new LocalDateTime[]{fromDate, toDate};
    }


}
