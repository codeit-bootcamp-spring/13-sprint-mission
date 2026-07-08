package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final ReadStatusMapper readStatusMapper;

  @Override
  @Transactional
  public ReadStatusDto create(ReadStatusCreateRequest request) {
    log.info("ReadStatus 생성 요청 - userId: {}, channelId: {}", request.userId(), request.channelId());

    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 userId 입니다."));
    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 channelId 입니다."));

    if (readStatusRepository.existsByUser_IdAndChannel_Id(request.userId(), request.channelId())) {
      throw new IllegalArgumentException("이미 존재하는 ReadStatus입니다.");
    }

    ReadStatus readStatus = new ReadStatus(user, channel, request.lastReadAt());
    readStatusRepository.save(readStatus);

    log.info("ReadStatus 생성 완료 - readStatusId: {}", readStatus.getId());
    return readStatusMapper.toDto(readStatus);
  }

  @Override
  @Transactional(readOnly = true)
  public ReadStatusDto find(UUID readStatusId) {
    log.info("ReadStatus 단건 조회 요청 - readStatusId: {}", readStatusId);

    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 readStatusId 입니다."));

    log.info("ReadStatus 단건 조회 완료 - readStatusId: {}", readStatusId);
    return readStatusMapper.toDto(readStatus);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReadStatusDto> findAllByUserId(UUID userId) {
    log.info("ReadStatus 목록 조회 - userId: {}", userId);

    List<ReadStatus> allByUserId = readStatusRepository.findAllByUser_Id(userId);
    List<ReadStatusDto> result = allByUserId.stream()
        .map(readStatusMapper::toDto)
        .toList();

    log.info("ReadStatus 목록 조회 완료 - 조회된 수: {}", result.size());
    return result;
  }

  @Override
  @Transactional
  public ReadStatusDto update(UUID readStatusId, ReadStatusUpdateRequest request) {
    log.info("ReadStatus 수정 요청 - readStatusId: {}", readStatusId);

    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 readStatusId 입니다."));
    readStatus.updateLastReadAt(request.newLastReadAt());

    log.info("ReadStatus 수정 완료 - readStatusId: {}", readStatusId);
    return readStatusMapper.toDto(readStatus);
  }

  @Override
  @Transactional
  public void delete(UUID readStatusId) {
    log.info("ReadStatus 삭제 요청 - readStatusId: {}", readStatusId);

    if (!readStatusRepository.existsById(readStatusId)) {
      throw new NoSuchElementException("존재하지 않는 readStatusId 입니다.");
    }
    readStatusRepository.deleteById(readStatusId);
    
    log.info("ReadStatus 삭제 완료 - readStatusId: {}", readStatusId);
  }
}