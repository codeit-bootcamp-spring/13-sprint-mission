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
    private static final String EXTENSION = ".ser"; // 상수 변수로 선언
    private static final String USER_DIRECTORY = System.getProperty("user.dir"); // user.dir 또한 상수로 표현
    private static final String FILE_STORAGE_DIR = "file-data-map"; // file-data-map 또한 처음 보는 사람도 알아볼 수 있도록 역할과 의미를 분명히 해야 한다

    public FileBinaryContentRepository(){
        this.DIRECTORY= Paths.get(USER_DIRECTORY, FILE_STORAGE_DIR, UserStatus.class.getSimpleName());
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
                    try ( // OS를 통해 읽고, OS를 통해 리소스를 사용하는데 반납을 하지 않으면 리소스가 고갈되고 문제가 발생한다
                          // 따라서 clear, close 반환이 필요하다
                          // InputStream을 open하면 CRUD 관련해 리소스를 반환해줘야 한다
                          // try() 문법을 사용해 파일 I/O를 마치면 JVM이 자동으로 close()를 호출하여 clear() 메서드를 호출하지 않아도 된다
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
