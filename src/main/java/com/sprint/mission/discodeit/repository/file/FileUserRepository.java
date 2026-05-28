package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileUserRepository implements UserRepository {

    private final Path directory = Paths.get(System.getProperty("user.dir"));
    private final Path filePath = directory.resolve("users.ser");

    public  FileUserRepository() {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFile(List<User> foundUser) {
        try (ObjectOutputStream oos = new ObjectOutputStream
                (new FileOutputStream(filePath.toFile()))) {
            oos.writeObject(foundUser);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<User> readFile() {
        if (Files.notExists(filePath)) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream
                (new FileInputStream(filePath.toFile()))) {
            return (List<User>) ois.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User create(User user) {
        List<User> foundUser = readFile();
        foundUser.add(user);
        saveFile(foundUser);
        return user;
    }

    @Override
    public User findById(UUID id) {
        List<User> foundUser = readFile();
        for (User user : foundUser) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        return readFile();
    }

    @Override
    public void update(User inputUser) {
        List<User> foundUser = readFile();
        for (User user : foundUser) {
            if (user.getId().equals(inputUser.getId())) {
                user.updateName(inputUser);
                break;
            }
        }
    }

    @Override
    public void delete(UUID id) {
        List<User> foundUser = readFile();
        foundUser.removeIf(u->u.getId().equals(id));
        saveFile(foundUser);
    }

}
/*
레포지토리 설계 및 구현
[ ] 다음의 조건을 만족하는 레포지토리 인터페이스의 구현체를 작성하세요.
[ ] 기존에 구현한 File*Service 구현체의 "저장 로직"과 관련된 코드를 참고하여 구현하세요.
 */