package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.config.StorageProperties;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
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

//BinaryContentRepository의 File 기반 구현체
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
@Repository
public class FileBinaryContentRepository implements BinaryContentRepository {
    private final Path DIRECTORY; //파일이 저장될 디렉토리 경로
    private final String EXTENSION; //저장 파일 확장자(.dat, .bin 등)

    public FileBinaryContentRepository(StorageProperties properties) { //생성자. storageProperties를 통해 외부 설정값 주입
        this.EXTENSION = properties.getExtension();
        //저장 경로 생성
        this.DIRECTORY = Paths.get(System.getProperty("user.dir"), properties.getRootPath(), BinaryContent.class.getSimpleName());
        if (Files.notExists(DIRECTORY)) { //디렉토리가 존재하지 않으면 생성
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

    @Override //BinaryContent 저장
    public BinaryContent save(BinaryContent binaryContent) {
        Path path = resolvePath(binaryContent.getId());
        try (
                FileOutputStream fos = new FileOutputStream(path.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(binaryContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return binaryContent;
    }

    @Override //ID로 파일 조회(역질력화)
    public Optional<BinaryContent> findById(UUID id) {
        BinaryContent binaryContentNullable = null;
        Path path = resolvePath(id);
        if (Files.exists(path)) {
            try (
                    FileInputStream fis = new FileInputStream(path.toFile());
                    ObjectInputStream ois = new ObjectInputStream(fis)
            ) {
                binaryContentNullable = (BinaryContent) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.ofNullable(binaryContentNullable);
    }

    @Override //여러 ID에 해당하는 BinaryContent 조회
    public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
        try (Stream<Path> paths = Files.list(DIRECTORY)) {
            return paths
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis)
                        ) {
                            return (BinaryContent) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(content -> ids.contains(content.getId()))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override //파일 존재 여부 확인
    public boolean existsById(UUID id) {
        Path path = resolvePath(id);
        return Files.exists(path);
    }

    @Override //파일 삭제
    public void deleteById(UUID id) {
        Path path = resolvePath(id);
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
