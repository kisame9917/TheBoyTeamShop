package com.vestshop.Repository;

import com.vestshop.Entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {

    Optional<KhachHang> findByMaKhachHang(String maKhachHang);
    Optional<KhachHang> findByTaiKhoan(String taiKhoan);
    Optional<KhachHang> findByEmail(String email);
    Optional<KhachHang> findBySoDienThoai(String soDienThoai);

    boolean existsByMaKhachHang(String maKhachHang);
    boolean existsByTaiKhoan(String taiKhoan);
    boolean existsByEmail(String email);
    boolean existsBySoDienThoai(String soDienThoai);
}

