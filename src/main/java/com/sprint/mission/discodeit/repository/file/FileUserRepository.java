package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class FileUserRepository implements UserRepository {
    private final String filePath = "users.dat";
    private List<User> users = new ArrayList<>();

    public FileUserRepository() {
        load();
    }

    //@Override
    public User save(User user) {
        users.add(user);
        saveToFile();
        return user;
    }

    //@Override
    public User findByld(UUID id){
        for (User user : users) {
            if (user.getId().equals(id))
                return user;
        }
        return null;
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("uncheked")
    private void load() {
        File file = new File(filePath);
        if (!file.exists()) {
            users = new ArrayList<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            users = (List<User>) ois.readObject();
        } catch (Exception e) {
            users = new ArrayList<>();
        }
    }

}
