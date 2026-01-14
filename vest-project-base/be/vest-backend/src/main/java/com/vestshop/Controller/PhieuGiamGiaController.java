package com.vestshop.Controller;

import com.vestshop.Entity.PhieuGiamGia;
import com.vestshop.Service.PhieuGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/test/pgg")
public class PhieuGiamGiaController {
    @Autowired
  private  PhieuGiamGiaService service;

    @GetMapping
    public String hienThiBang(Model model){
        model.addAttribute("listpgg",service.getAll());
        return "test_view_pgg";
    }
    @GetMapping("/{id}")
    public String findById(@PathVariable Long id , Model model){
        model.addAttribute("pgg",service.findbyId(id));
        return "test_view_detail_pgg";
    }

    @PostMapping("/test/pgg/create")
    public String CreatePgg(@ModelAttribute PhieuGiamGia pgg){
        service.create(pgg);
        return "test_view_pgg";
    }

}
