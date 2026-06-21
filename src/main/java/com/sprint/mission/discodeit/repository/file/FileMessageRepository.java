package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository // File*Repository 구현체를 Repository 인터페이스의 Bean으로 등록
public class FileMessageRepository implements MessageRepository {

    private final Path DIRECTORY;
    private static final String EXTENSION=".ser";
    private static final String USER_DIRECTORY = System.getProperty("user.dir");
    private static final String FILE_STORAGE_DIR = "file-data-map"; // file-data-map 또한 처음 보는 사람도 알아볼 수 있도록 역할과 의미를 분명히 해야 한다

    public FileMessageRepository(){
        this.DIRECTORY= Paths.get(USER_DIRECTORY,
                FILE_STORAGE_DIR, Message.class.getSimpleName());
        /*
        가정: if msg.length()>10 - 매직넘버, 10이 뭘 뜻하는지 알 수 없다
        -> private static final int MSG_MAX_LENGTH=10; 형식적으로 작성해야 의미 담아야 한다
         */
        if (Files.notExists(DIRECTORY)){
            try {
                Files.createDirectories(DIRECTORY);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Path resolvePath(UUID id){
        return DIRECTORY.resolve(id+EXTENSION);
    }

    @Override
    public Message save(Message message) {
        Path path=resolvePath(message.getId());
        try(
                FileOutputStream fileOutputStream=new FileOutputStream(path.toFile());
                ObjectOutputStream objectOutputStream=new ObjectOutputStream(fileOutputStream)
        ) {
            objectOutputStream.writeObject(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return message;
    }

    @Override
    public Optional<Message> findById(UUID id) {
        Message messageNullable=null;
        Path path=resolvePath(id);
        if (Files.exists(path)){
            try (
                    FileInputStream fileInputStream=new FileInputStream(path.toFile());
                    ObjectInputStream objectInputStream=new ObjectInputStream(fileInputStream)
            ){
                messageNullable=(Message) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.ofNullable(messageNullable);
    }

    @Override
    public List<Message> findAll() {
        try (Stream<Path> paths=Files.list(DIRECTORY)){
            return paths
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(path -> {
                        try (
                                FileInputStream fileInputStream=new FileInputStream(path.toFile());
                                ObjectInputStream objectInputStream=new ObjectInputStream(fileInputStream)
                        ){
                            return (Message)objectInputStream.readObject();

                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Message> findAllByChannelId(UUID channelId) {
        try (Stream<Path> paths=Files.list(DIRECTORY)){
            return paths
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(path -> {
                        try (
                                FileInputStream fileInputStream=new FileInputStream(path.toFile());
                                ObjectInputStream objectInputStream=new ObjectInputStream(fileInputStream)
                        ){
                            return (Message)objectInputStream.readObject();

                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }).filter(message -> message.getChannelId().equals(channelId))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
