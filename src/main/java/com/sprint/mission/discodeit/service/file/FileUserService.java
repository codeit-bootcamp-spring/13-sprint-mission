package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.util.*;

public class  FileUserService implements UserService {

    private final File file;
    private final Map<UUID, User>data;
    private Map<UUID, User> loadData()  {
        if (!file.exists()) {
            return new HashMap<UUID, User>();
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, User>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new HashMap<UUID, User>();
        }


    }
    private void saveData() {
        File parent = file.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(data);}
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public FileUserService() {
        this.file = new File("data/users.ser");
        this.data = loadData();
    }


    @Override
    public void create(User user) {
        data.put(user.getId(), user);
        saveData();
    }

    @Override
    public User findById(UUID id) {
        return data.get(id);
    }

    @Override
    public Collection<User> findAll() {
        return data.values();
    }

    @Override
    public void update(UUID id, String name, String email) {
        User user = data.get(id);
        if ( user != null) {
            user.renew(name, email);
        }
        saveData();
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
        saveData();
    }
}



