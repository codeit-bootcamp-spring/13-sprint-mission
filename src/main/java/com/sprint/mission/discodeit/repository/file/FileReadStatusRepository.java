package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
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
public class FileReadStatusRepository implements ReadStatusRepository {
    private final Path DIRECTORY;
    private final String EXTENSION = ".ser";

    public FileReadStatusRepository(){
        this.DIRECTORY= Paths.get(System.getProperty("user.dir"),
                "file-data-map", ReadStatus.class.getSimpleName());
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
    public ReadStatus save(ReadStatus status) {
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
    public Optional<ReadStatus> findById(UUID id) {
        ReadStatus readStatusNullable =null;
        Path path=resolvePath(id); // id가 파일 경로 직접 순회
        if (Files.exists(path)){
            try (
                    FileInputStream fileInputStream=new FileInputStream(path.toFile());
                    ObjectInputStream objectInputStream=new ObjectInputStream(fileInputStream)
            ) {
                readStatusNullable =(ReadStatus) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.ofNullable(readStatusNullable);
    }

    @Override
    public List<ReadStatus> findAllByChannelId(UUID channelId) {
        try (Stream<Path> paths=Files.list(DIRECTORY)){
            return paths
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(path -> {
                        try (
                                FileInputStream fileInputStream = new FileInputStream(path.toFile());
                                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)
                        ) {
                            return (ReadStatus) objectInputStream.readObject();

                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }).filter(readStatus -> readStatus.getChannelId().equals(channelId))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        try (Stream<Path> paths=Files.list(DIRECTORY)){
            return paths
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(path -> {
                        try (
                                FileInputStream fileInputStream = new FileInputStream(path.toFile());
                                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)
                        ) {
                            return (ReadStatus) objectInputStream.readObject();

                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }).filter(readStatus -> readStatus.getUserId().equals(userId))
                    .toList();
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
