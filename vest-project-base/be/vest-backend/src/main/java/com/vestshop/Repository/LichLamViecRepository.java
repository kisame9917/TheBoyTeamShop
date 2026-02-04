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
}