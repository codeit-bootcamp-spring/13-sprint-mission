package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.realtime.RealtimePublisher;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelPrivateCreateRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelPublicCreateRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.channel.ChannelAlreadyExistsException;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.ChannelUpdateNotAllowedException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicChannelService implements ChannelService {

  private final ChannelRepository repository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelMapper channelMapper;
  private final RealtimePublisher realtime;

  @CacheEvict(cacheNames = "channelsByUser", allEntries = true)
  public void addAllowedUserList(UUID channelId, UUID userId) {
    Channel channel = getChannelOrThrow(channelId);
    channel.addAllowedUserList(userId);
    repository.save(channel);
  }

  @CacheEvict(cacheNames = "channelsByUser", allEntries = true)
  public void disallowChannelList(UUID channelId, UUID userId) {
    Channel channel = getChannelOrThrow(channelId);

    channel.disallowedList(userId);
    repository.save(channel);
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "channelsByUser", allEntries = true)
  public ChannelResponse createPublicChannel(ChannelPublicCreateRequest request) {
    log.info("공개 채널 생성 시작: name={}", request.name());
    // PRIVATE 채널은 name이 null이므로 null-안전 비교가 필요하다.
    boolean isDuplicate = repository.findAll().stream()
        .anyMatch(c -> Objects.equals(c.getName(), request.name()));
    if (isDuplicate) {
      throw new ChannelAlreadyExistsException(request.name());
    }
    Channel channel = request.toEntity();

    repository.save(channel);

    log.info("{} 채널 생성됨", channel.getName());

    return notifyChange("channels.created", channel, channelMapper.toDto(channel, List.of(), null));
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "channelsByUser", allEntries = true)
  public ChannelResponse createPrivateChannel(ChannelPrivateCreateRequest request) {
    log.info("비공개 채널 생성 시작: participantCount={}", request.participantIds().size());

    // 읽음 상태가 참조할 채널을 먼저 저장하고 반환된 영속 엔티티를 사용한다.
    Channel channel = repository.save(request.toEntity());

    List<UUID> userIdList = request.participantIds();
    // toEntity()에서 이미 allowedUserList를 채우므로 중복 추가하지 않는다.
    // 각 유저마다 ReadStatus 생성

    userIdList.forEach(userId -> {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new UserNotFoundException("userId", userId));
      ReadStatus readStatus = new ReadStatus(user, channel, null);
      readStatusRepository.save(readStatus);
    });

    log.info("private 채널 생성됨");
    return notifyChange("channels.created", channel, toResponse(channel));
  }

  @Override
  @Transactional(readOnly = true)
  public ChannelResponse findChannelById(UUID channelId) {
    Channel channel = getChannelOrThrow(channelId);
    return toResponse(channel);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(cacheNames = "channelsByUser", key = "#userId")
  public List<ChannelResponse> findAllByUserId(UUID userId) {
    userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("userId", userId));

    List<Channel> channels = repository.findAll().stream()
        .filter(channel ->
            channel.getType() == ChannelType.PUBLIC
                || channel.getAllowedUserList().contains(userId)
        )
        .toList();

    Map<UUID, User> userById = loadParticipants(channels);
    return channels.stream()
        .map(channel -> channelMapper.toDto(
            channel,
            participantsOf(channel, userById),
            getLastMessageAt(channel)))
        .toList();
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "channelsByUser", allEntries = true)
  public ChannelResponse updateChannel(UUID channelId, ChannelUpdateRequest request) {
    log.info("채널 수정 시작: channelId={}", channelId);
    Channel channel = getChannelOrThrow(channelId);

    // PRIVATE 채널 수정 불가
    if (channel.getType() == ChannelType.PRIVATE) {
      throw new ChannelUpdateNotAllowedException(channelId);
    }

    channel.updateName(request.newName());
    channel.updateDescription(request.newDescription());
    repository.save(channel);
    log.info("채널 수정 완료: channelId={}", channelId);
    return notifyChange("channels.updated", channel, toResponse(channel));
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "channelsByUser", allEntries = true)
  public void deleteChannel(UUID channelId) {
    log.info("채널 삭제 시작: channelId={}", channelId);
    Channel deleted = getChannelOrThrow(channelId);
    notifyChange("channels.deleted", deleted, toResponse(deleted));
    // 연관 Message 삭제
    messageRepository.findAllByChannelId(channelId)
        .forEach(message -> messageRepository.deleteById(message.getId()));

    // 연관 ReadStatus 삭제
    readStatusRepository.findAllByChannelId(channelId)
        .forEach(rs -> readStatusRepository.deleteById(rs.getId()));

    repository.deleteById(channelId);
    log.info("채널 삭제 완료: channelId={}", channelId);
  }

  private ChannelResponse notifyChange(String name, Channel channel, ChannelResponse dto) {
    realtime.sse(channel.getType() == ChannelType.PRIVATE ? channel.getAllowedUserList()
        : channel.getType() == ChannelType.MANAGER
            ? userRepository.findAll().stream().filter(u -> u.getRole() != Role.USER).map(User::getId).toList()
            : null, name, dto);
    return dto;
  }

  private ChannelResponse toResponse(Channel channel) {
    Map<UUID, User> userById = loadParticipants(List.of(channel));
    return channelMapper.toDto(
        channel,
        participantsOf(channel, userById),
        getLastMessageAt(channel));
  }

  private Map<UUID, User> loadParticipants(List<Channel> channels) {
    Set<UUID> participantIds = channels.stream()
        .filter(channel -> channel.getType() == ChannelType.PRIVATE)
        .flatMap(channel -> channel.getAllowedUserList().stream())
        .collect(Collectors.toSet());

    return userRepository.findAllById(participantIds).stream()
        .collect(Collectors.toMap(User::getId, Function.identity()));
  }

  private List<User> participantsOf(Channel channel, Map<UUID, User> userById) {
    if (channel.getType() != ChannelType.PRIVATE) {
      return List.of();
    }

    return channel.getAllowedUserList().stream()
        .map(userId -> {
          User user = userById.get(userId);
          if (user == null) {
            throw new UserNotFoundException("userId", userId);
          }
          return user;
        })
        .toList();
  }

  private Instant getLastMessageAt(Channel channel) {
    return channel.getMessages().stream()
        .map(Message::getCreatedAt)
        .max(Instant::compareTo)
        .orElse(null);
  }

  private Channel getChannelOrThrow(UUID channelId) {
    return repository.findById(channelId)
        .orElseThrow(() -> new ChannelNotFoundException(channelId));
  }

}
