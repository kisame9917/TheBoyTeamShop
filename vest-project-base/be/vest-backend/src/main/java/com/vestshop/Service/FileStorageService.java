package com.vestshop.Service;

import com.vestshop.Exception.ApiException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.*;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024;

    @Value("${app.upload-dir}")
    private String uploadDir;

    private Path nhanVienDir;

    // ----------- ADD: folder khách hàng
    private Path khachHangDir;

    @PostConstruct
    public void init() {
        this.nhanVienDir = Paths.get(uploadDir, "nhanvien").toAbsolutePath().normalize();
        // ----------- ADD
        this.khachHangDir = Paths.get(uploadDir, "khachhang").toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.nhanVienDir);
            // ----------- ADD
            Files.createDirectories(this.khachHangDir);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Không tạo được thư mục upload: " + e.getMessage());
        }
    }

    public String storeNhanVienAvatar(MultipartFile file) {
        return storeImage(file, nhanVienDir, "/uploads/nhanvien/");
    }

    // ----------- ADD: method upload avatar khách hàng
    public String storeKhachHangAvatar(MultipartFile file) {
        return storeImage(file, khachHangDir, "/uploads/khachhang/");
    }

    // ----------- ADD: dùng chung validate + lưu file
    private String storeImage(MultipartFile file, Path targetDir, String urlPrefix) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "File rỗng");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Dung lượng tối đa 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Chỉ chấp nhận file ảnh");
        }

        String original = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "file"));
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0 && dot < original.length() - 1) ext = original.substring(dot).toLowerCase();

        if (ext.isBlank()) {
            String ct = contentType.toLowerCase();
            if (ct.contains("png")) ext = ".png";
            else if (ct.contains("jpeg") || ct.contains("jpg")) ext = ".jpg";
            else if (ct.contains("webp")) ext = ".webp";
            else ext = ".png";
        }

        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = targetDir.resolve(filename).normalize();

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Lưu file thất bại: " + e.getMessage());
        }

        return urlPrefix + filename;
    }
}
