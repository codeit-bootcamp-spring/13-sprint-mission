package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.CreateChannelRequest;
import com.sprint.mission.discodeit.dto.UpdateChannelRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicChannelService implements ChannelService {

    private static final Logger log =
            LoggerFactory.getLogger(BasicChannelService.class);

    private final ChannelRepository channelRepository;
    private final ChannelMapper channelMapper;

    @Override
    @Transactional
    public ChannelDto create(CreateChannelRequest request) {
        log.debug(
                "채널 생성 요청: name={}, description={}",
                request.getName(),
                request.getDescription()
        );

        Channel channel = new Channel(
                ChannelType.PUBLIC,
                request.getName(),
                request.getDescription()
        );

        Channel savedChannel = channelRepository.save(channel);

        log.info(
                "채널 생성 완료: channelId={}, type={}",
                savedChannel.getId(),
                savedChannel.getType()
        );

        return channelMapper.toDto(savedChannel);
    }

    @Override
    public ChannelDto find(UUID id) {
        log.debug("채널 조회 요청: channelId={}", id);

        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "채널 조회 실패 - 채널 없음: channelId={}",
                            id
                    );
                    return new ChannelNotFoundException(id);
                });

        log.debug("채널 조회 완료: channelId={}", id);

        return channelMapper.toDto(channel);
    }

    @Override
    public List<ChannelDto> findAll() {
        log.debug("채널 목록 조회 요청");

        List<ChannelDto> channels = channelRepository.findAll()
                .stream()
                .map(channelMapper::toDto)
                .toList();

        log.debug("채널 목록 조회 완료: count={}", channels.size());

        return channels;
    }

    @Override
    @Transactional
    public ChannelDto update(
            UUID id,
            UpdateChannelRequest request
    ) {
        log.debug(
                "채널 수정 요청: channelId={}, name={}",
                id,
                request.getName()
        );

        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "채널 수정 실패 - 채널 없음: channelId={}",
                            id
                    );
                    return new ChannelNotFoundException(id);
                });

        channel.update(
                channel.getType(),
                request.getName(),
                request.getDescription()
        );

        log.info("채널 수정 완료: channelId={}", id);

        return channelMapper.toDto(channel);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.debug("채널 삭제 요청: channelId={}", id);

        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "채널 삭제 실패 - 채널 없음: channelId={}",
                            id
                    );
                    return new ChannelNotFoundException(id);
                });

        channelRepository.delete(channel);

        log.info("채널 삭제 완료: channelId={}", id);
    }
}