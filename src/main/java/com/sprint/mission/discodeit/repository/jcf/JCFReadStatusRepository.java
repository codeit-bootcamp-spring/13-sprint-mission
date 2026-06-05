package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@NoArgsConstructor
@ConditionalOnProperty(
        value = "discodeit.repository.type",
        havingValue = "jcf",
        matchIfMissing = true
)
public class JCFReadStatusRepository implements ReadStatusRepository {

    //필드
    private final List<ReadStatus> readStatuses = new ArrayList<>();

    //interface
    @Override
    public boolean existsReadStatusByUserIdAndChannelId(UUID userId, UUID channelId) {
        return readStatuses.stream()
                .anyMatch(readStatus -> readStatus.getUserId().equals(userId) && readStatus.getChannelId().equals(channelId));
    }

    @Override
    public void createReadStatus(ReadStatus readStatus) {
        readStatuses.add(readStatus);
    }

    @Override
    public Optional<ReadStatus> findReadStatusById(UUID readStatusId) {
        return readStatuses.stream()
                .filter(readStatus -> readStatus.getId().equals(readStatusId))
                .findFirst();
    }

    @Override
    public List<ReadStatus> findAllReadStatusByChannelId(UUID channelId) {
        return readStatuses.stream()
                .filter(readStatus -> readStatus.getChannelId().equals(channelId))
                .toList();
    }

    @Override
    public List<ReadStatus> findAllReadStatusByUserId(UUID userId) {
        return readStatuses.stream()
                .filter(readStatus -> readStatus.getUserId().equals(userId))
                .toList();
    }

    @Override
    public void save() {

    }

    @Override
    public void deleteReadStatusByChannelId(UUID channelId) {
        readStatuses.removeAll(
                readStatuses.stream()
                        .filter(readStatus -> readStatus.getChannelId().equals(channelId))
                        .toList()
        );
    }

    @Override
    public void deleteReadStatusById(UUID readStatusId) {
        readStatuses.remove(
                readStatuses.stream()
                        .filter(readStatus -> readStatus.getId().equals(readStatusId))
                        .findFirst()
                        .get()
        );
    }
}
