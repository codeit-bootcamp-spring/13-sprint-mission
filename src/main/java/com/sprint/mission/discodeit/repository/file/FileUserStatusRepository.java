package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class FileUserStatusRepository implements UserStatusRepository {

    private final File userStatusFile;
    private final Map<UUID, UserStatus> userStatusRepo;
    private Map<UUID, UserStatus> loadUserStatusRepo()  {
        if (!userStatusFile.exists()) {
            return new HashMap<>();
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(userStatusFile))) {
            return (Map<UUID, UserStatus>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new HashMap<>();}
    }

    public FileUserStatusRepository() {
        this.userStatusFile = new File("data/repository-user-status.json");
        this.userStatusRepo = loadUserStatusRepo();
    }

    private void saveUserStatusRepo() {
        File parent = userStatusFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(userStatusFile))) {
            out.writeObject(userStatusRepo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(UserStatus userStatus) {
        userStatusRepo.put(userStatus.getId(), userStatus);
        saveUserStatusRepo();
    }

    @Override
    public UserStatus findById(UUID id) {
        return userStatusRepo.get(id);
    }

    @Override
    public UserStatus findByUserId(UUID userId) {
        for (UserStatus userStatus : userStatusRepo.values()) {
            if (userStatus.getUserId().equals(userId)) {
                return userStatus;
            }
        }
        return null;
    }

    @Override
    public void delete(UUID id) {
        userStatusRepo.remove(id);
        saveUserStatusRepo();
    }
}
