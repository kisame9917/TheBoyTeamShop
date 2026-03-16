package com.vestshop.Service;

import com.vestshop.dto.response.NotificationEventResponse;

public interface NotificationRealtimeService {
    void pushToUser(Long userId, NotificationEventResponse event);
    void pushToRole(String role, NotificationEventResponse event);
}