package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.dto.request.UserRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FileUserService implements UserService {

    // 주소 설정, 변수를 대문자로
    private final Path DIRECTORY = Paths.get(System.getProperty("user.dir"));
    private final Path filePath = DIRECTORY.resolve("users.ser");

    public  FileUserService() {
        try {
            Files.createDirectories(DIRECTORY);
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
    public UserResponse create(UserRequest dto) {
        List<User> foundUser = readFile();
        User newUser = new User(dto.username(), dto.email(), dto.password());
        foundUser.add(foundUser.get(0));
        saveFile(foundUser);
        return new UserResponse(newUser.getId(), newUser.getUsername(), newUser.getEmail(), true);
    }

    @Override
    public Optional<UserResponse> findById(UUID id) {
        List<User> foundUser = readFile();
        for (User user : foundUser) {
            if (user.getId().equals(id)) {
                UserResponse response = new UserResponse(user.getId(), user.getUsername(), user.getEmail(), true);
                return Optional.of(response);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<UserResponse> findAll() {
        List<User> foundUser = readFile();
        List<UserResponse> responseList = new ArrayList<>();
        for (User user : foundUser) {
            responseList.add(new UserResponse(user.getId(), user.getUsername(), user.getEmail(), true));
        }
        return responseList;
    }

    @Override
    public UserResponse update(UUID id, UserRequest dto) {
        List<User> foundUser = readFile();
        User updatedUser = null;
        for (User user : foundUser) {
            if (user.getId().equals(id)) {
                user.update(dto.username(), dto.password(), dto.email());
                if (!dto.profileImageName().equals(updatedUser.getProfileImageId())) {}
                user.updateProfileId(UUID.randomUUID());
                break;
            }
        }
        return null;
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