package com.vestshop.Repository;

import com.vestshop.Entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = """
        SELECT MAX(CAST(RIGHT(tai_khoan, 3) AS INT))
        FROM nhan_vien
        WHERE tai_khoan LIKE CONCAT(:base, '%')
          AND LEN(tai_khoan) = LEN(:base) + 3
          AND RIGHT(tai_khoan, 3) LIKE '[0-9][0-9][0-9]'
        """, nativeQuery = true)
    Integer findMaxNumericSuffixByBase(@Param("base") String base);

    @Query(value = """
        SELECT MAX(TRY_CAST(RIGHT(tai_khoan, 3) AS INT))
        FROM nhan_vien
        WHERE tai_khoan IS NOT NULL
          AND LEN(tai_khoan) >= 3
          AND RIGHT(tai_khoan, 3) LIKE '[0-9][0-9][0-9]'
        """, nativeQuery = true)
    Integer findMaxGlobalSuffix3Digits();

}
