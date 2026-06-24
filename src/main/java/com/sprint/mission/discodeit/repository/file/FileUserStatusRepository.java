package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.stereotype.Repository;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public class FileUserStatusRepository implements UserStatusRepository {

    private final Path DIRECTORY;
    private static final String EXTENSION = ".ser";
    private static final String USER_DIRECTORY = System.getProperty("user.dir"); // user.dir 또한 상수로 표현
    private static final String FILE_STORAGE_DIR = "file-data-map"; // file-data-map 또한 처음 보는 사람도 알아볼 수 있도록 역할과 의미를 분명히 해야 한다
    public FileUserStatusRepository(){
        this.DIRECTORY= Paths.get(USER_DIRECTORY,
                FILE_STORAGE_DIR, UserStatus.class.getSimpleName());
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
    public UserStatus save(UserStatus status) {
        Path path=resolvePath(status.getId());
        try(
                FileOutputStream fileOutputStream = new FileOutputStream(path.toFile());
                ObjectOutputStream objectOutputStream =new ObjectOutputStream(fileOutputStream)
        ) {
            objectOutputStream.writeObject(status); // 직렬화로 저장
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return status;
    }

    @Override
    public Optional<UserStatus> findById(UUID id) {
        UserStatus userStatusNullable =null;
        Path path=resolvePath(id); // id가 파일 경로 직접 순회
        if (Files.exists(path)){
            try (
                    FileInputStream fileInputStream=new FileInputStream(path.toFile());
                    ObjectInputStream objectInputStream=new ObjectInputStream(fileInputStream)
            ) {
                userStatusNullable =(UserStatus) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.ofNullable(userStatusNullable);
    }

    @Override
    public Optional<UserStatus> findByUserId(UUID userId) {
        try (Stream<Path> paths=Files.list(DIRECTORY)){
            return paths
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(path -> {
                        try (
                                FileInputStream fileInputStream=new FileInputStream(path.toFile());
                                ObjectInputStream objectInputStream=new ObjectInputStream(fileInputStream)
                        ){
                            return (UserStatus)objectInputStream.readObject();

                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }).filter(userstatus->userstatus.getUserId().equals(userId))
                    .findFirst();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UserStatus> findAll() {
        try (Stream<Path> paths=Files.list(DIRECTORY)){
            return paths
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(path -> {
                        try (
                                FileInputStream fileInputStream = new FileInputStream(path.toFile());
                                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)
                        ) {
                            return (UserStatus) objectInputStream.readObject();

                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existById(UUID id) {
        Path path=resolvePath(id);
        return Files.exists(path);
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
}
