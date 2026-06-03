package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.util.*;

public class FileUser implements UserService {

    private final String FILE_PATH = "users.dat";
    private Map<UUID, User> data;

    public FileUser() {

        load();
    }

    private void load() {

        File file = new File(FILE_PATH);
        if (!file.exists()) {

            data = new HashMap<>();
            return;
        }

        try (
                ObjectInputStream ois =
                        new ObjectInputStream(
                                new FileInputStream(file)
                        )
        ) {
            data = (Map<UUID, User>) ois.readObject();

        } catch (Exception e) {

            data = new HashMap<>();
        }
    }

    private void save() {

        try (
                ObjectOutputStream oos =
                        new ObjectOutputStream(
                                new FileOutputStream(FILE_PATH)
                        )
        ) {
            oos.writeObject(data);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @Override
    public void create(User user) {

        data.put(user.getId(), user);
        save();
    }

    @Override
    public User read(UUID id) {

        return data.get(id);
    }

    @Override
    public List<User> readAll() {

        return new ArrayList<>(data.values());
    }

    @Override
    public void delete(UUID id) {

        data.remove(id);
        save();
    }
}