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
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final BinaryContentService binaryContentService;

    @Override
    public ChannelResponse createPublicChannel(ChannelCreateRequest request) {
        log.info(
                "공개 채널 생성을 시작합니다. name={}",
                request == null ? null : request.getName()
        );

        if (request == null) {
            log.warn("공개 채널 생성 요청이 비어 있습니다.");
            throw new IllegalArgumentException("채널 생성 요청은 비어 있을 수 없습니다.");
        }

        try {
            log.debug(
                    "공개 채널 생성 요청을 처리합니다. name={}, description={}",
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
                    "공개 채널 생성이 완료되었습니다. channelId={}, name={}",
                    savedChannel.getId(),
                    savedChannel.getName()
            );

            return toResponse(savedChannel);

        } catch (RuntimeException e) {
            log.error(
                    "공개 채널 생성 중 오류가 발생했습니다. name={}",
                    request.getName(),
                    e
            );
            throw e;
        }
    }

    @Override
    public ChannelResponse createPrivateChannel(PrivateChannelCreateRequest request) {
        log.info("비공개 채널 생성을 시작합니다.");

        if (request == null) {
            log.warn("비공개 채널 생성 요청이 비어 있습니다.");
            throw new IllegalArgumentException("PRIVATE 채널 생성 요청은 비어 있을 수 없습니다.");
        }

        List<UUID> participantUserIds = request.getParticipantIds();

        if (participantUserIds == null || participantUserIds.isEmpty()) {
            log.warn("비공개 채널 참여자 목록이 비어 있습니다.");
            throw new IllegalArgumentException("PRIVATE 채널 참여자 목록은 비어 있을 수 없습니다.");
        }

        log.debug(
                "비공개 채널 참여자를 검증합니다. participantCount={}, participantIds={}",
                participantUserIds.size(),
                participantUserIds
        );

        Set<UUID> uniqueParticipantUserIds = new HashSet<>();

        for (UUID userId : participantUserIds) {
            if (userId == null) {
                log.warn("비공개 채널 참여자 id가 null입니다.");
                throw new IllegalArgumentException("참여자 id는 null일 수 없습니다.");
            }

            if (!uniqueParticipantUserIds.add(userId)) {
                log.warn(
                        "비공개 채널 참여자 id가 중복되었습니다. userId={}",
                        userId
                );
                throw new IllegalArgumentException(
                        "PRIVATE 채널 참여자 id가 중복되었습니다. userId=" + userId
                );
            }

            if (!userRepository.existsById(userId)) {
                log.warn(
                        "비공개 채널 참여자를 찾을 수 없습니다. userId={}",
                        userId
                );

                throw new UserNotFoundException(userId);
            }
        }

        try {
            Channel channel = new Channel(
                    ChannelType.PRIVATE,
                    null,
                    null
            );

            Channel savedChannel = channelRepository.save(channel);

            for (UUID userId : uniqueParticipantUserIds) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> {
                            log.warn(
                                    "ReadStatus 생성 중 참여자를 찾을 수 없습니다. userId={}",
                                    userId
                            );

                            return new UserNotFoundException(userId);
                        });

                ReadStatus readStatus = new ReadStatus(
                        user,
                        savedChannel
                );

                readStatusRepository.save(readStatus);
            }

            log.info(
                    "비공개 채널 생성이 완료되었습니다. channelId={}, participantCount={}",
                    savedChannel.getId(),
                    uniqueParticipantUserIds.size()
            );

            return toResponse(savedChannel);

        } catch (RuntimeException e) {
            log.error(
                    "비공개 채널 생성 중 오류가 발생했습니다. participantCount={}",
                    uniqueParticipantUserIds.size(),
                    e
            );
            throw e;
        }
    }


    @Override
    @Transactional(readOnly = true)
    public ChannelResponse find(UUID id) {
        Channel channel = findChannelById(id);

        return toResponse(channel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("채널을 조회할 사용자 id는 필수입니다.");
        }

        if (!userRepository.existsById(userId)) {
            log.warn(
                    "채널을 조회할 사용자를 찾을 수 없습니다. userId={}",
                    userId
            );

            throw new UserNotFoundException(userId);
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

    @Override
    public ChannelResponse update(ChannelUpdateRequest request) {
        log.info(
                "채널 수정을 시작합니다. channelId={}",
                request == null ? null : request.getId()
        );

        if (request == null) {
            log.warn("채널 수정 요청이 비어 있습니다.");
            throw new IllegalArgumentException("채널 수정 요청은 비어 있을 수 없습니다.");
        }

        if (request.getId() == null) {
            log.warn("수정할 채널 id가 null입니다.");
            throw new IllegalArgumentException("수정할 채널 id는 null일 수 없습니다.");
        }

        Channel channel = findChannelById(request.getId());

        if (channel.getType() == ChannelType.PRIVATE) {
            log.warn(
                    "비공개 채널 수정 요청을 거부합니다. channelId={}",
                    request.getId()
            );

            throw new PrivateChannelUpdateException(
                    request.getId()
            );
        }

        try {
            log.debug(
                    "채널 수정 요청을 처리합니다. channelId={}, name={}, description={}",
                    request.getId(),
                    request.getName(),
                    request.getDescription()
            );

            channel.update(
                    ChannelType.PUBLIC,
                    request.getName(),
                    request.getDescription()
            );

            Channel savedChannel = channelRepository.save(channel);

            log.info(
                    "채널 수정이 완료되었습니다. channelId={}",
                    savedChannel.getId()
            );

            return toResponse(savedChannel);

        } catch (RuntimeException e) {
            log.error(
                    "채널 수정 중 오류가 발생했습니다. channelId={}",
                    request.getId(),
                    e
            );
            throw e;
        }
    }

    @Override
    public void delete(UUID id) {
        log.info("채널 삭제를 시작합니다. channelId={}", id);

        Channel channel = findChannelById(id);

        try {
            log.debug(
                    "채널 관련 데이터를 삭제합니다. channelId={}, channelType={}",
                    channel.getId(),
                    channel.getType()
            );

            deleteMessageAttachmentsByChannelId(channel.getId());

            messageRepository.deleteByChannel_Id(channel.getId());
            readStatusRepository.deleteByChannel_Id(channel.getId());
            channelRepository.deleteById(channel.getId());

            log.info(
                    "채널 삭제가 완료되었습니다. channelId={}",
                    channel.getId()
            );

        } catch (RuntimeException e) {
            log.error(
                    "채널 삭제 중 오류가 발생했습니다. channelId={}",
                    id,
                    e
            );
            throw e;
        }
    }

    private void deleteMessageAttachmentsByChannelId(UUID channelId) {
        log.debug(
                "채널 메시지 첨부파일 삭제를 시작합니다. channelId={}",
                channelId
        );

        List<Message> messages =
                messageRepository.findAllByChannel_Id(channelId);

        log.debug(
                "채널 메시지를 조회했습니다. channelId={}, messageCount={}",
                channelId,
                messages.size()
        );

        Set<UUID> attachmentIds = new HashSet<>();

        for (Message message : messages) {
            List<UUID> messageAttachmentIds = message.getAttachmentIds();

            if (messageAttachmentIds == null || messageAttachmentIds.isEmpty()) {
                continue;
            }

            attachmentIds.addAll(messageAttachmentIds);
        }

        log.debug(
                "삭제할 채널 메시지 첨부파일을 수집했습니다. channelId={}, attachmentCount={}",
                channelId,
                attachmentIds.size()
        );

        for (UUID attachmentId : attachmentIds) {
            if (attachmentId == null) {
                continue;
            }

            log.debug(
                    "채널 메시지 첨부파일을 삭제합니다. channelId={}, attachmentId={}",
                    channelId,
                    attachmentId
            );

            binaryContentService.delete(attachmentId);
        }

        log.info(
                "채널 메시지 첨부파일 삭제가 완료되었습니다. channelId={}, deletedCount={}",
                channelId,
                attachmentIds.size()
        );
    }

    private Channel findChannelById(UUID channelId) {
        if (channelId == null) {
            log.warn(
                    "채널 조회에 실패했습니다. channelId가 null입니다."
            );

            throw new IllegalArgumentException(
                    "채널 id는 필수입니다."
            );
        }

        return channelRepository.findById(channelId)
                .orElseThrow(() -> {
                    log.warn(
                            "채널을 찾을 수 없습니다. channelId={}",
                            channelId
                    );

                    return new ChannelNotFoundException(channelId);
                });
    }

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
        BinaryContentResponse profileResponse =
                toBinaryContentResponse(user.getProfile());

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

    private BinaryContentResponse toBinaryContentResponse(BinaryContent binaryContent) {
        if (binaryContent == null) {
            return null;
        }

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