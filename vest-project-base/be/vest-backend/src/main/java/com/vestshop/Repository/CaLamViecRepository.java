package com.vestshop.Repository;

import com.vestshop.Entity.CaLamViec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CaLamViecRepository extends JpaRepository<CaLamViec, Long> {
    // Lấy tất cả ca đang hoạt động để hiển thị lên combobox xếp lịch
    List<CaLamViec> findAllByTrangThai(Integer trangThai);
}