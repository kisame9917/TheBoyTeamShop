package com.vestshop.Service;

import com.vestshop.Entity.MauSac;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface MauSacService {
    Page<MauSac> getAll(Pageable pageable);
    List<MauSac> getAllList();
    MauSac getById(Long id);
    MauSac create(MauSac request);
    MauSac update(Long id, MauSac request);
    void delete(Long id);
}
