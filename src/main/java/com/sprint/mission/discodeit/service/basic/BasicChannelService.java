package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.ChannelTypeNotAllowedException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelMapper channelMapper;

  @Transactional
  @Override
  public ChannelDto create(PublicChannelCreateRequest request) {
    log.debug("PUBLIC 채널 생성 요청 - name={}", request.name());

    Channel channel = new Channel(ChannelType.PUBLIC, request.name(), request.description());
    channelRepository.save(channel);

    log.info("PUBLIC 채널 생성 완료 - channelId={}, name={}", channel.getId(), request.name());
    return channelMapper.toDto(channel);
  }

  @Transactional
  @Override
  public ChannelDto create(PrivateChannelCreateRequest request) {
    log.debug("PRIVATE 채널 생성 요청 - 참여자 수={}", request.participantIds().size());

    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    channelRepository.save(channel);

    List<ReadStatus> readStatuses = userRepository.findAllById(request.participantIds()).stream()
            .map(user -> new ReadStatus(user, channel, channel.getCreatedAt()))
            .toList();
    readStatusRepository.saveAll(readStatuses);

    log.info("PRIVATE 채널 생성 완료 - channelId={}, 참여자 수={}",
            channel.getId(), readStatuses.size());
    return channelMapper.toDto(channel);
  }

  @Transactional(readOnly = true)
  @Override
  public ChannelDto find(UUID channelId) {
    log.debug("채널 단건 조회 - channelId={}", channelId);
    return channelRepository.findById(channelId)
            .map(channelMapper::toDto)
            .orElseThrow(() -> new ChannelNotFoundException(channelId));
  }

  @Transactional(readOnly = true)
  @Override
  public List<ChannelDto> findAllByUserId(UUID userId) {
    log.debug("사용자별 채널 목록 조회 - userId={}", userId);

    List<UUID> mySubscribedChannelIds = readStatusRepository.findAllByUserId(userId).stream()
            .map(ReadStatus::getChannel)
            .map(Channel::getId)
            .toList();

    List<ChannelDto> channels = channelRepository
            .findAllByTypeOrIdIn(ChannelType.PUBLIC, mySubscribedChannelIds)
            .stream()
            .map(channelMapper::toDto)
            .toList();

    log.debug("채널 목록 조회 완료 - userId={}, 채널 수={}", userId, channels.size());
    return channels;
  }

  @Transactional
  @Override
  public ChannelDto update(UUID channelId, PublicChannelUpdateRequest request) {
    log.debug("채널 수정 요청 - channelId={}", channelId);

    Channel channel = channelRepository.findById(channelId)
            .orElseThrow(() -> new ChannelNotFoundException(channelId));

    if (channel.getType().equals(ChannelType.PRIVATE)) {
      // WARN: 비정상적인 요청이지만 시스템 에러는 아님
      throw new ChannelTypeNotAllowedException(channelId);
    }

    channel.update(request.newName(), request.newDescription());
    log.info("채널 수정 완료 - channelId={}, newName={}", channelId, request.newName());
    return channelMapper.toDto(channel);
  }

  @Transactional
  @Override
  public void delete(UUID channelId) {
    log.debug("채널 삭제 요청 - channelId={}", channelId);

    if (!channelRepository.existsById(channelId)) {
      log.warn("채널 삭제 실패 - 존재하지 않는 channelId={}", channelId);
      throw new ChannelNotFoundException(channelId);
    }

    messageRepository.deleteAllByChannelId(channelId);
    readStatusRepository.deleteAllByChannelId(channelId);
    channelRepository.deleteById(channelId);

    log.info("채널 삭제 완료 - channelId={}", channelId);
  }
}