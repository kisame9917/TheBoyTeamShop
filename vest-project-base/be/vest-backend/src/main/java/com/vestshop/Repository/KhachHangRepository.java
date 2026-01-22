package com.vestshop.Repository;

import com.vestshop.Entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {

    boolean existsByMaKhachHang(String maKhachHang);
    boolean existsByTaiKhoan(String taiKhoan);
    boolean existsByEmail(String email);
    boolean existsBySoDienThoai(String soDienThoai);

    Optional<KhachHang> findByTaiKhoan(String taiKhoan);
    Optional<KhachHang> findByEmail(String email);
    Optional<KhachHang> findBySoDienThoai(String soDienThoai);

    Optional<KhachHang> findTopByMaKhachHangStartingWithOrderByMaKhachHangDesc(String prefix);
}
