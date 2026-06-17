package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FileBinaryContentRepository implements BinaryContentRepository {

    private final Path DIRECTORY;
    private final String EXTENSION = ".ser";

    public FileBinaryContentRepository(){
        this.DIRECTORY= Paths.get(System.getProperty("user.dir"),
                "file-data-map", UserStatus.class.getSimpleName());
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
    public BinaryContent save(BinaryContent content) {
        Path path=resolvePath(content.getId());
        try(
                FileOutputStream fileOutputStream = new FileOutputStream(path.toFile());
                ObjectOutputStream objectOutputStream =new ObjectOutputStream(fileOutputStream)
        ) {
            objectOutputStream.writeObject(content); // 직렬화로 저장
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content;
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        BinaryContent binaryContentNullable =null;
        Path path=resolvePath(id); // id가 파일 경로 직접 순회
        if (Files.exists(path)){
            try (
                    FileInputStream fileInputStream=new FileInputStream(path.toFile());
                    ObjectInputStream objectInputStream=new ObjectInputStream(fileInputStream)
            ) {
                binaryContentNullable =(BinaryContent) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.ofNullable(binaryContentNullable);
    }

    @Override
    public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
        return ids.stream()
                .map(this::resolvePath)
                .filter(Files::exists)
                .map(path -> {
                    try (
                            FileInputStream fileInputStream = new FileInputStream(path.toFile());
                            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)
                    ) {
                        return (BinaryContent) objectInputStream.readObject();

                    } catch (IOException | ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
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
