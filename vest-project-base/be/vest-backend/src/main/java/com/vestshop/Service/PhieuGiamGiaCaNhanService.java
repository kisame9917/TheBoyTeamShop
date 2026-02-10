package com.vestshop.Service;

import com.vestshop.dto.response.PhieuGiamGiaCaNhanProjection;
import com.vestshop.dto.response.PhieuGiamGiaCaNhanResponse;
import org.springframework.stereotype.Service;

import java.util.List;

public interface PhieuGiamGiaCaNhanService {
    List<PhieuGiamGiaCaNhanProjection> getKhachHangNhanPhieu(Long pggId,Boolean includeShip);
}
