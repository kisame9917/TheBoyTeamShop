package com.vestshop.spec;

import com.vestshop.Entity.HoaDon;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class HoaDonSpecifications {

    public static Specification<HoaDon> advanced(
            String keyword,
            Integer trangThaiDon,
            String phanLoai,
            Boolean loaiDon,
            LocalDateTime from,
            LocalDateTime to,
            BigDecimal minTotal,
            BigDecimal maxTotal,
            Boolean hasVoucher,
            Long idNhanVien,
            Boolean active
    ) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String k = "%" + keyword.trim().toLowerCase() + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("maHoaDon")), k),
                        cb.like(cb.lower(root.get("soDienThoai")), k),
                        cb.like(cb.lower(root.get("tenKhachHang")), k)
                ));
            }

            if (active != null) {
                ps.add(cb.equal(root.get("trangThai"), active));
            }

            if (loaiDon != null) {
                ps.add(cb.equal(root.get("loaiDon"), loaiDon));
            }

            if (trangThaiDon != null) {
                ps.add(cb.equal(root.get("trangThaiDon"), trangThaiDon));
            }

            if (phanLoai != null && !phanLoai.isBlank()) {
                Set<Integer> codes = mapPhanLoaiToCodes(phanLoai.trim());
                if (!codes.isEmpty()) {
                    ps.add(root.get("trangThaiDon").in(codes));
                }
            }

            if (from != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("ngayTao"), from));
            }
            if (to != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("ngayTao"), to));
            }

            if (minTotal != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("tongTienSauGiam"), minTotal));
            }
            if (maxTotal != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("tongTienSauGiam"), maxTotal));
            }

            if (hasVoucher != null) {
                if (hasVoucher) ps.add(cb.isNotNull(root.get("phieuGiamGia")));
                else ps.add(cb.isNull(root.get("phieuGiamGia")));
            }

            if (idNhanVien != null) {
                ps.add(cb.equal(root.get("nhanVien").get("id"), idNhanVien));
            }

            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private static Set<Integer> mapPhanLoaiToCodes(String phanLoai) {
        String v = phanLoai.toUpperCase(Locale.ROOT);

        if (v.equals("DANG_GIAO")) return Set.of(2);
        if (v.equals("DA_HOAN_THANH")) return Set.of(4);
        if (v.equals("CHUA_HOAN_THANH")) return Set.of(0, 1, 2, 3);
        if (v.equals("DA_HUY")) return Set.of(5);
        if (v.equals("HOAN_HANG")) return Set.of(6, 7);

        return Set.of();
    }
}
