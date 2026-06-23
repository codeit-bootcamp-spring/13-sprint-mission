package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JCFReadStatusRepository implements ReadStatusRepository {

    private final List<ReadStatus> data = new ArrayList<>();

    @Override
    public void save(ReadStatus readStatus) {
        data.removeIf(
                status -> status.getId().equals(readStatus.getId())
        );
        data.add(readStatus);
    }

    @Override
    public Optional<ReadStatus> findById(UUID id) {
        return data.stream()
                .filter(status -> status.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<ReadStatus> findAll() {
        return new ArrayList<>(data);
    }

    @Override
    public void delete(UUID id) {
        data.removeIf(status -> status.getId().equals(id));
    }

    @Override
    public List<ReadStatus> findAllByChannelId(UUID channelId) {
        return data.stream()
                .filter(status -> status.getChannelId().equals(channelId))
                .toList();
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        return data.stream()
                .filter(status -> status.getUserId().equals(userId))
                .toList();
    }

    @Override
    public Optional<ReadStatus> findByUserIdAndChannelId(UUID userId, UUID channelId) {
        return data.stream()
                .filter(status -> status.getUserId().equals(userId)
                        && status.getChannelId().equals(channelId))
                .findFirst();
    }
}
