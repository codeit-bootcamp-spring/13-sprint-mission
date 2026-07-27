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
import java.util.List;
import java.util.UUID;
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

    return channelMapper.toDto(channel);
  }

  @Override
  public ChannelDto createPrivateChannel(PrivateChannelCreateRequest request) {
    log.debug(
        "비공개 채널 생성 시작: participantCount={}",
        request.participantIds().size()
    );

    Channel channel = new Channel(null, null, Channel.ChannelType.PRIVATE);
    channelRepository.save(channel);

    for (UUID userId : request.participantIds()) {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> {
            log.warn(
                "비공개 채널 생성 실패: 참여자를 찾을 수 없음, userId={}",
                userId
            );
            return new UserNotFoundException(userId);
          });

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
        request.participantIds().size()
    );

    return channelMapper.toDto(channel);
  }


  @Transactional(readOnly = true)
  @Override
  public List<ChannelDto> findAllByUserId(UUID userId) {
    log.debug("사용자별 채널 목록 조회 시작: userId={}", userId);

    List<UUID> joinedChannelIds =
        readStatusRepository.findByUser_Id(userId).stream()
            .map(readStatus -> readStatus.getChannel().getId())
            .toList();

    List<ChannelDto> channels = channelRepository.findAll().stream()
        .filter(channel ->
            channel.getType() == ChannelType.PUBLIC
                || joinedChannelIds.contains(channel.getId())
        )
        .map(channelMapper::toDto)
        .toList();

    log.debug(
        "사용자별 채널 목록 조회 완료: userId={}, count={}",
        userId,
        channels.size()
    );

    return channels;
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

    return channelMapper.toDto(channel);
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
      throw new PrivateChannelUnmodifiableException(channel.getId());
    }

    channel.update(request.newName(), request.newDescription());

    log.info("채널 수정 완료: channelId={}", id);

    return channelMapper.toDto(channel);
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

          binaryContentRepository.deleteById(attachmentId);
          binaryContentStorage.delete(attachmentId);
        }
      }

      messageRepository.delete(message);
    }

    readStatusRepository.deleteByChannel_Id(id);
    channelRepository.delete(channel);

    log.info("채널 삭제 완료: channelId={}", id);
  }
}
