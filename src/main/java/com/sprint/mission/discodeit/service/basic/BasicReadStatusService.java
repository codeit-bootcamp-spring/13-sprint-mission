package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.ReadStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

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
  public ReadStatusDto create(ReadStatusCreateRequest request) {
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new UserNotFoundException(request.userId()));

    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> new ChannelNotFoundException(request.channelId()));

    boolean isAlreadyExist = readStatusRepository.findAll().stream()
        .anyMatch(rs -> rs.getUser().getId().equals(request.userId())
            && rs.getChannel().getId().equals(request.channelId()));

    if (isAlreadyExist) {
      throw new ReadStatusAlreadyExistsException(request.userId(), request.channelId());
    }

    ReadStatus readStatus = new ReadStatus(
        user, channel, request.lastReadAt()
    );

    readStatusRepository.save(readStatus);

    return convertToResponse(readStatus);
  }


  @Override
  public List<ReadStatusDto> findAllByUserId(UUID userId) {
    return readStatusRepository.findAll().stream()
        .filter(rs -> rs.getUser().getId().equals(userId))
        .map(this::convertToResponse)
        .toList();
  }

  @Override
  public ReadStatusDto updateLastReadAt(UUID id, ReadStatusUpdateRequest request) {
    ReadStatus readStatus = readStatusRepository.findById(id)
        .orElseThrow(() -> new ReadStatusNotFoundException(id));

    readStatus.updateLastReadAt(request.newLastReadAt());
    readStatusRepository.save(readStatus);

    return convertToResponse(readStatus);
  }


  private ReadStatusDto convertToResponse(ReadStatus readStatus) {
    return new ReadStatusDto(
        readStatus.getId(),
        readStatus.getUser().getId(),
        readStatus.getChannel().getId(),
        readStatus.getLastReadAt()
    );
  }
}
