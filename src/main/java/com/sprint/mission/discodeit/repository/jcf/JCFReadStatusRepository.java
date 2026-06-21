package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import lombok.NoArgsConstructor;

import java.util.*;

@NoArgsConstructor
public class JCFReadStatusRepository implements ReadStatusRepository {

    private static final Map<UUID, ReadStatus> data = new HashMap<>(); // 인스턴스를 생성할 때 마다 새로운 Map을 만들지 않도록 한다

    @Override
    public ReadStatus save(ReadStatus status) {
        data.put(status.getId(), status);
        return status;
    }

    @Override
    public Optional<ReadStatus> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<ReadStatus> findAllByChannelId(UUID channelId) {
        return data.values().stream()
                .filter(readStatus -> readStatus.getChannelId().equals(channelId))
                .toList();
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        return data.values().stream()
                .filter(readStatus -> readStatus.getUserId().equals(userId))
                .toList();
    }

    @Override
    public boolean existById(UUID id) {
        return data.containsKey(id);
    }

    @Override
    public void deleteById(UUID id) {
        data.remove(id);
    }

}
