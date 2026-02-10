package com.vestshop.Service;

import com.vestshop.dto.response.KhachHangChiTietResponse;

public interface KhachHangChiTietService {
   KhachHangChiTietResponse getStats(Long khId, String month, Boolean includeShip);

}
