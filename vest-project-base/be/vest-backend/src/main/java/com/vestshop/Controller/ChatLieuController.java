package com.vestshop.Controller;

import com.vestshop.Entity.ChatLieu;
import com.vestshop.Service.ChatLieuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chat-lieu")
@RequiredArgsConstructor
public class ChatLieuController {

    private final ChatLieuService chatLieuService;

    @GetMapping
    public ResponseEntity<Page<ChatLieu>> getAll(Pageable pageable) {
        return ResponseEntity.ok(chatLieuService.getAll(pageable));
    }

    @GetMapping("/list")
    public ResponseEntity<List<ChatLieu>> getAllList() {
        return ResponseEntity.ok(chatLieuService.getAllList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatLieu> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(chatLieuService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ChatLieu> create(@RequestBody ChatLieu request) {
        return ResponseEntity.ok(chatLieuService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChatLieu> update(@PathVariable("id") Long id, @RequestBody ChatLieu request) {
        return ResponseEntity.ok(chatLieuService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        chatLieuService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
