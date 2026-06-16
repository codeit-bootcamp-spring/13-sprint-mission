package com.sprint.mission.discodeit.repository.file;


import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
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
public class FileChannelRepository implements ChannelRepository {

    private final Path DIRECTORY;
    private final String EXTENSION=".ser";

    public FileChannelRepository(){
        this.DIRECTORY= Paths.get(System.getProperty("user.dir"),
                "file-data-map", Channel.class.getSimpleName());
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
    public Channel save(Channel channel) {
        Path path=resolvePath(channel.getId()); // 경로 포함한 파일명은 파일을 읽는 부분에서 처리
        try (
                FileOutputStream fileOutputStream=new FileOutputStream(path.toFile());
                ObjectOutputStream objectOutputStream=new ObjectOutputStream(fileOutputStream)
        ){
            objectOutputStream.writeObject(channel);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return channel;
    }

    @Override
    public Optional<Channel> findById(UUID id) {
        Channel channelNullable=null;
        Path path=resolvePath(id);
        if (Files.exists(path)){
            try (
                    FileInputStream fileInputStream=new FileInputStream(path.toFile());
                    ObjectInputStream objectInputStream=new ObjectInputStream(fileInputStream)
            ){
                channelNullable=(Channel) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.ofNullable(channelNullable);
    }

    @Override
    public List<Channel> findAll() {
        try (Stream<Path> paths=Files.list(DIRECTORY)){
            return paths
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(path -> {
                        try (
                                FileInputStream fileInputStream=new FileInputStream(path.toFile());
                                ObjectInputStream objectInputStream=new ObjectInputStream(fileInputStream)
                        ){
                            return (Channel)objectInputStream.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
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
