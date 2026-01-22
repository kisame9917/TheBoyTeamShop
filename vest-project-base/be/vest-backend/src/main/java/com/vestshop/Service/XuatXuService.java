package com.vestshop.Service;

import com.vestshop.Entity.XuatXu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface XuatXuService {
    Page<XuatXu> getAll(Pageable pageable);
    List<XuatXu> getAllList();
    XuatXu getById(Long id);
    XuatXu create(XuatXu request);
    XuatXu update(Long id, XuatXu request);
    void delete(Long id);
}
