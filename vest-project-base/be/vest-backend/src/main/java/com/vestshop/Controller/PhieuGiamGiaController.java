package com.vestshop.Controller;

import com.vestshop.Entity.PhieuGiamGia;
import com.vestshop.Service.PhieuGiamGiaService;
import com.vestshop.dto.request.PhieuGiamGiaCreateRequest;
import com.vestshop.dto.request.PhieuGiamGiaUpdateRequest;
import com.vestshop.dto.response.PhieuGiamGiaDetailResponse;
import com.vestshop.dto.response.PhieuGiamGiaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pgg")
public class PhieuGiamGiaController {
    @Autowired
  private  PhieuGiamGiaService service;

    @GetMapping
    public List<PhieuGiamGiaResponse> getAll(){
        return service.getAll();
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody PhieuGiamGiaCreateRequest pgg){
        PhieuGiamGia saved = service.create(pgg);
        return ResponseEntity.ok(saved.getId());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PhieuGiamGiaUpdateRequest pgg) throws Exception {
        service.update(id,pgg);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{id}")
    public ResponseEntity<PhieuGiamGiaDetailResponse> detail(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.detail(id));
    }

    @PutMapping("/end-pgg/{id}")
    public ResponseEntity<?> endpgg(@PathVariable("id") Long id) throws Exception{
        service.endpgg(id);
        return ResponseEntity.ok().build();
    }

}
