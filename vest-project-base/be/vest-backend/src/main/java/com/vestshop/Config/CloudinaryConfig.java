package com.vestshop.Config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CloudinaryProperties.class)
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(CloudinaryProperties properties) {
        String url = properties.getUrl();
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("Thiếu CLOUDINARY_URL. Hãy cấu hình biến môi trường Cloudinary trước khi chạy backend.");
        }

        Cloudinary cloudinary = new Cloudinary(url);
        cloudinary.config.secure = properties.isSecure();
        return cloudinary;
    }
}
