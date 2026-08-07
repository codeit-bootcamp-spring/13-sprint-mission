package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUnmodifiableException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
@Transactional
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelMapper channelMapper;
  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;

  @Override
  public ChannelDto createPublicChannel(PublicChannelCreateRequest request) {
    log.debug("공개 채널 생성 시작");

    Channel channel = new Channel(
        request.name(),
        request.description(),
        Channel.ChannelType.PUBLIC
    );
    channelRepository.save(channel);

    log.info("공개 채널 생성 완료: channelId={}", channel.getId());

    return channelMapper.toDto(
        channel,
        List.of(),
        null
    );
  }

  @Override
  public ChannelDto createPrivateChannel(
      PrivateChannelCreateRequest request
  ) {
    log.debug(
        "비공개 채널 생성 시작: participantCount={}",
        request.participantIds().size()
    );

    Channel channel = new Channel(
        null,
        null,
        ChannelType.PRIVATE
    );

    channelRepository.save(channel);

    List<User> participants = new ArrayList<>();

    for (UUID userId : request.participantIds()) {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> {
            log.warn(
                "비공개 채널 생성 실패: 참여자를 찾을 수 없음, userId={}",
                userId
            );
            return new UserNotFoundException(userId);
          });

      participants.add(user);

      ReadStatus readStatus = new ReadStatus(
          user,
          channel,
          Instant.now()
      );

      readStatusRepository.save(readStatus);
    }

    log.info(
        "비공개 채널 생성 완료: channelId={}, participantCount={}",
        channel.getId(),
        participants.size()
    );

    return channelMapper.toDto(
        channel,
        participants,
        null
    );
  }


  @Transactional(readOnly = true)
  @Override
  public List<ChannelDto> findAllByUserId(UUID userId) {
    log.debug("사용자별 채널 목록 조회 시작: userId={}", userId);

    List<Channel> channels = new ArrayList<>(
        channelRepository.findByType(ChannelType.PUBLIC)
    );

    List<ReadStatus> privateReadStatuses =
        readStatusRepository.findByUser_IdAndChannel_Type(userId, ChannelType.PRIVATE);

    channels.addAll(privateReadStatuses.stream()
        .map(ReadStatus::getChannel)
        .toList()
    );

    if (channels.isEmpty()) {
      log.debug("사용자별 채널 목록 조회 완료: userId={}, count=0", userId);
    
      return List.of();
    }

    List<UUID> channelIds = channels.stream()
        .map(Channel::getId)
        .toList();

    Map<UUID, List<User>> participantsByChannelId =
        readStatusRepository.findByChannel_IdIn(channelIds).stream()
            .collect(Collectors.groupingBy(
                readStatus -> readStatus.getChannel().getId(),
                Collectors.mapping(
                    ReadStatus::getUser,
                    Collectors.toList()
                )
            ));

    Map<UUID, Instant> lastMessageAtByChannelId =
        messageRepository.findLastMessageAtByChannelIds(channelIds).stream()
            .collect(Collectors.toMap(
                row -> (UUID) row[0],
                row -> (Instant) row[1]
            ));

    List<ChannelDto> responses = channels.stream()
        .map(channel -> channelMapper.toDto(
            channel,
            participantsByChannelId.getOrDefault(
                channel.getId(),
                List.of()
            ),
            lastMessageAtByChannelId.get(channel.getId())
        ))
        .toList();

    log.debug(
        "사용자별 채널 목록 조회 완료: userId={}, count={}",
        userId,
        responses.size()
    );

    return responses;
  }

  @Transactional(readOnly = true)
  @Override
  public ChannelDto findById(UUID id) {
    log.debug("채널 단건 조회 시작: channelId={}", id);

    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("채널 조회 실패: channelId={}를 찾을 수 없음", id);
          return new ChannelNotFoundException(id);
        });

    log.debug("채널 단건 조회 완료: channelId={}", id);

    return toDto(channel);
  }

  @Override
  public ChannelDto update(UUID id, ChannelUpdateRequest request) {
    log.debug("채널 수정 시작: channelId={}", id);

    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("채널 수정 실패: channelId={}를 찾을 수 없음", id);
          return new ChannelNotFoundException(id);
        });

    if (channel.getType() == Channel.ChannelType.PRIVATE) {
      log.warn("비공개 채널 수정 시도: channelId={}", id);
      throw new PrivateChannelUnmodifiableException(id);
    }

    channel.update(request.newName(), request.newDescription());

    log.info("채널 수정 완료: channelId={}", id);

    return toDto(channel);
  }

  @Override
  public void delete(UUID id) {
    log.debug("채널 삭제 시작: channelId={}", id);

    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("채널 삭제 실패: channelId={}를 찾을 수 없음", id);
          return new ChannelNotFoundException(id);
        });

    List<Message> messages = messageRepository.findByChannel_Id(id);

    log.debug(
        "채널 관련 메시지 삭제 시작: channelId={}, messageCount={}",
        id,
        messages.size()
    );

    for (Message message : messages) {
      if (message.getAttachments() != null) {
        for (BinaryContent attachment : message.getAttachments()) {
          UUID attachmentId = attachment.getId();

          binaryContentStorage.delete(attachmentId);
          binaryContentRepository.deleteById(attachmentId);
        }
      }

      messageRepository.delete(message);
    }

    readStatusRepository.deleteByChannel_Id(id);
    channelRepository.delete(channel);

    log.info("채널 삭제 완료: channelId={}", id);
  }

  private ChannelDto toDto(Channel channel) {
    List<User> participants =
        readStatusRepository.findByChannel_Id(channel.getId()).stream()
            .map(ReadStatus::getUser)
            .toList();

    Instant lastMessageAt =
        messageRepository
            .findTopByChannel_IdOrderByCreatedAtDesc(channel.getId())
            .map(Message::getCreatedAt)
            .orElse(null);

    return channelMapper.toDto(
        channel,
        participants,
        lastMessageAt
    );
  }
}
