package com.sprint.mission.discodeit.service.file;


import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileUserService implements UserService {

    private final Path filePath;

    public FileUserService(Path filePath) {
        this.filePath = filePath;
        if (!Files.exists(filePath.getParent())){
            try {
                Files.createDirectories(filePath.getParent());
            } catch (IOException e) {
                throw new RuntimeException("디렉토리 생성 실패");
            }
        }
    }

    // 파일에 data 저장하기
    private void saveToFile(Map<UUID, User>data){
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패!");
        }
    }

    //파일에서 data 불러오기
    private Map<UUID, User> loadFromFile(){
        if (!Files.exists(filePath)){
            return new HashMap<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toFile()))){
            return (Map<UUID, User>) ois.readObject();
        }catch (IOException | ClassNotFoundException e){
            throw new RuntimeException("파일 불러오기 실패");
        }
    }

    //user 생성
    @Override
    public User createUser(String name, String email, String password) {
        Map<UUID, User>data = loadFromFile();
        boolean duplicateCheck = data.values().stream()
                .anyMatch(user -> user.getEmail().equals(email));
        if (name == null || name.isBlank()){
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }
        if (email == null || email.isBlank()){
            throw new IllegalArgumentException("이메일을 입력해주세요");
        }
        if (password == null || password.isBlank()){
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }
        if (duplicateCheck){
            throw new IllegalArgumentException("이미 생성된 이메일 입니다.");
        }
        User user = new User(name, email, password);
        data.put(user.getId(), user);
        saveToFile(data);
        return user;
    }
    //user 단건 조회
    @Override
    public User findByUser(UUID userId) {
        Map<UUID, User>data = loadFromFile();
        User user = data.get(userId);
        if (user == null){
            throw new NoSuchElementException("존재하지 않는 유저입니다.");
        }
        return user;
    }

    //User 전체 조회
    @Override
    public List<User> findAllUser() {
        Map<UUID, User>data = loadFromFile();
        return new ArrayList<>(data.values());
    }

    @Override
    public User updateUser(UUID userId, String name, String email, String password) {
        Map<UUID, User>data = loadFromFile();
        User user = data.get(userId);
        if (user == null){
            throw new NoSuchElementException("존재하지 않는 유저 입니다.");
        }
        if (name != null){
            user.updateUserName(name);
        }
        if (email != null){
            user.updateUserEmail(email);
        }
        if (password != null){
            user.updateUserPassword(password);
        }

        saveToFile(data);
        return user;
    }

    @Override
    public void deleteUser(UUID userId) {
        Map<UUID, User>data = loadFromFile();
        User user = data.get(userId);
        if (user == null){
            throw new NoSuchElementException("존재 하지 않는 유저 입니다.");
        }
        data.remove(userId);
        saveToFile(data);
    }
}
