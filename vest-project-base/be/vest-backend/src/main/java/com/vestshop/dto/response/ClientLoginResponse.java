package com.vestshop.dto.response;

public record ClientLoginResponse(  String token,
                                    Long id,
                                    String taiKhoan,
                                    String anhDaiDien,
                                    String tenKhachHang) {
}
