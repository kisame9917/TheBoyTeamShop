package com.vestshop.Service.impl;

import com.vestshop.Service.NotificationRealtimeService;
import com.vestshop.dto.response.NotificationEventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationRealtimeServiceImpl implements NotificationRealtimeService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void pushToUser(Long userId, NotificationEventResponse event) {
        messagingTemplate.convertAndSend("/topic/users/" + userId + "/notifications", event);
    }

    @Override
    public void pushToRole(String role, NotificationEventResponse event) {
        messagingTemplate.convertAndSend("/topic/roles/" + role.toUpperCase() + "/notifications", event);
    }
}