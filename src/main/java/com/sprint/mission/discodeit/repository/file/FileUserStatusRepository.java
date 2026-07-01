package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
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
public class FileUserStatusRepository implements UserStatusRepository {

    private final Path path;

    public FileUserStatusRepository(@Value("${file.path.userStatus}") String path) {
        this.path = Paths.get(path);

        try {
            Files.createDirectories(this.path.getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveFile(List<UserStatus> userStatuses) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
            oos.writeObject(new ArrayList<>(userStatuses));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<UserStatus> loadFile() {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            return (List<UserStatus>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(UserStatus userStatus) {
        List<UserStatus> userStatuses = loadFile();

        boolean isUpdated = false;
        for (int i = 0; i < userStatuses.size(); i++) {
            if (userStatuses.get(i).getId().equals(userStatus.getId())) {
                userStatuses.set(i, userStatus);
                isUpdated = true;
                break;
            }
        }

        if(!isUpdated) {
            userStatuses.add(userStatus);
        }
        saveFile(userStatuses);
    }

    @Override
    public UserStatus findById(UUID id) {
        List<UserStatus> userStatuses = loadFile();

        for (UserStatus userStatus : userStatuses) {
            if(userStatus.getId().equals(id)) {
                return userStatus;
            }
        }
        return null;
    }

    @Override
    public List<UserStatus> findAll() {
        return loadFile();
    }

    @Override
    public void delete(UUID id) {
        List<UserStatus> userStatuses = loadFile();

        if(userStatuses.removeIf(userStatus -> userStatus.getId().equals(id))) {
            saveFile(userStatuses);
        }
    }
}
