package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class BasicReadStatusService implements ReadStatusService {

  // 의존성 주입
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final ReadStatusMapper readStatusMapper;

  @Transactional
  @Override
  public ReadStatusDto create(ReadStatusCreateRequest request) {
    // 관련된 Channel이나 User가 존재하지 않으면 예외 발생
    Channel channel = channelRepository.findById(request.getChannelId())
        .orElseThrow(() -> new NoSuchElementException(
            "Channel with id " + request.getChannelId() + " not found"));
    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> new NoSuchElementException(
            "User with id " + request.getUserId() + " not found"));
    // 같은 Channel과 User와 관련된 객체가 이미 존재하면 예외 발생
    if (readStatusRepository.existsByUserIdAndChannelId(request.getUserId(),
        request.getChannelId())) { // 쿼리 메서드 활용
      throw new IllegalArgumentException(
          "ReadStatus with userId " + request.getUserId() + " and channelId "
              + request.getChannelId() + " already exists");
    }
    ReadStatus saved = readStatusRepository.save(
        new ReadStatus(user, channel, request.getLastReadAt()));

    return readStatusMapper.toDto(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public ReadStatusDto find(UUID readStatusId) { // id로 조회
    return readStatusRepository.findById(readStatusId)
        .map(status -> readStatusMapper.toDto(status))
        .orElseThrow(
            () -> new NoSuchElementException("ReadStatus with id " + readStatusId + " not found"));
  }

  @Transactional(readOnly = true)
  @Override
  public List<ReadStatusDto> findAllByUserId(UUID userId) { // userId를 조건으로 조회
    return readStatusRepository.findByUserId(userId).stream()
        .map(readStatusMapper::toDto)
        .toList();
  }

  @Transactional
  @Override
  public ReadStatusDto update(UUID readStatusId, ReadStatusUpdateRequest request) {
    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(
            () -> new NoSuchElementException("ReadStatus with id " + readStatusId + " not found"));
    readStatus.update(request.getNewLastReadAt());
    return readStatusMapper.toDto(readStatus);
  }

  @Transactional
  @Override
  public void delete(UUID readStatusId) { // id로 삭제
    if (!readStatusRepository.existsById(readStatusId)) {
      throw new NoSuchElementException("ReadStatus with id " + readStatusId + " not found");
    }
    readStatusRepository.deleteById(readStatusId);
  }
}
