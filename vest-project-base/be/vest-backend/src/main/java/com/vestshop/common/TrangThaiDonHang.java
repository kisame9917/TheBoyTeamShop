package com.vestshop.common;

import java.util.Arrays;

public enum TrangThaiDonHang {
    CHO_XAC_NHAN(0, "Chờ xác nhận"),
    DANG_XU_LY(1, "Đang xử lý"),
    DANG_GIAO(2, "Đang giao"),
    DA_GIAO(3, "Đã giao"),
    HOAN_THANH(4, "Hoàn thành"),
    DA_HUY(5, "Đã huỷ"),
    YEU_CAU_HOAN(6, "Yêu cầu hoàn"),
    DA_HOAN(7, "Đã hoàn");

    private final int code;
    private final String ten;

    TrangThaiDonHang(int code, String ten) {
        this.code = code;
        this.ten = ten;
    }

    public int getCode() {
        return code;
    }

    public String getTen() {
        return ten;
    }

    public static TrangThaiDonHang fromCode(Integer code) {
        if (code == null) return CHO_XAC_NHAN;
        return Arrays.stream(values())
                .filter(x -> x.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("trangThaiDon không hợp lệ: " + code));
    }
}
