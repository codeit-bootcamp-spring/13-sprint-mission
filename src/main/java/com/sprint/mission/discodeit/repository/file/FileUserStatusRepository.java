package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FileUserStatusRepository implements UserStatusRepository {

    private final String filePath = "user_status.dat";

    @SuppressWarnings("unchecked")
    private List<UserStatus> loadAll() {
        File file = new File(filePath);
        if (!file.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<UserStatus>) ois.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void saveAll(List<UserStatus> list) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void create(UserStatus userStatus) {
        List<UserStatus> list = loadAll();
        list.add(userStatus);
        saveAll(list);
    }

    @Override
    public Optional<UserStatus> findById(UUID id) {
        return loadAll().stream().filter(us -> us.getId().equals(id)).findFirst();
    }

    @Override
    public List<UserStatus> findAll() {
        return loadAll();
    }

    @Override
    public void update(UserStatus userStatus) {
        List<UserStatus> list = loadAll();
        list.removeIf(us -> us.getId().equals(userStatus.getId()));
        list.add(userStatus);
        saveAll(list);
    }

    @Override
    public void delete(UUID id) {
        List<UserStatus> list = loadAll();
        list.removeIf(us -> us.getId().equals(id));
        saveAll(list);
    }
}
