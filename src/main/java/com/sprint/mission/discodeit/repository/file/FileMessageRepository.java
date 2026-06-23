package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.config.StorageProperties;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
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

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
@Repository
//MessageRepository의 File 기반 구현체
public class FileMessageRepository implements MessageRepository {
    private final Path DIRECTORY; //메시지 파일 저장 디렉토리
    private final String EXTENSION; //파일 확장자

    public FileMessageRepository(StorageProperties properties) { //생성자. storageProperties를 통해 외부 설정값 주입
        this.EXTENSION = properties.getExtension();
        // 저장 경로
        this.DIRECTORY = Paths.get(System.getProperty("user.dir"), properties.getRootPath(), Message.class.getSimpleName());
        if (Files.notExists(DIRECTORY)) { //디렉토리가 없으면 생성
            try {
                Files.createDirectories(DIRECTORY);
            }catch (IOException e){
                throw new RuntimeException(e);
            }
        }
    }

    // UUID->파일 경로 변환
    private Path resolvePath(UUID id){ return DIRECTORY.resolve(id + EXTENSION); }

    @Override //메시지 저장
    public Message save(Message message) {
        Path path = resolvePath(message.getId());
        try(
                FileOutputStream fos = new FileOutputStream(path.toFile()); //파일 출력 스트림 생성
                ObjectOutputStream oos = new ObjectOutputStream(fos) //객체 직렬화 스트림 생성
        ) {
            oos.writeObject(message); //Message 객체를 파일에 저장
        }catch (IOException e) {throw new RuntimeException(e);}
        return message;
    }

    @Override //메시지 단건조회
    public Optional<Message> findById(UUID id) {
        Message messageNullable = null;
        Path path = resolvePath(id);
        if (Files.exists(path)) {
            try ( //파일이 존재할 때만 읽기 수행
                    FileInputStream fis = new FileInputStream(path.toFile()); //파일 입력 스트림
                    ObjectInputStream ois = new ObjectInputStream(fis) //객체 역직렬화 스트림
            ) { //파일 데이터를 Message 객체로 복원
                messageNullable = (Message) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {throw new RuntimeException(e);}
        }
        return Optional.ofNullable(messageNullable);
    }

    @Override //전체 메시지 조회
    public List<Message> findAllByChannelId(UUID channelId){
        try (Stream<Path> paths = Files.list(DIRECTORY)) {
            return paths
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis)
                        ){
                            return (Message) ois.readObject();
                        }catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(message -> message.getChannelId().equals(channelId))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override //메시지 존재 여부 확인
    public boolean existsById(UUID id) {
        Path path = resolvePath(id);
        return Files.exists(path);
    }

    @Override //메시지 삭제
    public void deleteById(UUID id) {
        Path path = resolvePath(id);
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override //채널 기준 메시지 전체 삭제
    public void deleteAllByChannelId(UUID channelId) {
        this.findAllByChannelId(channelId)
                .forEach(message ->this.deleteById(message.getId()));
    }
}