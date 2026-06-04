package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class FileUserRepository implements UserRepository {

    private final Path filePath = Paths.get(System.getProperty("user.dir"), "users.ser");

    @SuppressWarnings("unchecked")
    private List<User> readFile() {
        if(!Files.exists(filePath)) {
            return new ArrayList<>();
        }
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
        ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (List<User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new  ArrayList<>();
        }
    }

    private void saveFile(List<User> users) {
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile());
        ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(users);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User create(User user) {
        List<User> users = readFile();
        users.add(user);
        saveFile(users);
        return user;
    }

    @Override
    public User findById(UUID id) {
        return readFile().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<User> findAll() {
        return readFile();
    }

    @Override
    public void update(User user) {
        List<User> users = readFile();
        users.replaceAll(u -> u.getId()
                .equals(user.getId())
                ? user : u);
        saveFile(users);
    }

    @Override
    public void delete(UUID id) {
        List<User> users = readFile();
        users.removeIf(u -> u.getId().equals(id));
        saveFile(users);
    }

}
/*
레포지토리 설계 및 구현
[ ] 다음의 조건을 만족하는 레포지토리 인터페이스의 구현체를 작성하세요.
[ ] 기존에 구현한 File*Service 구현체의 "저장 로직"과 관련된 코드를 참고하여 구현하세요.
 */