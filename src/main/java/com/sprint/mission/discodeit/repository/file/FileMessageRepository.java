package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//MessageRepository 인터페이스의 파일 기반(File I/O) 구현체
public class FileMessageRepository implements MessageRepository {
    private final Path DIRECTORY;
    private final String EXTENSION = ".ser";

    public FileMessageRepository() {
        this.DIRECTORY = Paths.get(System.getProperty("user.dir"), "file-data-map", Message.class.getSimpleName());
        if (Files.notExists(DIRECTORY)) {
            try {
                Files.createDirectories(DIRECTORY);
            }catch (IOException e){
                throw new RuntimeException(e);
            }
        }
    }

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
    public List<Message> findAll(){
        try {
            return Files.list(DIRECTORY)
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
}