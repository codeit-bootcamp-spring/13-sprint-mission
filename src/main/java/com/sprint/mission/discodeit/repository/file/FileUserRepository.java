package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileUserRepository implements UserRepository {

    //users 저장 객체 및 저장 경로
    private final List<User> users = new ArrayList<>();
    private final Path binPath = Path.of("data/users.ser");

    //ctor
    public FileUserRepository() {
        File file = new File("data/users.ser");
        // 파일 저장 위치에 파일이 존재한다면 로드하도록.
        if (file.exists())
            loadFromBinary();
    }

    //method
    //직렬화 메서드
    private void saveToBinary() {
        Path parent = binPath.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new RuntimeException("폴더 생성에 실패했습니다.");
            }
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(binPath)))) {
            oos.writeObject(new ArrayList<>(users));
        } catch (IOException e) {
            throw new RuntimeException("직렬화에 실패했습니다.");
        }
    }

    //역직렬화 메서드
    private void loadFromBinary() {
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(binPath)))) {
            List<User> usersTemp = (List<User>) ois.readObject();
            // 일단 users 초기화
            if (users != null)
                users.clear();
            // 유저에 역직렬화한 List<User> 넣기
            users.addAll(usersTemp);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("역직렬화에 실패했습니다.");
        } catch (IOException e) {
            throw new RuntimeException("역직렬화에 실패했습니다22.");
        }
    }

    
    //interface
    @Override
    public void saveUser(User user) {

    }

    @Override
    public void findUser(User user) {

    }

    @Override
    public void findAll() {

    }

    @Override
    public void updateUser() {

    }

    @Override
    public void deleteUser() {

    }
}
