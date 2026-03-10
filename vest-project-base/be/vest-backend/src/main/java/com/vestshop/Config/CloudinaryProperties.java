package com.vestshop.Config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "cloudinary")
public class CloudinaryProperties {
    private String url;
    private boolean secure = true;
    private String defaultFolder = "vestshop";
}
