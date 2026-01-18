package com.vestshop.Service;

import com.vestshop.dto.response.PhieuGiamGiaCaNhanResponse;
import org.springframework.stereotype.Service;

import java.util.List;

public interface PhieuGiamGiaCaNhanService {
    List<PhieuGiamGiaCaNhanResponse> getKhachHangNhanPhieu(Long pggId);
}
