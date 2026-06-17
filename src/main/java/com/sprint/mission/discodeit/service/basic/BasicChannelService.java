package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static com.sprint.mission.discodeit.entity.ChannelType.PRIVATE;
import static com.sprint.mission.discodeit.entity.ChannelType.PUBLIC;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;



    @Override
    public ChannelResponse createPublic(PublicChannelCreateRequest request) {
        Channel channel = new Channel(PUBLIC, request.name(), request.description());
        Channel savedChannel = channelRepository.save(channel);

        log.info("채널: {} 공개 채널이 생성됨.", savedChannel.getName());
        return toResponse(savedChannel);
    }

    @Override
    public ChannelResponse createPrivate(PrivateChannelCreateRequest request) {
        if (request.participantIds() == null || request.participantIds().isEmpty()) {
            throw new IllegalArgumentException("PRIVATE 채널에는 참여 유저가 필요합니다.");
        }

        request.participantIds().forEach(userId -> {
            if (!userRepository.existsById(userId)) {
                throw new NoSuchElementException("유저 ID: " + userId + " 를 찾을 수 없습니다.");
            }
        });

        Channel channel = new Channel(PRIVATE, null, null);
        Channel savedChannel = channelRepository.save(channel);

        request.participantIds().forEach(userId -> {
            ReadStatus readStatus = new ReadStatus(
                    userId,
                    savedChannel.getId(),
                    Instant.now()
            );

            readStatusRepository.save(readStatus);
            log.info("ReadStatus가 생성됨.");
        });

        log.info("채널: private Channel이 생성됨.");
        return toResponse(savedChannel);
    }

    @Override
    public ChannelResponse find(UUID channelId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(
                        () -> new NoSuchElementException("채널 ID: " + channelId + " 를 찾을 수 없습니다."));

        return toResponse(channel);
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("유저 ID: " + userId + " 를 찾을 수 없습니다.");
        }

        List<UUID> joinedPrivateChannel = readStatusRepository.findAll().stream()
                .filter(readStatus -> readStatus.getUserId().equals(userId))
                .map(ReadStatus::getChannelId)
                .toList();

        return channelRepository.findAll().stream()
                .filter(channel -> channel.getType() == PUBLIC
                        || joinedPrivateChannel.contains(channel.getId()))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ChannelResponse update(ChannelUpdateRequest request) {
        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new NoSuchElementException("채널 ID: " + request.channelId() + " 를 찾을 수 없습니다."));

        if (channel.getType() == ChannelType.PRIVATE) {
            throw new IllegalArgumentException("PRIVATE 채널은 수정할 수 없습니다.");
        }

        channel.update(request.newName(), request.newDescription());
        Channel savedChannel = channelRepository.save(channel);

        log.info("채널: {}가 수정됨.", savedChannel.getName());
        return toResponse(savedChannel);
    }

    @Override
    public void delete(UUID channelId) {
        if (!channelRepository.existsById(channelId)) {
            throw new NoSuchElementException("채널 ID:  " + channelId + " 를 찾을 수 없습니다.");
        }

        messageRepository.findAll().stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .forEach(message -> {
                    message.getAttachmentIds().forEach(binaryContentRepository::deleteById);
                    messageRepository.deleteById(message.getId());
                });

        readStatusRepository.findAll().stream()
                .filter(readStatus -> readStatus.getChannelId().equals(channelId))
                .forEach(readStatus -> readStatusRepository.deleteById(readStatus.getId()));

        channelRepository.deleteById(channelId);
        log.info("채널: {}가 삭제됨.", channelId);
    }

    private ChannelResponse toResponse(Channel channel) {
        Instant lastMessageAt = messageRepository.findAll().stream()
                .filter(message -> message.getChannelId().equals(channel.getId()))
                .map(Message::getCreatedAt)
                .max(Instant::compareTo)
                .orElse(null);

        List<UUID> participantIds = channel.getType() == ChannelType.PRIVATE
                ? readStatusRepository.findAll().stream()
                .filter(readStatus -> readStatus.getChannelId().equals(channel.getId()))
                .map(ReadStatus::getUserId)
                .toList() : List.of();

        return new ChannelResponse(
                channel.getId(),
                channel.getType(),
                channel.getName(),
                channel.getDescription(),
                channel.getCreatedAt(),
                channel.getUpdatedAt(),
                lastMessageAt,
                participantIds
        );
    }
}
