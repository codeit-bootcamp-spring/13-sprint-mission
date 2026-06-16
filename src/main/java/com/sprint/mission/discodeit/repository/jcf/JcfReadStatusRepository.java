package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JcfReadStatusRepository implements ReadStatusRepository {

    private final List<ReadStatus> database = new ArrayList<>();

    @Override
    public void create(ReadStatus readStatus) {
        database.add(readStatus);
    }

    @Override
    public Optional<ReadStatus> findById(UUID id) {
        return database.stream()
                .filter(rs -> rs.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<ReadStatus> findAll() {
        return new ArrayList<>(database);
    }

    @Override
    public void update(ReadStatus readStatus) {
        findById(readStatus.getId()).ifPresent(rs -> {
            database.remove(rs);
            database.add(readStatus);
        });
    }

    @Override
    public void delete(UUID id) {
        database.removeIf(rs -> rs.getId().equals(id));
    }

    @Override
    public void deleteByChannelId(UUID channelId) {
        database.removeIf(rs -> rs.getChannelId().equals(channelId));
    }
}
