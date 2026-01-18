package com.vestshop.dto.request;

import lombok.Data;

import java.util.List;
@Data
public class UpdateKhachHangNhanPhieuRequest {
    private List<Long> khachHangIds;
}
