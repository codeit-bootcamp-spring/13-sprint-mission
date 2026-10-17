package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> findAll(
            @AuthenticationPrincipal
            DiscodeitUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserDto().id();

        List<NotificationDto> notifications = notificationService.findAllByReceiverId(userId);

        return ResponseEntity.ok(notifications);
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal
            DiscodeitUserDetails userDetails,
            @PathVariable UUID notificationId
    ) {
        UUID requesterId = userDetails.getUserDto().id();

        notificationService.delete(
                notificationId,
                requesterId
        );

        return ResponseEntity.noContent().build();
    }

}
