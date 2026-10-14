package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadStatusService {

  private final ReadStatusRepository repository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final ReadStatusMapper readStatusMapper;

  @Transactional
  public ReadStatusResponse createReadStatus(ReadStatusCreateRequest request) {
    var user = userRepository.findById(request.userId())
        .orElseThrow(() -> new UserNotFoundException("userId", request.userId()));
    var channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> new ChannelNotFoundException(request.channelId()));
    ReadStatus readStatus = new ReadStatus(user, channel, request.lastReadAt());

    repository.save(readStatus);
    return readStatusMapper.toDto(readStatus);
  }

  @Transactional(readOnly = true)
  public ReadStatusResponse findReadStatusById(UUID readStatusId) {
    return readStatusMapper.toDto(getReadStatusOrThrow(readStatusId));
  }

  @Transactional(readOnly = true)
  public List<ReadStatusResponse> findAllReadStatusByUserId(UUID userId) {
    userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("userId", userId));

    return repository.findAllByUserId(userId).stream()
        .map(readStatusMapper::toDto)
        .toList();
  }

  @Transactional
  public ReadStatusResponse updateReadStatus(UUID readStatusId, ReadStatusUpdateRequest request) {
    ReadStatus readStatus = getReadStatusOrThrow(readStatusId);
    readStatus.updateLastReadAt(request.newLastReadAt());
    readStatus.updateNotificationEnabled(request.newNotificationEnabled());
    repository.save(readStatus);
    return readStatusMapper.toDto(readStatus);
  }

  @Transactional
  public void deleteReadStatus(UUID readStatusId) {
    getReadStatusOrThrow(readStatusId);
    repository.deleteById(readStatusId);
  }

  private ReadStatus getReadStatusOrThrow(UUID id) {
    return repository.findById(id)
        .orElseThrow(() -> new ReadStatusNotFoundException(id));
  }
}
