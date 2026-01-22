package com.vestshop.Controller;

import com.vestshop.Service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Autowired
    private StorageService storageService;

    @PostMapping
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        String filename = storageService.store(file);
        Map<String, String> response = new HashMap<>();
        // Return relative URL that will be served by static resource handler
        response.put("url", "/images/" + filename); 
        return ResponseEntity.ok(response);
    }
}
