package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepesitory;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {
    private ReadStatusRepesitory readStatusRepesitory;
    private UserRepository userRepository;
    private ChannelRepository channelRepository;

    @Override
    public ReadStatus create(ReadStatusCreateRequest request) {
        UUID userId = request.userId();
        UUID channelId = request.channelId();

        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("user with id " + userId + " does not exist");
        }
        if (!channelRepository.existsById(channelId)) {
            throw new NoSuchElementException("channel with id " + channelId + " does not exist");
        }
        if (readStatusRepesitory.findAllByUserId(userId).stream()
                .anyMatch(readStatus -> readStatus.getChannelId().equals(channelId))) {
            throw new NoSuchElementException("channel with userId " + userId + " and channelId " + channelId+ " already exists");
        }
        Instant lastReadAt = request.lastReadAt();
        ReadStatus readStatus = new ReadStatus(userId, channelId, lastReadAt);
        return  readStatusRepesitory.save(readStatus);
    }

    @Override
    public ReadStatus find(UUID readStatusId) {
        return readStatusRepesitory.findById(readStatusId)
                .orElseThrow(() -> new NoSuchElementException("readStatus with id " + readStatusId + " does not exist"));
    }
     @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        return readStatusRepesitory.findAllByUserId(userId).stream().toList();
     }

    @Override
    public ReadStatus update(UUID readStatusId, ReadStatusUpdateRequest readStatus) {
        return null;
    }

    @Override
    public void delete(UUID readStatusId) {
        if (!readStatusRepesitory.existsById(readStatusId)) {
            throw new NoSuchElementException("readStatus with id " + readStatusId + " does not exist");
        }
        readStatusRepesitory.deleteById(readStatusId);
    }

}
