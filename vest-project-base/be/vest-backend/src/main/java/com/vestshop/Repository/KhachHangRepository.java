package com.vestshop.Repository;

import com.vestshop.Entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {

    boolean existsByMaKhachHang(String maKhachHang);
    boolean existsByTaiKhoan(String taiKhoan);
    boolean existsByEmail(String email);
    boolean existsBySoDienThoai(String soDienThoai);

    Optional<KhachHang> findByTaiKhoan(String taiKhoan);
    Optional<KhachHang> findByTaiKhoanIgnoreCase(String taiKhoan);

    @Query("select max(k.maKhachHang) from KhachHang k")
    String findMaxMaKhachHang();

    Optional<KhachHang> findByEmail(String email);
    Optional<KhachHang> findByEmailIgnoreCase(String email);
    Optional<KhachHang> findBySoDienThoai(String soDienThoai);

    Optional<KhachHang> findTopByMaKhachHangStartingWithOrderByMaKhachHangDesc(String prefix);
}