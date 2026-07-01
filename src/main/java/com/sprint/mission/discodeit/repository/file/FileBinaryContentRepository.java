package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.config.StorageProperties;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

//BinaryContentRepository의 File 기반 구현체
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
@Repository
public class FileBinaryContentRepository implements BinaryContentRepository {
    private final Path DIRECTORY; //파일이 저장될 디렉토리 경로
    private final String EXTENSION; //저장 파일 확장자(.dat, .bin 등)
    private final FileLockProvider fileLockProvider; //동일한 파일에 여러스레드가 동시에 접근하는 것을 방지하기 위한 Lock 제공 객체

    //생성자. StorageProperties에서 저장 관련 설정을 읽어오고, 저장 디렉토리를 생성한 후 FileLockProvider룰 주입받음.
    public FileBinaryContentRepository(StorageProperties properties,
                                       @Value(".discodeit") String fileDirectory,
                                       FileLockProvider fileLockProvider) { //생성자. storageProperties를 통해 외부 설정값 주입
        this.EXTENSION = properties.getExtension();
        //저장 경로 생성
        this.DIRECTORY = Paths.get(System.getProperty("user.dir"), fileDirectory,
                properties.getRootPath(), BinaryContent.class.getSimpleName());
        if (Files.notExists(DIRECTORY)) { //디렉토리가 존재하지 않으면 생성
            try {
                Files.createDirectories(DIRECTORY);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.fileLockProvider = fileLockProvider; //FileLockProvider를 저장함.
    }

    //UUID->파일 경로 변환
    private Path resolvePath(UUID id) {
        return DIRECTORY.resolve(id + EXTENSION);
    }

    @Override //BinaryContent 저장
    public BinaryContent save(BinaryContent binaryContent) {
        Path path = resolvePath(binaryContent.getId()); //저장할 파일의 경로를 생성함.
        ReentrantLock lock = fileLockProvider.getLock(path); //해당 파일에 대한 Lock을 가져움
        lock.lock(); //다른 스레드가 접근하지 못하도록 Lock을 획득함.
        try (
                FileOutputStream fos = new FileOutputStream(path.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(binaryContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            lock.unlock(); //작업이 끝나면 반드시 Lock을 해제함.
        }
        return binaryContent;
    }

    @Override //ID로 파일 조회(역질력화)
    public Optional<BinaryContent> findById(UUID id) {
        BinaryContent binaryContentNullable = null;
        Path path = resolvePath(id);
        ReentrantLock lock = fileLockProvider.getLock(path);
        lock.lock();
        if (Files.exists(path)) {
            try (
                    FileInputStream fis = new FileInputStream(path.toFile());
                    ObjectInputStream ois = new ObjectInputStream(fis)
            ) {
                binaryContentNullable = (BinaryContent) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }finally {
                lock.unlock();
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
                        ReentrantLock lock = fileLockProvider.getLock(path);
                        lock.lock();
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis)
                        ) {
                            return (BinaryContent) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }finally {
                            lock.unlock();
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
