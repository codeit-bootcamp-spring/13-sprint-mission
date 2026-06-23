package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.config.StorageProperties;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

//사용자 온라인 상태(userStatus)를 파일 시스템에 저장/조회/삭제
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
@Repository
public class FileUserStatusRepository implements UserStatusRepository {
    private final Path DIRECTORY; //userStatus 저장 디렉토리
    private final String EXTENSION; //파일 확장자

    public FileUserStatusRepository(StorageProperties properties) { //생성자. storageProperties 기반 설정 주입
        this.EXTENSION = properties.getExtension();
        //저장 경로
        this.DIRECTORY = Paths.get(System.getProperty("user.dir"), properties.getRootPath(), UserStatus.class.getSimpleName());
        if (Files.notExists(DIRECTORY)) { //디렉토리가 없으면 생성
            try {
                Files.createDirectories(DIRECTORY);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //UUID->파일 경로 변환
    private Path resolvePath(UUID id) {
        return DIRECTORY.resolve(id + EXTENSION);
    }

    @Override //UserStatus 저장
    public UserStatus save(UserStatus userStatus) {
        Path path = resolvePath(userStatus.getId());
        try (
                FileOutputStream fos = new FileOutputStream(path.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(userStatus);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return userStatus;
    }

    @Override //ID로 UserStatus 조회(역직력화)
    public Optional<UserStatus> findById(UUID id) {
        UserStatus userStatusNullable = null;
        Path path = resolvePath(id);
        if (Files.exists(path)) {
            try (
                    FileInputStream fis = new FileInputStream(path.toFile());
                    ObjectInputStream ois = new ObjectInputStream(fis)
            ) {
                userStatusNullable = (UserStatus) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.ofNullable(userStatusNullable);
    }

    @Override //userId 기준 상태 조회
    public Optional<UserStatus> findByUserId(UUID userId) {
        return findAll().stream()
                .filter(userStatus -> userStatus.getUserId().equals(userId))
                .findFirst();
    }

    @Override //전체 UserStatus 조회
    public List<UserStatus> findAll() {
        try (Stream<Path> paths = Files.list(DIRECTORY)) {
            return paths
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis)
                        ) {
                            return (UserStatus) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override //존재 여부 확인(파일 존재 여부)
    public boolean existsById(UUID id) {
        Path path = resolvePath(id);
        return Files.exists(path);
    }

    @Override //삭제 (파일 삭제)
    public void deleteById(UUID id) {
        Path path = resolvePath(id);
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override //userId 기준 삭제
    public void deleteByUserId(UUID userId) {
        this.findByUserId(userId)
                .ifPresent(userStatus -> this.deleteById(userStatus.getId()));
    }
}
