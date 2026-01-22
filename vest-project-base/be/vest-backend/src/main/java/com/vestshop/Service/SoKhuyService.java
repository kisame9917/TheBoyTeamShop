package com.vestshop.Service;

import com.vestshop.Entity.SoKhuy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface SoKhuyService {
    Page<SoKhuy> getAll(Pageable pageable);
    List<SoKhuy> getAllList();
    SoKhuy getById(Long id);
    SoKhuy create(SoKhuy request);
    SoKhuy update(Long id, SoKhuy request);
    void delete(Long id);
}
