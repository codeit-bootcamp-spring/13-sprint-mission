package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping(value = "/public")
    public ResponseEntity<ChannelDto> createPublic(@Valid @RequestBody PublicChannelCreateRequest request) {
        log.info("공개 채널 생성 요청: name={}", request.channelName());
        return ResponseEntity.ok(channelService.createPublic(request));
    }

    @PostMapping(value = "/private")
    public ResponseEntity<ChannelDto> createPrivate(@Valid @RequestBody PrivateChannelCreateRequest request) {
        log.info("비공개 채널 생성 요청: 참여자 수={}", request.userIds().size());
        return ResponseEntity.ok(channelService.createPrivate(request));
    }

    @GetMapping
    public ResponseEntity<List<ChannelDto>> findAllByUserId(@RequestParam UUID userId) {
        return ResponseEntity.ok(channelService.findAllByUserId(userId));
    }

    @PatchMapping(value = "/{channelId}")
    public ResponseEntity<ChannelDto> update(@PathVariable UUID channelId, @Valid @RequestBody ChannelUpdateRequest request) {
        log.info("채널 수정 요청: channelId={}", channelId);
        return ResponseEntity.ok(channelService.update(channelId, request));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        log.info("채널 삭제 요청: channelId={}", id);
        channelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
