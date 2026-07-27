package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicReadStatusService implements ReadStatusService {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final ReadStatusMapper readStatusMapper;


  @Override
  public ReadStatusDto create(ReadStatusCreateRequest request) {
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new UserNotFoundException(request.userId()));

    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> new ChannelNotFoundException(request.channelId()));

    boolean isAlreadyExist = readStatusRepository.findByUser_IdAndChannel_Id(
        request.userId(),
        request.channelId()
    ).isPresent();

    if (isAlreadyExist) {
      throw new ReadStatusAlreadyExistsException(request.userId(), request.channelId());
    }

    ReadStatus readStatus = new ReadStatus(
        user, channel, request.lastReadAt()
    );

    readStatusRepository.save(readStatus);

    return readStatusMapper.toDto(readStatus);
  }


  @Transactional(readOnly = true)
  @Override
  public List<ReadStatusDto> findAllByUserId(UUID userId) {
    return readStatusRepository.findByUser_Id(userId).stream()
        .map(readStatus -> readStatusMapper.toDto(readStatus))
        .toList();
  }

  @Override
  public ReadStatusDto updateLastReadAt(UUID readStatusId, ReadStatusUpdateRequest request) {
    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(() -> new ReadStatusNotFoundException(readStatusId));

    readStatus.updateLastReadAt(request.newLastReadAt());

    return readStatusMapper.toDto(readStatus);
  }

  @Override
  public void delete(UUID readStatusId) {
    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(() -> new ReadStatusNotFoundException(readStatusId));

    readStatusRepository.delete(readStatus);
  }

  @Transactional(readOnly = true)
  @Override
  public ReadStatusDto findById(UUID readStatusId) {
    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(() -> new ReadStatusNotFoundException(readStatusId));

    return readStatusMapper.toDto(readStatus);
  }
}
