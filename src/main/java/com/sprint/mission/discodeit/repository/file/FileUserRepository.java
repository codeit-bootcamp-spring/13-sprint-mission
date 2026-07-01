package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
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
public class FileUserRepository implements UserRepository {

    private final Path path;

    public FileUserRepository(@Value("${file.path.user}") String path) {
        this.path = Paths.get(path);

        try {
            Files.createDirectories(this.path.getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveFile(List<User> users) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
            oos.writeObject(new ArrayList<>(users));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<User> loadFile() {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            return (List<User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(User user) {
        List<User> users = loadFile();

        boolean isUpdated = false;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(user.getId())) {
                users.set(i, user);
                isUpdated = true;
                break;
            }
        }

        if(!isUpdated) {
            users.add(user);
        }
        saveFile(users);
    }

    @Override
    public User findById(UUID id) {
        List<User> users = loadFile();

        for (User user : users) {
            if(user.getId().equals(id)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public User findByUserName(String userName) {
        List<User> users = loadFile();
        for (User user : users) {
            if(userName.equals(user.getUserName())) {
                return user;
            }
        }
        return null;
    }

    @Override
    public User findByEmail(String email) {
        List<User> users = loadFile();

        for (User user : users) {
            if(user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        return loadFile();
    }

    @Override
    public void delete(UUID id) {
        List<User> users = loadFile();

        if(users.removeIf(user -> user.getId().equals(id))) {
            saveFile(users);
        }
    }
}
