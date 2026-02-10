package com.vestshop.Service.impl;

import com.vestshop.Entity.KhachHang;
import com.vestshop.Repository.HoaDonRepository;
import com.vestshop.Repository.KhachHangRepository;
import com.vestshop.Service.KhachHangChiTietService;
import com.vestshop.dto.response.KhachHangChiTietResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;

@Service
public class KhachHangChiTietServiceImpl implements KhachHangChiTietService {

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Override
    public KhachHangChiTietResponse getStats(Long khId, String month, Boolean includeShip) {

        boolean incShip = (includeShip == null) ? true : includeShip;

        YearMonth ym = (month == null || month.isBlank())
                ? YearMonth.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                : YearMonth.parse(month);

        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to = ym.plusMonths(1).atDay(1).atStartOfDay();

        Long monthlyCount = hoaDonRepository.monthlyOrderCount(khId, from, to);
        if (monthlyCount == null) monthlyCount = 0L;

        BigDecimal monthlySpent = hoaDonRepository.monthlySpent(khId, from, to, incShip);
        if (monthlySpent == null) monthlySpent = BigDecimal.ZERO;

        BigDecimal totalSpent = hoaDonRepository.totalSpent(khId, incShip);
        if (totalSpent == null) totalSpent = BigDecimal.ZERO;

        // ✅ LẤY KHÁCH HÀNG để có ngày sinh
        KhachHang kh = khachHangRepository.findById(khId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng id=" + khId));

        KhachHangChiTietResponse res = new KhachHangChiTietResponse();
        res.setCustomerId(khId);
        res.setMonth(ym.toString());
        res.setMonthlyOrderCount(monthlyCount);
        res.setMonthlySpent(monthlySpent);
        res.setTotalSpent(totalSpent);

        // ✅ giờ mới không null (nếu DB có)
        res.setNgaySinh(kh.getNgaySinh());


        return res;

    }

}
