package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
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

@RequiredArgsConstructor
@Service
public class BasicReadStatusService implements ReadStatusService {

  // 의존성 주입
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;

  @Override
  public ReadStatusResponse create(ReadStatusCreateRequest request) {
    // 관련된 Channel이나 User가 존재하지 않으면 예외 발생
    channelRepository.findById(request.getChannelId())
        .orElseThrow(() -> new NoSuchElementException(
            "Channel with id " + request.getChannelId() + " not found"));
    userRepository.findById(request.getUserId())
        .orElseThrow(() -> new NoSuchElementException(
            "User with id " + request.getUserId() + " not found"));
    // 같은 Channel과 User와 관련된 객체가 이미 존재하면 예외 발생
    readStatusRepository.findAllByUserId(request.getUserId())
        .stream() // 채널 뿐만 아니라 User와도 관련된 객체도 다루도록 한다
        .filter(readStatus -> readStatus.getChannelId().equals(request.getChannelId()))
        .findFirst()
        .ifPresent(readStatus -> {
          throw new IllegalArgumentException(
              "ReadStatus with userId " + request.getUserId() + " and channelId "
                  + request.getChannelId() + " already exists");
        });
    Instant lastReadAt = request.getLastReadAt();
    ReadStatus saved = readStatusRepository.save(
        new ReadStatus(request.getUserId(), request.getChannelId(), lastReadAt));

    return ReadStatusResponse.from(saved);
  }

  @Override
  public ReadStatusResponse find(UUID readStatusId) { // id로 조회
    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(
            () -> new NoSuchElementException("ReadStatus with id " + readStatusId + " not found"));
    return ReadStatusResponse.from(readStatus);
  }

  @Override
  public List<ReadStatusResponse> findAllByUserId(UUID userId) { // userId를 조건으로 조회
    return readStatusRepository.findAllByUserId(userId).stream()
        .map(ReadStatusResponse::from)
        .toList();
  }

  @Override
  public ReadStatusResponse update(UUID readStatusId, ReadStatusUpdateRequest request) {
    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(
            () -> new NoSuchElementException("ReadStatus with id " + readStatusId + " not found"));
    readStatus.update(request.getNewLastReadAt());
    readStatusRepository.save(readStatus);
    return ReadStatusResponse.from(readStatus);
  }

  @Override
  public void delete(UUID readStatusId) { // id로 삭제
    if (!readStatusRepository.existById(readStatusId)) {
      throw new NoSuchElementException("ReadStatus with id " + readStatusId + " not found");
    }
    readStatusRepository.deleteById(readStatusId);
  }
}
