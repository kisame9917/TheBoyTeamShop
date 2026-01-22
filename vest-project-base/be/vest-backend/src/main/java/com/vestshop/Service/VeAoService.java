package com.vestshop.Service;

import com.vestshop.Entity.VeAo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface VeAoService {
    Page<VeAo> getAll(Pageable pageable);
    List<VeAo> getAllList();
    VeAo getById(Long id);
    VeAo create(VeAo request);
    VeAo update(Long id, VeAo request);
    void delete(Long id);
}
