package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ChannelCreateRequest;
import com.sprint.mission.discodeit.dto.ChannelResponse;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    /*
     * API 명세 v1.2 기준
     * POST /api/channels/public
     */
    @PostMapping("/public")
    public ResponseEntity<ChannelResponse> createPublicChannel(
            @Valid @RequestBody ChannelCreateRequest request
    ) {
        ChannelCreateRequest fixedRequest = new ChannelCreateRequest(
                ChannelType.PUBLIC,
                request.getName(),
                request.getDescription()
        );

        ChannelResponse response =
                channelService.createPublicChannel(fixedRequest);

        URI location = URI.create(
                "/api/channels/" + response.getId()
        );

        return ResponseEntity
                .created(location)
                .body(response);
    }

    /*
     * API 명세 v1.2 기준
     * POST /api/channels/private
     */
    @PostMapping("/private")
    public ResponseEntity<ChannelResponse> createPrivateChannel(
            @Valid @RequestBody PrivateChannelCreateRequest request
    ) {
        ChannelResponse response =
                channelService.createPrivateChannel(request);

        URI location = URI.create(
                "/api/channels/" + response.getId()
        );

        return ResponseEntity
                .created(location)
                .body(response);
    }

    /*
     * 기존 테스트/Postman 호환용 단건 조회
     */
    @GetMapping("/{channelId}")
    public ResponseEntity<ChannelResponse> find(
            @PathVariable UUID channelId
    ) {
        ChannelResponse response =
                channelService.find(channelId);

        return ResponseEntity.ok(response);
    }

    /*
     * API 명세 v1.2 기준
     * GET /api/channels?userId=...
     */
    @GetMapping
    public ResponseEntity<List<ChannelResponse>> findAllByUserId(
            @RequestParam UUID userId
    ) {
        List<ChannelResponse> responses =
                channelService.findAllByUserId(userId);

        return ResponseEntity.ok(responses);
    }

    /*
     * 기존 호환용 채널 수정
     *
     * PATCH /api/channels
     */
    @PatchMapping
    public ResponseEntity<ChannelResponse> update(
            @Valid @RequestBody ChannelUpdateRequest request
    ) {
        ChannelResponse response =
                channelService.update(request);

        return ResponseEntity.ok(response);
    }

    /*
     * API 명세 v1.2 기준
     * PATCH /api/channels/{channelId}
     *
     * body:
     * {
     *   "newName": "새 채널 이름",
     *   "newDescription": "새 채널 설명"
     * }
     */
    @PatchMapping("/{channelId}")
    public ResponseEntity<ChannelResponse> updateByPathVariable(
            @PathVariable UUID channelId,
            @Valid @RequestBody ChannelUpdateRequest request
    ) {
        ChannelUpdateRequest fixedRequest =
                new ChannelUpdateRequest(
                        channelId,
                        ChannelType.PUBLIC,
                        request.getName(),
                        request.getDescription()
                );

        ChannelResponse response =
                channelService.update(fixedRequest);

        return ResponseEntity.ok(response);
    }

    /*
     * API 명세 v1.2 기준
     * DELETE /api/channels/{channelId}
     */
    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID channelId
    ) {
        channelService.delete(channelId);

        return ResponseEntity
                .noContent()
                .build();
    }
}