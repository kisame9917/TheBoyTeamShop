package com.vestshop.Service;

import com.vestshop.Entity.KieuTui;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface KieuTuiService {
    Page<KieuTui> getAll(Pageable pageable);
    List<KieuTui> getAllList();
    KieuTui getById(Long id);
    KieuTui create(KieuTui request);
    KieuTui update(Long id, KieuTui request);
    void delete(Long id);
}
