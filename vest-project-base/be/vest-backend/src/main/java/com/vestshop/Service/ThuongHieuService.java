package com.vestshop.Service;

import com.vestshop.Entity.ThuongHieu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ThuongHieuService {
    Page<ThuongHieu> getAll(Pageable pageable);
    List<ThuongHieu> getAllList();
    ThuongHieu getById(Long id);
    ThuongHieu create(ThuongHieu request);
    ThuongHieu update(Long id, ThuongHieu request);
    void delete(Long id);
}
