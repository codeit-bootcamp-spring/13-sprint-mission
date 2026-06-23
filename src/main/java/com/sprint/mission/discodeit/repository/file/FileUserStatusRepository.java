package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FileUserStatusRepository implements UserStatusRepository {

    private static final String DATA_DIRECTORY = "data";
    private static final String USERSTATUS_DIRECTORY = "user_status";

    private final Path directory;

    public FileUserStatusRepository() {
        this.directory =
                Paths.get(System.getProperty("user.dir"),
                        DATA_DIRECTORY,
                        USERSTATUS_DIRECTORY);
        init(directory);
    }

    private void init(Path directory) {
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void save(UserStatus userStatus) {
        Path filePath = directory.resolve(userStatus.getId() + ".ser");

        try (FileOutputStream fos = new FileOutputStream(filePath.toFile());
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(userStatus);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UserStatus> findAll() {
        if (!Files.exists(directory)) {
            return List.of();
        }

        try {
            return Files.list(directory).map(path -> {
                try (FileInputStream fis = new FileInputStream(path.toFile());
                     ObjectInputStream ois = new ObjectInputStream(fis))
                {
                    return (UserStatus) ois.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }).toList();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(UUID id) {
        Path filePath = directory.resolve(id + ".ser");
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<UserStatus> findById(UUID id) {
        Path filePath = directory.resolve(id + ".ser");

        if (!Files.exists(filePath)) {
            return Optional.empty();
        }

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             ObjectInputStream ois = new ObjectInputStream(fis))
        {
            return Optional.of((UserStatus) ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<UserStatus> findByUserId(UUID userId) {
        return findAll().stream()
                .filter(status -> status.getUserId().equals(userId))
                .findFirst();
    }
}
