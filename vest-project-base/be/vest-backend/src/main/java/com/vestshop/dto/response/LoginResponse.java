package com.vestshop.dto.response;

public record LoginResponse(String token, String role, Long id,String tenNhanVien) {

}
