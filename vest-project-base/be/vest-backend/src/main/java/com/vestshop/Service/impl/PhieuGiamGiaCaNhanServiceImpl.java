package com.vestshop.Service.impl;

import com.vestshop.Entity.PhieuGiamGia;
import com.vestshop.Entity.PhieuGiamGiaCaNhan;
import com.vestshop.Repository.PhieuGiamGiaCaNhanRepository;
import com.vestshop.Repository.PhieuGiamGiaRepository;
import com.vestshop.Service.PhieuGiamGiaCaNhanService;
import com.vestshop.dto.response.PhieuGiamGiaCaNhanProjection;
import com.vestshop.dto.response.PhieuGiamGiaCaNhanResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PhieuGiamGiaCaNhanServiceImpl implements PhieuGiamGiaCaNhanService {
    @Autowired
    private PhieuGiamGiaCaNhanRepository cnRepo;

    @Override
    public List<PhieuGiamGiaCaNhanProjection> getKhachHangNhanPhieu(Long pggId, Boolean includeShip) {
        boolean incShip = (includeShip == null) ? true : includeShip;

        YearMonth ym = YearMonth.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to = ym.plusMonths(1).atDay(1).atStartOfDay();

        return cnRepo.findDsKhachHangNhanPhieuWithStats(pggId, from, to, incShip);
    }
}
