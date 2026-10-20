package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.docs.NotificationControllerDoc;
import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationControllerDoc {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<NotificationDto>> queryNoty(
            Authentication authentication
    )
    {
        return ResponseEntity.ok(notificationService.getNotifies(authentication));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Object> delete(
            @PathVariable UUID notificationId,
            Authentication authentication
    )
    {
        notificationService.deleteNotify(notificationId, authentication);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
