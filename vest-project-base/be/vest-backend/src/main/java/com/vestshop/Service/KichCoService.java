package com.vestshop.Service;

import com.vestshop.Entity.KichCo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface KichCoService {
    Page<KichCo> getAll(Pageable pageable);
    List<KichCo> getAllList();
    KichCo getById(Long id);
    KichCo create(KichCo request);
    KichCo update(Long id, KichCo request);
    void delete(Long id);
}
