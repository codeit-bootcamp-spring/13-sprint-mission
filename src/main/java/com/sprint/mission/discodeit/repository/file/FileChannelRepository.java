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

@Repository
//ChannelRepository 인터페이스의 파일 기반(File I/o) 구현체
public class FileChannelRepository implements ChannelRepository {
    private final Path DIRECTORY; //Channel 파일들이 저장될 디렉토리 경로
    private final String EXTENSION = ".ser"; //직렬화 파일 확장자

    public FileChannelRepository() { //Repository 생성 시 저장 폴더가 존재하지 않으면 자동 생성
        this.DIRECTORY = Paths.get(System.getProperty("user.dir"), "file-data-map", Channel.class.getSimpleName());
        if (Files.notExists(DIRECTORY)) { //디렉토리가 존재하지 않으면 생성
            try {
                Files.createDirectories(DIRECTORY);
            }catch (IOException e){
                throw new RuntimeException(e);
            }
        }
    }

    //UUID를 파일 경로로 변환
    private Path resolvePath(UUID id){ return DIRECTORY.resolve(id + EXTENSION); }

    @Override //채널 저장
    public Channel save(Channel channel) {
        Path path = resolvePath(channel.getId()); //저장 파일 경로 생성
        try(
                FileOutputStream fos = new FileOutputStream(path.toFile()); //파일 출력 스트림 생성
                ObjectOutputStream oos = new ObjectOutputStream(fos) //객체 직렬화 스트림 생성
        ) {
            oos.writeObject(channel); //객체를 파일에 저장
        }catch (IOException e) {throw new RuntimeException(e);}
        return channel;
    }

    @Override //채널 조회
    public Optional<Channel> findById(UUID id) {
        Channel channelNullable = null;
        Path path = resolvePath(id);
        if (Files.exists(path)) { //파일 존재 시에만 읽기 수행
            try (
                    FileInputStream fis = new FileInputStream(path.toFile());
                    ObjectInputStream ois = new ObjectInputStream(fis)
            ) { //파일->객체 변환
                channelNullable = (Channel) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {throw new RuntimeException(e);}
        }
        return Optional.ofNullable(channelNullable);
    }

    @Override //전체 체널 조회
    public List<Channel> findAll(){
        try {
            return Files.list(DIRECTORY)
                    .filter(path -> path.toString().endsWith(EXTENSION)) //.ser 파일만 조회
                    .map(path -> { //파일->채널 객체 변환
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis)
                        ){
                            return (Channel) ois.readObject();
                                }catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList(); //Stream->List 변환
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override //채널 존재 여부 확인
    public boolean existsById(UUID id) {
        Path path = resolvePath(id);
        return Files.exists(path);
    }

    @Override //채널 삭제
    public void deleteById(UUID id) {
        Path path = resolvePath(id);
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}