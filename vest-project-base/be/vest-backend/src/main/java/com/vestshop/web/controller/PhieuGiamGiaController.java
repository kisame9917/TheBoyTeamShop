package com.vestshop.web.controller;

import com.vestshop.Service.PhieuGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/test/pgg")
public class PhieuGiamGiaController {
    @Autowired
  private  PhieuGiamGiaService service;

    @GetMapping
    public String hienThiBang(Model model){
        model.addAttribute("listpgg",service.getAll());
        return "test";
    }


}
