package com.vestshop.Service;

import com.vestshop.Entity.XeTa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface XeTaService {
    Page<XeTa> getAll(Pageable pageable);
    List<XeTa> getAllList();
    XeTa getById(Long id);
    XeTa create(XeTa request);
    XeTa update(Long id, XeTa request);
    void delete(Long id);
}
