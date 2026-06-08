package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;

import java.util.*;

public class JCFReadStatusRepository implements ReadStatusRepository {

    private final Map<UUID, ReadStatus> database = new HashMap<>();

    @Override
    public ReadStatus save(ReadStatus readStatus) {
        database.put(readStatus.getId(), readStatus);
        return readStatus;
    }

    @Override
    public Optional<ReadStatus> findById(UUID id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<ReadStatus> findAll() {
        return new ArrayList<>(database.values());
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        return database.values().stream()
                .filter(rs -> rs.getUserId() != null && rs.getUserId().equals(userId))
                .toList();
    }

    @Override
    public ReadStatus update(ReadStatus readStatus) {
        database.put(readStatus.getId(), readStatus);
        return readStatus;
    }

    @Override
    public void delete(UUID id) {
        database.remove(id);
    }
}
