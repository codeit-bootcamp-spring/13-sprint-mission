package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static com.sprint.mission.discodeit.entity.ChannelType.PRIVATE;
import static com.sprint.mission.discodeit.entity.ChannelType.PUBLIC;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelMapper channelMapper;

    @Override
    public ChannelDto createPublic(PublicChannelCreateRequest request) {
        Channel channel = Channel.create(PUBLIC, request.name(), request.description());
        Channel savedChannel = channelRepository.save(channel);

        log.info("채널: {} 공개 채널이 생성됨.", savedChannel.getName());
        return channelMapper.toDto(savedChannel);
    }

    @Override
    public ChannelDto createPrivate(PrivateChannelCreateRequest request) {
        if (request.participantIds() == null || request.participantIds().isEmpty()) {
            throw new IllegalArgumentException("PRIVATE 채널에는 참여 유저가 필요합니다.");
        }

        List<User> participants = userRepository.findAllById(request.participantIds());
        if (participants.size() != request.participantIds().size()) {
            throw new NoSuchElementException("존재하지 않는 유저가 참여자 목록에 포함되어 있습니다.");
        }

        Channel channel = Channel.create(PRIVATE, null, null);
        Channel savedChannel = channelRepository.save(channel);

        participants.forEach(participant -> {
            ReadStatus readStatus = ReadStatus.create(participant, savedChannel, Instant.now());
            readStatusRepository.save(readStatus);
            log.info("ReadStatus가 생성됨.");
        });

        log.info("채널: private Channel이 생성됨.");
        return channelMapper.toDto(savedChannel);
    }

    @Override
    @Transactional(readOnly = true)
    public ChannelDto find(UUID channelId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(
                        () -> new NoSuchElementException("채널 ID: " + channelId + " 를 찾을 수 없습니다."));

        return channelMapper.toDto(channel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelDto> findAllByUserId(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("유저 ID: " + userId + " 를 찾을 수 없습니다.");
        }

        List<UUID> joinedPrivateChannelIds = readStatusRepository.findAllByUserId(userId).stream()
                .map(readStatus -> readStatus.getChannel().getId())
                .toList();

        return channelRepository.findAll().stream()
                .filter(channel -> channel.getType() == PUBLIC
                        || joinedPrivateChannelIds.contains(channel.getId()))
                .map(channelMapper::toDto)
                .toList();
    }

    @Override
    public ChannelDto update(ChannelUpdateRequest request) {
        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new NoSuchElementException("채널 ID: " + request.channelId() + " 를 찾을 수 없습니다."));

        if (channel.getType() == ChannelType.PRIVATE) {
            throw new IllegalArgumentException("PRIVATE 채널은 수정할 수 없습니다.");
        }

        channel.update(request.newName(), request.newDescription());

        log.info("채널: {}가 수정됨.", channel.getName());
        return channelMapper.toDto(channel);
    }

    @Override
    public void delete(UUID channelId) {
        if (!channelRepository.existsById(channelId)) {
            throw new NoSuchElementException("채널 ID:  " + channelId + " 를 찾을 수 없습니다.");
        }

        channelRepository.deleteById(channelId);
        log.info("채널: {}가 삭제됨.", channelId);
    }
}