package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FileReadStatusRepository extends FileRepositoryRoot<ReadStatus> implements ReadStatusRepository {

    //ctor
    public FileReadStatusRepository() {
        super(Path.of("data/readStatuses.ser"));
    }

    //interface
    @Override
    public boolean existsReadStatusByUserIdAndChannelId(UUID userId, UUID channelId) {
        return storage.stream()
                .anyMatch(readStatus -> readStatus.getUserId().equals(userId) && readStatus.getChannelId().equals(channelId));
    }

    @Override
    public void createReadStatus(ReadStatus readStatus) {
        storage.add(readStatus);

        saveToBinary();
    }

    @Override
    public Optional<ReadStatus> findReadStatusById(UUID readStatusId) {
        return storage.stream()
                .filter(readStatus -> readStatus.getId().equals(readStatusId))
                .findFirst();
    }

    @Override
    public List<ReadStatus> findAllReadStatusByChannelId(UUID channelId) {
        return storage.stream()
                .filter(readStatus -> readStatus.getChannelId().equals(channelId))
                .toList();
    }

    @Override
    public List<ReadStatus> findAllReadStatusByUserId(UUID userId) {
        return storage.stream()
                .filter(readStatus -> readStatus.getUserId().equals(userId))
                .toList();
    }

    @Override
    public void save() {
        saveToBinary();
    }

    @Override
    public void deleteReadStatusByChannelId(UUID channelId) {
        storage.removeAll(
                storage.stream()
                        .filter(readStatus -> readStatus.getChannelId().equals(channelId))
                        .toList()
        );

        saveToBinary();
    }

    @Override
    public void deleteReadStatusById(UUID readStatusId) {
        storage.remove(
                storage.stream()
                        .filter(readStatus -> readStatus.getId().equals(readStatusId))
                        .findFirst()
                        .get()
        );

        saveToBinary();
    }
}
