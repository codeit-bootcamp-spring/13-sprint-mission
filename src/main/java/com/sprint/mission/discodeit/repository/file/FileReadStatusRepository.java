package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@ConditionalOnProperty(
        prefix = "discodeit.repository",
        name = "type",
        havingValue = "file"
)
public class FileReadStatusRepository implements ReadStatusRepository {

    private final Path path;

    public FileReadStatusRepository(@Value("${file.path.readStatus}") String path) {
        this.path = Paths.get(path);

        try {
            Files.createDirectories(this.path.getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveFile(List<ReadStatus> readStatuses) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
            oos.writeObject(new ArrayList<>(readStatuses));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ReadStatus> loadFile() {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            return (List<ReadStatus>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(ReadStatus readStatus) {
        List<ReadStatus> readStatuses = loadFile();

        boolean isUpdated = false;
        for (int i = 0; i < readStatuses.size(); i++) {
            if (readStatuses.get(i).getId().equals(readStatus.getId())) {
                readStatuses.set(i, readStatus);
                isUpdated = true;
                break;
            }
        }

        if(!isUpdated) {
            readStatuses.add(readStatus);
        }
        saveFile(readStatuses);
    }

    @Override
    public ReadStatus findById(UUID id) {
        List<ReadStatus> readStatuses = loadFile();

        for (ReadStatus readStatus : readStatuses) {
            if(readStatus.getId().equals(id)) {
                return readStatus;
            }
        }
        return null;
    }

    @Override
    public List<ReadStatus> findAll() {
        return loadFile();
    }

    @Override
    public void delete(UUID id) {
        List<ReadStatus> readStatuses = loadFile();

        if(readStatuses.removeIf(readStatus -> readStatus.getId().equals(id))) {
            saveFile(readStatuses);
        }
    }
}
