package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor // 생성자를 @RequiredArgsConstructor로 대체
@Service // Basic*Service 구현체를 Service 인터페이스의 Bean으로 등록
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  // 의존성 주입

  // PRIVATE, PUBLIC 채널 생성 메소드 분리
  // PUBLIC 채널 생성할 때는 기존 로직 유지
  @Override
  public ChannelDto createPublicChannel(PublicChannelCreateRequest request) {
    Channel channel = new Channel(ChannelType.PUBLIC, request.getName(), request.getDescription());
    channelRepository.save(channel);
    return ChannelDto.from(channel, null, null);
  }

  // PRIVATE 채널 생성할 때 채널에 참여하는 User 정보 받아 User 별 ReadStatus 정보 생성 (name, description 속성 생략)
  @Override
  public ChannelDto createPrivateChannel(PrivateChannelCreateRequest request) {
    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    Channel createdChannel = channelRepository.save(channel);
    // 채널 생성
    request.getParticipantIds().stream()
        .map(userId -> new ReadStatus(userId, createdChannel.getId(), Instant.MIN))
        .forEach(readStatusRepository::save);
    return ChannelDto.from(channel, Instant.MIN, request.getParticipantIds());
  }

  @Override
  public ChannelDto find(UUID channelId) {
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(
            () -> new NoSuchElementException("Channel with id " + channelId + " not found"));
    List<Message> allMessages = messageRepository.findAllByChannelId(channelId);
    Instant lastMessageAt = allMessages.stream()
        .map(Message::getUpdatedAt)
        .max(Comparator.naturalOrder())
        .orElse(null); // 메시지가 없을 때 null 처리
    // PRIVATE 채널인 경우 참여한 User id 정보 포함
    List<UUID> userIds = null;
    if (channel.getType() == ChannelType.PRIVATE) {
      userIds = readStatusRepository.findAllByChannelId(channelId).stream()
          .map(ReadStatus::getUserId)
          .toList();
    }
    return ChannelDto.from(channel, lastMessageAt, userIds); // 가장 최근 메시지의 시간 정보 포함
  }

  @Override
  public List<ChannelDto> findAllByUserId(UUID userId) { // 특정 User가 볼 수 있는 Channel 목록 조회
    List<UUID> participantsId = readStatusRepository.findAllByUserId(userId).stream()
        .map(ReadStatus::getChannelId)
        .toList();
    // PUBLIC: 전체 조회, PRIVATE: User 참여 채널 조회
    return channelRepository.findAll().stream()
        .filter(channel -> channel.getType() == ChannelType.PUBLIC || participantsId.contains(
            channel.getId()))
        .map(channel -> {
          Instant lastMessageAt = messageRepository.findAllByChannelId(channel.getId()).stream()
              .map(Message::getUpdatedAt)
              .max(Comparator.naturalOrder())
              .orElse(null);
          // PRIVATE 채널인 경우 참여한 User id 정보 포함
          List<UUID> userIds = null;
          if (channel.getType() == ChannelType.PRIVATE) {
            userIds = readStatusRepository.findAllByChannelId(channel.getId()).stream()
                .map(ReadStatus::getUserId)
                .toList();
          }
          return ChannelDto.from(channel, lastMessageAt, userIds);
        }).toList();
  }

  @Override
  public ChannelDto update(UUID channelId, PublicChannelUpdateRequest request) {
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(
            () -> new NoSuchElementException(
                "Channel with id" + channelId + " not found")); // 제공된 API 스펙과 맞추어 수정
    // PRIVATE 채널은 수정 불가능
    if (channel.getType() == ChannelType.PRIVATE) {
      throw new IllegalArgumentException("Private channel cannot be updated"); // 제공된 API 스펙과 맞추어 수정
    }
    channel.update(request.getNewName(), request.getNewDescription());
    channelRepository.save(channel);
    return ChannelDto.from(channel, null, null);
  }

  @Override
  public void delete(UUID channelId) {
    if (!channelRepository.existById(channelId)) {
      throw new NoSuchElementException("Channel with id" + channelId + " not found");
    }
    // 관련된 도메인도 삭제: message, readStatus
    messageRepository.findAllByChannelId(channelId)
        .forEach(message -> messageRepository.deleteById(message.getId()));
    readStatusRepository.findAllByChannelId(channelId)
        .forEach(readStatus -> readStatusRepository.deleteById(readStatus.getId()));
    channelRepository.deleteById(channelId);
  }
}
