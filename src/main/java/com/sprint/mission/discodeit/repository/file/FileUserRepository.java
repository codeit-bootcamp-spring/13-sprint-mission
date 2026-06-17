package com.sprint.mission.discodeit.repository.file;


import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository // File*Repository 구현체를 Repository 인터페이스의 Bean으로 등록
public class FileUserRepository implements UserRepository {

    private final Path DIRECTORY;
    private final String EXTENSION = ".ser";

    public FileUserRepository(){
        this.DIRECTORY= Paths.get(System.getProperty("user.dir"),
                "file-data-map", User.class.getSimpleName());
        if (Files.notExists(DIRECTORY)){ // Repository 생성 시점에서 폴더 존재 여부 한 번만 검사
            try {
                Files.createDirectories(DIRECTORY);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Path resolvePath(UUID id) {
        return DIRECTORY.resolve(id+EXTENSION);
    }

    @Override
    public User save(User user) {
        Path path=resolvePath(user.getId());
        try(
                FileOutputStream fileOutputStream = new FileOutputStream(path.toFile());
                ObjectOutputStream objectOutputStream =new ObjectOutputStream(fileOutputStream)
        ) {
            objectOutputStream.writeObject(user); // 직렬화로 저장
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        User userNullable=null;
        Path path=resolvePath(id); // id가 파일 경로 직접 순회
        if (Files.exists(path)){
            try (
                    FileInputStream fileInputStream=new FileInputStream(path.toFile());
                    ObjectInputStream objectInputStream=new ObjectInputStream(fileInputStream)
            ) {
                userNullable=(User) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.ofNullable(userNullable);
    }

    @Override
    public List<User> findAll() {
        try {
            return Files.list(DIRECTORY)
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(path -> {
                        try (
                                FileInputStream fileInputStream = new FileInputStream(path.toFile());
                                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)
                        ) {
                            return (User) objectInputStream.readObject();

                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void deleteById(UUID id) {
        Path path=resolvePath(id);
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existById(UUID id) {
        Path path=resolvePath(id);
        return Files.exists(path);
    }
}
