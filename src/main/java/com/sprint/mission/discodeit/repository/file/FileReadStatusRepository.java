package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.config.StorageProperties;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
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

//ReadStatus File 기반 Repository 구현체
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
@Repository
public class FileReadStatusRepository implements ReadStatusRepository {
    private final Path DIRECTORY; //ReadStatus 파일 저장 디렉토리
    private final String EXTENSION; //파일 확장자

    public FileReadStatusRepository(StorageProperties properties) { //생성자. storageProperties 기반 설정값 주입
        this.EXTENSION = properties.getExtension();
        //저장경로
        this.DIRECTORY = Paths.get(System.getProperty("user.dir"), properties.getRootPath(), ReadStatus.class.getSimpleName());
        if (Files.notExists(DIRECTORY)) { //디렉토리 없으면 생성
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

    @Override //ReadStatus 저장
    public ReadStatus save(ReadStatus readStatus) {
        Path path = resolvePath(readStatus.getId());
        try (
                FileOutputStream fos = new FileOutputStream(path.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(readStatus);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return readStatus;
    }

    @Override //ID로 ReadStatus 조회(역질렬화)
    public Optional<ReadStatus> findById(UUID id) {
        ReadStatus readStatusNullable = null;
        Path path = resolvePath(id);
        if (Files.exists(path)) {
            try (
                    FileInputStream fis = new FileInputStream(path.toFile());
                    ObjectInputStream ois = new ObjectInputStream(fis)
            ) {
                readStatusNullable = (ReadStatus) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.ofNullable(readStatusNullable);
    }

    @Override //특정 사용자 기준 ReadStatus 전체 조회
    public List<ReadStatus> findAllByUserId(UUID userId) {
        try (Stream<Path> paths = Files.list(DIRECTORY)) {
            return paths
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis)
                        ) {
                            return (ReadStatus) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(readStatus -> readStatus.getUserId().equals(userId))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override //특정 채널 기준 ReadStatus 조회
    public List<ReadStatus> findAllByChannelId(UUID channelId) {
        try {
            return Files.list(DIRECTORY)
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis)
                        ) {
                            return (ReadStatus) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(readStatus -> readStatus.getChannelId().equals(channelId))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override //ReadStatus 존재 여부 확인
    public boolean existsById(UUID id) {
        Path path = resolvePath(id);
        return Files.exists(path);
    }

    @Override //ReadStatus 삭제
    public void deleteById(UUID id) {
        Path path = resolvePath(id);
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override //특정 채넝의 ReadStatus 전체 삭제
    public void deleteAllByChannelId(UUID channelId) {
        this.findAllByChannelId(channelId)
                .forEach(readStatus -> this.deleteById(readStatus.getId()));
    }
}
