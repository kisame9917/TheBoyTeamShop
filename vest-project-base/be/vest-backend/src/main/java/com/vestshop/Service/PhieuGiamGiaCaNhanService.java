package com.vestshop.Service;

import com.vestshop.dto.response.PhieuGiamGiaCaNhanProjection;
import java.util.List;

public interface PhieuGiamGiaCaNhanService {

    List<PhieuGiamGiaCaNhanProjection> getKhachHangNhanPhieu(
            Long pggId,
            Boolean includeShip,
            String statsMode,
            String month,
            Integer year,
            String from,
            String to
    );

    List<PhieuGiamGiaCaNhanProjection> getAllKhachHangWithStats(
            Boolean includeShip,
            String statsMode,
            String month,
            Integer year,
            String from,
            String to
    );
}
