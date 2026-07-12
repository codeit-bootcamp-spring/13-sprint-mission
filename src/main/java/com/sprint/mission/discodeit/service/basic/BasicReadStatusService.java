package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class BasicReadStatusService implements ReadStatusService {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;

  @Override
  public ReadStatus create(UUID userId, UUID channelId, Instant lastReadAt) {
    User user = userRepository.findById(userId)
            .orElseThrow(
                    () -> new NoSuchElementException("User with id " + userId + " does not exist"));
    Channel channel = channelRepository.findById(channelId)
            .orElseThrow(
                    () -> new NoSuchElementException("Channel with id " + channelId + " does not exist"));

    if (readStatusRepository.findByUser_IdAndChannel_Id(userId, channelId).isPresent()) {
      throw new IllegalArgumentException(
              "ReadStatus with userId " + userId + " and channelId " + channelId + " already exists");
    }

    ReadStatus readStatus = new ReadStatus(user, channel, lastReadAt);
    return readStatusRepository.save(readStatus);
  }

  @Transactional(readOnly = true)
  @Override
  public ReadStatus find(UUID readStatusId) {
    return readStatusRepository.findById(readStatusId)
            .orElseThrow(
                    () -> new NoSuchElementException("ReadStatus with id " + readStatusId + " not found"));
  }

  @Transactional(readOnly = true)
  @Override
  public List<ReadStatus> findAllByUserId(UUID userId) {
    return readStatusRepository.findAllByUser_Id(userId).stream().toList();
  }

  @Override
  public ReadStatus update(UUID readStatusId, Instant newLastReadAt) {
    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
            .orElseThrow(
                    () -> new NoSuchElementException("ReadStatus with id " + readStatusId + " not found"));
    readStatus.update(newLastReadAt);
    return readStatusRepository.save(readStatus);
  }

  @Override
  public void delete(UUID readStatusId) {
    if (!readStatusRepository.existsById(readStatusId)) {
      throw new NoSuchElementException("ReadStatus with id " + readStatusId + " not found");
    }
    readStatusRepository.deleteById(readStatusId);
  }
}