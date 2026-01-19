package com.vestshop.spec;

import com.vestshop.Entity.HoaDon;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
            List<Predicate> predicates = new ArrayList<>();

            // keyword: ma_hoa_don / ten_khach_hang / so_dien_thoai
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("maHoaDon")), kw),
                        cb.like(cb.lower(root.get("tenKhachHang")), kw),
                        cb.like(cb.lower(root.get("soDienThoai")), kw)
                ));
            }

            if (trangThaiDon != null) {
                predicates.add(cb.equal(root.get("trangThaiDon"), trangThaiDon));
            }

            if (loaiDon != null) {
                predicates.add(cb.equal(root.get("loaiDon"), loaiDon));
            }

            // ✅ DATE filter: ngayTao >= from && ngayTao < to (to đã = toDate + 1 day 00:00)
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("ngayTao"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThan(root.get("ngayTao"), to));
            }

            if (minTotal != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("tongTienSauGiam"), minTotal));
            }
            if (maxTotal != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("tongTienSauGiam"), maxTotal));
            }

            if (hasVoucher != null) {
                if (hasVoucher) predicates.add(cb.isNotNull(root.get("phieuGiamGia")));
                else predicates.add(cb.isNull(root.get("phieuGiamGia")));
            }

            if (idNhanVien != null) {
                predicates.add(cb.equal(root.get("nhanVien").get("id"), idNhanVien));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("trangThai"), active));
            }

            // phanLoai: bạn tự định nghĩa (ví dụ: ONLINE/OFFLINE)
            // nếu chưa dùng thì bỏ qua

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
