package com.vestshop.web.controller;

import com.vestshop.Service.PhieuGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/test/pgg")
public class PhieuGiamGiaController {
    @Autowired
  private  PhieuGiamGiaService service;


}
