package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.service.ChannelService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

//파일 IO 기반 서비스처럼 보이지만,
//실제 동작은 JCFChannelService에 모두 "위임"하는 클래스
public class FileChannelService implements ChannelService {
    private final Path DIRECTORY; //Channel 데이터 저장 디렉토리
    private final String EXTENSION = ".ser"; //직렬화 파일 확장자

    public FileChannelService() { //서비스 객체 생성 시 저장 폴더를 준비하고 저장폴더가 존재하지 않는다면 자동 생성
        this.DIRECTORY = Paths.get(System.getProperty("user.dir"), "file-data-map", Channel.class.getSimpleName());
        if (Files.notExists(DIRECTORY)) {
            try {
                Files.createDirectories(DIRECTORY);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //UUID를 실제 파일 경로로 변환
    private Path resolvePath(UUID id) { return DIRECTORY.resolve(id + EXTENSION); }

    @Override //채널 생성
    public Channel create(ChannelType type, String name, String description) {
        Channel channel = new Channel(type, name, description);
        Path path = resolvePath(channel.getId()); //저장할 파일 경로 생성
        try ( //사용이 끝난 스트림 자동 close()
                FileOutputStream fos = new FileOutputStream(path.toFile()); //파일출력스트림: 파일에 데잍터를 기록
                ObjectOutputStream oos = new ObjectOutputStream(fos); //객체출력스트림: java 객체를 직렬화하여 저장
        ) {
            oos.writeObject(channel); //channel 객체 저장
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return channel;
    }

    @Override //채널 단건조회
    public Channel find(UUID channelId) { //UUID에 해당하는 파일을 읽어 Channel 객체로 복원함
        Channel channelNullable = null; //조회 결과 저장 변수
        Path path = resolvePath(channelId); //조회 대상 파일 경로 생성
        if (Files.exists(path)) { //파일 존재 여부 확인
            try (
                    FileInputStream fis = new FileInputStream(path.toFile()); //파일 입력 스트림
                    ObjectInputStream ois = new ObjectInputStream(fis) //객체 입력 스트림 (파일->객체 복원)
            ) { //역질렬화 수행. 파일에 저장된 channel 객체복원
                channelNullable = (Channel) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } // 객체가 존재하지 않으면 예외 발생
        return Optional.ofNullable(channelNullable)
                .orElseThrow(()->new NoSuchElementException("Channel with id " + channelId + " not found"));
    }

    @Override //전체 채널 조회
    public List<Channel> findAll() {
        try {
            return Files.list(DIRECTORY)
                    .filter(path -> path.toString().endsWith(EXTENSION)) //.ser 파일만 필터링
                    .map(path -> { //파일-> channel 객체 변환
                        try(
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis)
                        ){ //역질렬화 수행
                            return (Channel) ois.readObject();
                        }catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }) //stream-> List 변환
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override //채널 수정
    public Channel update(UUID channelId, String newName, String newDescription) {
        Channel channelNullable = null;
        Path path = resolvePath(channelId); //수정 대상 파일 결오 생성
        if (Files.exists(path)) { //파일 존재 시 읽기
            try (
                    FileInputStream fis = new FileInputStream(path.toFile());
                    ObjectInputStream ois = new ObjectInputStream(fis)
            ) { //파일->channel 객체 복원
                channelNullable = (Channel) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } //존재하지 않으면 예외 발생
        Channel channel = Optional.ofNullable(channelNullable)
                .orElseThrow(()->new NoSuchElementException("Channel with id " + channelId + " not found"));
        channel.update(newName,newDescription); //엔티티 내부 update 수행

        try ( //수정된 객체를 파일에 다시 저장
                FileOutputStream fis = new FileOutputStream(path.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fis)
        ){
            oos.writeObject(channel);
        }catch (IOException e){
            throw new RuntimeException(e);
        }

        return channel;
    }

    @Override //채널 삭제
    public void delete(UUID channelId) {
        Path path = resolvePath(channelId); //삭제 대상 파일 경로
        if (Files.notExists(path)) { //파일이 존재하지 않으면 예외 발생
            throw new NoSuchElementException("Channel with id " + channelId + " not found");
        }
        try { //실제 파일 삭제
            Files.delete(path);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

}
