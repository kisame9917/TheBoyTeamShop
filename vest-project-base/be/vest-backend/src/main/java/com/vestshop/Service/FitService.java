package com.vestshop.Service;

import com.vestshop.Entity.Fit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface FitService {
    Page<Fit> getAll(Pageable pageable);
    List<Fit> getAllList();
    Fit getById(Long id);
    Fit create(Fit request);
    Fit update(Long id, Fit request);
    void delete(Long id);
}
