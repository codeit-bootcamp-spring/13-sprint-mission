package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(
        value = "discodeit.repository.type",
        havingValue = "file"
)
public class FileReadStatusRepository extends FileRepositoryRoot<ReadStatus> implements ReadStatusRepository {

    //ctor
    public FileReadStatusRepository(@Value("${discodeit.repository.file-directory}") String fileDirectory) {
        super(Path.of(fileDirectory).resolve("readStatuses.ser"));
    }

    //interface
    @Override
    public boolean existsReadStatusByUserIdAndChannelId(UUID userId, UUID channelId) {
        return storage.stream()
                .anyMatch(readStatus -> readStatus.getUser().getId().equals(userId) && readStatus.getChannel().getId().equals(channelId));
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
                .filter(readStatus -> readStatus.getChannel().getId().equals(channelId))
                .toList();
    }

    @Override
    public List<ReadStatus> findAllReadStatusByUserId(UUID userId) {
        return storage.stream()
                .filter(readStatus -> readStatus.getUser().getId().equals(userId))
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
                        .filter(readStatus -> readStatus.getChannel().getId().equals(channelId))
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
