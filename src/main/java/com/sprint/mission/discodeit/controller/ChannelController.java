package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.CreateChannelRequest;
import com.sprint.mission.discodeit.dto.UpdateChannelRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/channels")
@RequiredArgsConstructor
public class ChannelController {

    private static final Logger log =
            LoggerFactory.getLogger(ChannelController.class);

    private final ChannelService channelService;

    @PostMapping
    public ChannelDto create(
            @Valid @RequestBody CreateChannelRequest request
    ) {
        log.debug(
                "채널 생성 요청: name={}, description={}",
                request.getName(),
                request.getDescription()
        );

        ChannelDto channelDto = channelService.create(request);

        log.info("채널 생성 완료");

        return channelDto;
    }

    @GetMapping
    public List<ChannelDto> findAll() {
        log.debug("채널 목록 조회 요청");

        List<ChannelDto> channels = channelService.findAll();

        log.debug("채널 목록 조회 완료: count={}", channels.size());

        return channels;
    }

    @GetMapping("/{id}")
    public ChannelDto find(
            @PathVariable UUID id
    ) {
        log.debug("채널 단건 조회 요청: channelId={}", id);

        return channelService.find(id);
    }

    @PutMapping("/{id}")
    public ChannelDto update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateChannelRequest request
    ) {
        log.debug("채널 수정 요청: channelId={}", id);

        ChannelDto channelDto = channelService.update(id, request);

        log.info("채널 수정 완료: channelId={}", id);

        return channelDto;
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id
    ) {
        log.debug("채널 삭제 요청: channelId={}", id);

        channelService.delete(id);

        log.info("채널 삭제 완료: channelId={}", id);
    }
}