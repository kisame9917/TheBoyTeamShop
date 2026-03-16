package com.vestshop.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationEventResponse {
    private String id;
    private String title;
    private String time;
    private String link;
    private String type;
    private String createdAt;
}
