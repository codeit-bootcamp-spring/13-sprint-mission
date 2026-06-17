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

    // users 디렉토리 경로
    private final Path directory;

    public FileUserService() {
        this.directory = Paths.get(
                System.getProperty("user.dir"),
                "data",
                "users"
        );
        init(directory);
    }

    public static void init(Path directory) {
        // 저장할 경로의 파일 초기화
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // 매개변수 변경으로 인한 오류로 빈 메서드 생성
    @Override
    public User create(String name, String email, String password) {
        return null;
    }

    //    @Override
//    public void createUser(User user) {
//        Path filePath =
//                directory.resolve(user.getId() + ".ser");
//
//        save(filePath, user);
//    }

    // 단일 조회
    @Override
    public User find(UUID id) {
        // 저장 로직
        Path filePath = directory.resolve(id + ".ser");

        if (!Files.exists(filePath)) {
            return null;
        }
        try (   // 저장 로직
                FileInputStream fis = new FileInputStream(filePath.toFile());
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            return (User) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // 유저 전체 조회
    @Override
    public List<User> findAll() {
        if (Files.exists(directory)) {
            try {
                // 저장 로직
                List<User> list = Files.list(directory)
                        .map(path -> {
                            try (   // 저장 로직
                                    FileInputStream fis = new FileInputStream(path.toFile());
                                    ObjectInputStream ois = new ObjectInputStream(fis))
                            {
                                Object data = ois.readObject();
                                return (User) data;
                            } catch (IOException | ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }).toList();
                return list;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new ArrayList<>();
        }
    }

    // 수정 후 조회
    @Override
    public void update(UUID id, String name, String email, String password) {
        User user = find(id);
        // 비즈니스 로직
        if (user == null) {
            return;
        }

        // 비즈니스 로직
        user.update(name, email, password);

        // 저장 로직
        Path filePath = directory.resolve(id + ".ser");
        save(filePath, user);
    }


    @Override
    public void delete(UUID id) {
        Path filePath = directory.resolve(id + ".ser");

        try {
            // 저장 로직
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 객체 저장
    private <T> void save(Path filePath, T data) {
        try(
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

/**
 * JCF*Service와 File*Service 비교
 *
 * 공통점
 * 1. UserService 인터페이스를 구현
 * 2. 사용자 생성, 조회, 수정, 삭제 기능
 * 3. 사용자 수정 시 user.update() 메서드
 *
 * 차이점
 * JCF*Service : 메모리에 저장 -> 프로그램 종료 후 데이터 사라짐
 * File*Service : 파일에 저장 -> 프로그램 종료 후에도 데이터 유지
 */
