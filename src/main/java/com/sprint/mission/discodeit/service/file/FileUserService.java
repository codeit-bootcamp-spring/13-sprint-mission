package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileUserService implements UserService {

    private final Path directory = Paths.get(System.getProperty("user.dir"));
    private final Path filePath = directory.resolve("users.ser");

    public  FileUserService() {
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
기본 요구사항
File IO를 통한 데이터 영속화
[ ]  다음의 조건을 만족하는 서비스 인터페이스의 구현체를 작성하세요.
[ ]  JCF 대신 FileIO와 객체 직렬화를 활용해 메소드를 구현하세요.
[ ]  Application에서 서비스 구현체를 File*Service로 바꾸어 테스트해보세요.
 */