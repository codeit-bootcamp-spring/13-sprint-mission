package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentResponse;
import com.sprint.mission.discodeit.dto.ChannelCreateRequest;
import com.sprint.mission.discodeit.dto.ChannelResponse;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentService binaryContentService;

    // PUBLIC 채널 생성
    @Override
    public ChannelResponse createPublicChannel(ChannelCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("채널 생성 요청은 비어 있을 수 없습니다.");
        }

        Channel channel = new Channel(
                ChannelType.PUBLIC,
                request.getName(),
                request.getDescription()
        );

        Channel savedChannel = channelRepository.save(channel);

        return toResponse(savedChannel);
    }

    // PRIVATE 채널 생성
    @Override
    public ChannelResponse createPrivateChannel(PrivateChannelCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("PRIVATE 채널 생성 요청은 비어 있을 수 없습니다.");
        }

        /*
         * API 명세 v1.2 기준 필드는 participantIds.
         * PrivateChannelCreateRequest에서 getParticipantIds() 호환 getter를 만들어뒀다면
         * 이 메서드를 쓰는 게 가장 안전하다.
         */
        List<UUID> participantUserIds = request.getParticipantIds();

        if (participantUserIds == null || participantUserIds.isEmpty()) {
            throw new IllegalArgumentException("PRIVATE 채널 참여자 목록은 비어 있을 수 없습니다.");
        }

        Set<UUID> uniqueParticipantUserIds = new HashSet<>();

        for (UUID userId : participantUserIds) {
            if (userId == null) {
                throw new IllegalArgumentException("참여자 id는 null일 수 없습니다.");
            }

            if (!uniqueParticipantUserIds.add(userId)) {
                throw new IllegalArgumentException("PRIVATE 채널 참여자 id가 중복되었습니다. userId=" + userId);
            }

            if (!userRepository.existsById(userId)) {
                throw new IllegalArgumentException("PRIVATE 채널 참여자를 찾을 수 없습니다. userId=" + userId);
            }
        }

        Channel channel = new Channel(
                ChannelType.PRIVATE,
                null,
                null
        );

        Channel savedChannel = channelRepository.save(channel);

        for (UUID userId : uniqueParticipantUserIds) {
            ReadStatus readStatus = new ReadStatus(
                    userId,
                    savedChannel.getId()
            );

            readStatusRepository.save(readStatus);
        }

        return toResponse(savedChannel);
    }

    // id로 채널 단건 조회
    @Override
    @Transactional(readOnly = true)
    public ChannelResponse find(UUID id) {
        Channel channel = findChannelById(id);

        return toResponse(channel);
    }

    // 특정 User가 볼 수 있는 채널 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("채널을 조회할 사용자 id는 필수입니다.");
        }

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("채널을 조회할 사용자를 찾을 수 없습니다. userId=" + userId);
        }

        List<Channel> channels = channelRepository.findAll();
        List<ChannelResponse> responses = new ArrayList<>();

        List<UUID> channelIds = readStatusRepository.findChannelIdsByUserId(userId);

        Set<UUID> participatedPrivateChannelIds = channelIds == null
                ? new HashSet<>()
                : new HashSet<>(channelIds);

        for (Channel channel : channels) {
            if (channel.getType() == ChannelType.PUBLIC) {
                responses.add(toResponse(channel));
                continue;
            }

            if (channel.getType() == ChannelType.PRIVATE
                    && participatedPrivateChannelIds.contains(channel.getId())) {
                responses.add(toResponse(channel));
            }
        }

        return responses;
    }

    // 채널 수정
    @Override
    public ChannelResponse update(ChannelUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("채널 수정 요청은 비어 있을 수 없습니다.");
        }

        if (request.getId() == null) {
            throw new IllegalArgumentException("수정할 채널 id는 null일 수 없습니다.");
        }

        Channel channel = findChannelById(request.getId());

        if (channel.getType() == ChannelType.PRIVATE) {
            throw new IllegalArgumentException("PRIVATE 채널은 수정할 수 없습니다.");
        }

        channel.update(
                ChannelType.PUBLIC,
                request.getName(),
                request.getDescription()
        );

        Channel savedChannel = channelRepository.save(channel);

        return toResponse(savedChannel);
    }

    // 채널 삭제
    @Override
    public void delete(UUID id) {
        Channel channel = findChannelById(id);

        deleteMessageAttachmentsByChannelId(channel.getId());

        messageRepository.deleteByChannelId(channel.getId());
        readStatusRepository.deleteByChannelId(channel.getId());
        channelRepository.deleteById(channel.getId());
    }

    // 채널 메시지에 연결된 첨부파일 BinaryContent 삭제
    private void deleteMessageAttachmentsByChannelId(UUID channelId) {
        List<Message> messages = messageRepository.findAllByChannelId(channelId);

        Set<UUID> attachmentIds = new HashSet<>();

        for (Message message : messages) {
            if (message.getAttachmentIds() == null || message.getAttachmentIds().isEmpty()) {
                continue;
            }

            attachmentIds.addAll(message.getAttachmentIds());
        }

        for (UUID attachmentId : attachmentIds) {
            if (attachmentId != null) {
                binaryContentService.delete(attachmentId);
            }
        }
    }

    // Channel 단건 조회 보조 메서드
    private Channel findChannelById(UUID channelId) {
        if (channelId == null) {
            throw new IllegalArgumentException("채널 id는 필수입니다.");
        }

        return channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "채널을 찾을 수 없습니다. id=" + channelId
                ));
    }

    // Channel 엔티티를 ChannelResponse DTO로 변환
    private ChannelResponse toResponse(Channel channel) {
        List<UUID> participantUserIds = getParticipantUserIds(channel);
        List<UserResponse> participants = toParticipantResponses(participantUserIds);

        return new ChannelResponse(
                channel.getId(),
                channel.getCreatedAt(),
                channel.getUpdatedAt(),
                channel.getType(),
                channel.getName(),
                channel.getDescription(),
                getLastMessageAt(channel.getId()),
                participantUserIds,
                participants
        );
    }

    private Instant getLastMessageAt(UUID channelId) {
        return messageRepository.findLastMessageAtByChannelId(channelId);
    }

    private List<UUID> getParticipantUserIds(Channel channel) {
        if (channel.getType() != ChannelType.PRIVATE) {
            return new ArrayList<>();
        }

        List<UUID> participantUserIds =
                readStatusRepository.findUserIdsByChannelId(channel.getId());

        if (participantUserIds == null) {
            return new ArrayList<>();
        }

        return participantUserIds;
    }

    private List<UserResponse> toParticipantResponses(List<UUID> participantUserIds) {
        List<UserResponse> participants = new ArrayList<>();

        if (participantUserIds == null || participantUserIds.isEmpty()) {
            return participants;
        }

        for (UUID userId : participantUserIds) {
            if (userId == null) {
                continue;
            }

            userRepository.findById(userId)
                    .map(this::toUserResponse)
                    .ifPresent(participants::add);
        }

        return participants;
    }

    private UserResponse toUserResponse(User user) {
        BinaryContentResponse profileResponse = toProfileResponse(user.getProfileId());

        return new UserResponse(
                user.getId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileId(),
                profileResponse,
                false
        );
    }

    private BinaryContentResponse toProfileResponse(UUID profileId) {
        if (profileId == null) {
            return null;
        }

        return binaryContentRepository.findById(profileId)
                .map(this::toBinaryContentResponse)
                .orElse(null);
    }

    private BinaryContentResponse toBinaryContentResponse(BinaryContent binaryContent) {
        return new BinaryContentResponse(
                binaryContent.getId(),
                binaryContent.getCreatedAt(),
                binaryContent.getUpdatedAt(),
                binaryContent.getFileName(),
                binaryContent.getContentType(),
                binaryContent.getSize()
        );
    }
}