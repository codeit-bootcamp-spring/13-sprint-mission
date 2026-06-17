package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.stereotype.*;

import java.util.*;
@Repository
@ConditionalOnProperty(name = "discodeit.repository.type",  matchIfMissing = true, havingValue = "jcf")
public class JCFReadStatusRepository implements ReadStatusRepository {

    private final Map<UUID, ReadStatus> data;

    public JCFReadStatusRepository() {
        this.data = new HashMap<>();
    }

    @Override
    public void create(ReadStatus readStatus) {
        data.put(readStatus.getId(), readStatus);
    }

    @Override
    public List<ReadStatus> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public ReadStatus find(UUID id) {
        return data.get(id);
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }

    @Override
    public List<ReadStatus> findByChannelId(UUID channelId) {
        return data.values().stream()
                .filter(readStatus -> Objects.equals(readStatus.getChannelId(), channelId))
                .toList();
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        return data.values().stream()
                .filter(readStatus -> Objects.equals(readStatus.getUserId(), userId))
                .toList();
    }

    @Override
    public void update(ReadStatus readStatus) {
        data.put(readStatus.getId(), readStatus);
    }

    @Override
    public ReadStatus findByUserIdAndChannelId(UUID userId, UUID channelId) {
        return data.values().stream()
                .filter(readStatus ->
                        Objects.equals(readStatus.getUserId(), userId)
                                && Objects.equals(readStatus.getChannelId(), channelId)
                )
                .findFirst()
                .orElse(null);
    }
}
