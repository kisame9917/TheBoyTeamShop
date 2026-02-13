package com.vestshop.Repository;

import com.vestshop.Entity.LichLamViec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LichLamViecRepository extends JpaRepository<LichLamViec, Long> {

    // Tìm lịch làm việc theo khoảng thời gian (để hiển thị lên bảng lịch)
    @Query("SELECT l FROM LichLamViec l WHERE l.ngayLamViec BETWEEN :startDate AND :endDate ORDER BY l.ngayLamViec ASC, l.caLamViec.gioBatDau ASC")
    List<LichLamViec> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Tìm lịch của 1 nhân viên cụ thể trong khoảng thời gian
    List<LichLamViec> findByNhanVienIdAndNgayLamViecBetween(Long idNv, LocalDate startDate, LocalDate endDate);

    // Lấy danh sách lịch của 1 nhân viên trong 1 ngày cụ thể (Dùng để check trùng giờ)
    List<LichLamViec> findByNhanVienIdAndNgayLamViec(Long idNv, LocalDate ngayLamViec);

    // ================== BỔ SUNG PHỤC VỤ "LỊCH BIỂU" ==================
    // Đếm số nhân viên đã được xếp vào 1 ca trong 1 ngày (để giới hạn tối đa 3)
    long countByCaLamViecIdAndNgayLamViec(Long idCaLamViec, LocalDate ngayLamViec);

    // Check trùng đúng "nhân viên + ca + ngày"
    boolean existsByNhanVienIdAndCaLamViecIdAndNgayLamViec(Long idNhanVien, Long idCaLamViec, LocalDate ngayLamViec);

    // Update: loại trừ chính bản ghi đang sửa
    long countByCaLamViecIdAndNgayLamViecAndIdNot(Long idCaLamViec, LocalDate ngayLamViec, Long id);

    boolean existsByNhanVienIdAndCaLamViecIdAndNgayLamViecAndIdNot(Long idNhanVien, Long idCaLamViec, LocalDate ngayLamViec, Long id);

    // Check: 1 ca / 1 ngày chỉ có 1 nhân viên (trangThai=1)
    List<LichLamViec> findByCaLamViec_IdAndNgayLamViecAndTrangThai(Long idCaLamViec, LocalDate ngayLamViec, Integer trangThai);

}