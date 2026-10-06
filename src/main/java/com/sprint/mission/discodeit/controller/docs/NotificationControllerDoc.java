package com.sprint.mission.discodeit.controller.docs;

import com.sprint.mission.discodeit.dto.response.NotificationDto;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

public interface NotificationControllerDoc {

    @GetMapping
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401")
    })
    ResponseEntity<List<NotificationDto>> queryNoty(
            Authentication authentication
    );


    @DeleteMapping("/{notificationId}")
    @ApiResponses({
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "401"),
            @ApiResponse(responseCode = "403"),
            @ApiResponse(responseCode = "404")
    })
    ResponseEntity<Object> delete(
            @PathVariable UUID notificationId,
            Authentication authentication
    );


}
