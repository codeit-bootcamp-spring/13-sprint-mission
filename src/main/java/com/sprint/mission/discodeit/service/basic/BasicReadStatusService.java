package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusResponse;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.ReadStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Primary
public class BasicReadStatusService implements ReadStatusService {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;

  public BasicReadStatusService(ReadStatusRepository readStatusRepository,
      UserRepository userRepository, ChannelRepository channelRepository) {
    this.readStatusRepository = readStatusRepository;
    this.userRepository = userRepository;
    this.channelRepository = channelRepository;
  }

  @Override
  public ReadStatusResponse create(ReadStatusCreateRequest request) {
    userRepository.findById(request.userId())
        .orElseThrow(() -> new UserNotFoundException(request.userId()));
    channelRepository.findById(request.channelId())
        .orElseThrow(() -> new ChannelNotFoundException(request.channelId()));

    boolean isAlreadyExist = readStatusRepository.findAll().stream()
        .anyMatch(rs -> rs.getUserId().equals(request.userId())
            && rs.getChannelId().equals(request.channelId()));

    if (isAlreadyExist) {
      throw new ReadStatusAlreadyExistsException(request.userId(), request.channelId());
    }

    ReadStatus readStatus = ReadStatus.builder()
        .id(UUID.randomUUID())
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .userId(request.userId())
        .channelId(request.channelId())
        .readAt(Instant.now())
        .build();

    readStatusRepository.save(readStatus);

    return convertToResponse(readStatus);
  }


  @Override
  public List<ReadStatusResponse> findAllByUserId(UUID userId) {
    return readStatusRepository.findAll().stream()
        .filter(rs -> rs.getUserId().equals(userId))
        .map(this::convertToResponse)
        .collect(Collectors.toList());
  }

  @Override
  public ReadStatusResponse update(UUID id, ReadStatusUpdateRequest request) {
    ReadStatus readStatus = readStatusRepository.findById(id)
        .orElseThrow(() -> new ReadStatusNotFoundException(id));

    readStatus.update(request.newLastReadAt());
    readStatusRepository.save(readStatus);

    return convertToResponse(readStatus);
  }


  private ReadStatusResponse convertToResponse(ReadStatus readStatus) {
    return new ReadStatusResponse(
        readStatus.getId(),
        readStatus.getCreatedAt(),
        readStatus.getUpdatedAt(),
        readStatus.getUserId(),
        readStatus.getChannelId(),
        readStatus.getReadAt()
    );
  }
}
