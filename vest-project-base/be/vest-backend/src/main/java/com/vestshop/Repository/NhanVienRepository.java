package com.vestshop.Repository;

import com.vestshop.Entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Long> {

    Optional<NhanVien> findByMaNhanVien(String maNhanVien);
    Optional<NhanVien> findByTaiKhoan(String taiKhoan);
    Optional<NhanVien> findByEmail(String email);
    Optional<NhanVien> findByCccd(String cccd);
    Optional<NhanVien> findBySoDienThoai(String soDienThoai);

    boolean existsByMaNhanVien(String maNhanVien);
    boolean existsByTaiKhoan(String taiKhoan);
    boolean existsByEmail(String email);
    boolean existsByCccd(String cccd);
    boolean existsBySoDienThoai(String soDienThoai);
}
