package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

//파일 IO 기반 서비스처럼 이름만 붇었고,
//실제 동작은 JCFMessageService에 모두 "위임"하는 클래스
public class FileMessageService implements MessageService {
    private final Path DIRECTORY; //message 파일 저장 디렉토리
    private final String EXTENSION = ".ser"; //직렬화 파일 확장자
    private final ChannelService channelService; //메시지가 작성될 채널 존재 여부를 검증하기 위한 서비스
    private final UserService userService; //메시지를 작성하는 사용자 존재 여부를 검증하기 위한 서비스

    public FileMessageService(ChannelService channelService, UserService userService) {
        this.DIRECTORY = Paths.get(System.getProperty("user.dir"), "file-data-map", Message.class.getSimpleName());
        if (Files.notExists(DIRECTORY)) {
            try {
                Files.createDirectories(DIRECTORY);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } //의존성 주입
        this.channelService = channelService;
        this.userService = userService;
    }

    private Path resolvePath(UUID id) { return DIRECTORY.resolve(id + EXTENSION); }

    @Override //메시지 생성
    public Message create(String content, UUID channelId, UUID userId) {
        try{ //채널과 사용자 존재 여부 검증. 존재하지 않으면 NoSuchElementException 발생
            channelService.find(channelId);
            userService.find(userId);
        }catch(NoSuchElementException e){
            throw e;
        }

        Message message = new Message(content, channelId, userId); //Message 객체 생성
        Path path = resolvePath(message.getId()); //저장할 파일 경로 생성
        try ( //객체 직렬화 후 저장
                FileOutputStream fos = new FileOutputStream(path.toFile()); //파일 출력 스트림
                ObjectOutputStream oos = new ObjectOutputStream(fos); //객체 출력 스트림
        ) { //Message 객체를 파일로 저장
            oos.writeObject(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return message;
    }

    @Override //메시지 단건조회
    public Message find(UUID messageId) {
        Message messageNullable = null;
        Path path = resolvePath(messageId);
        if (Files.exists(path)) { //파일 존재 여부 확인
            try (
                    FileInputStream fis = new FileInputStream(path.toFile()); //파일 입력 스트림
                    ObjectInputStream ois = new ObjectInputStream(fis) //객체 입력 스트림
            ) { //파일->Message 객체 복원
                messageNullable = (Message) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } //존재하지 않으면 예외 발생
        return Optional.ofNullable(messageNullable)
                .orElseThrow(()->new NoSuchElementException("Message with id " + messageId + " not found"));
    }

    @Override //전체 메시지 조회. Message 폴더의 모든 .ser 파일을 읽어서 List<Message> 형태로 변환함.
    public List<Message> findAll() {
        try {
            return Files.list(DIRECTORY)
                    .filter(path -> path.toString().endsWith(EXTENSION)) //.ser 파일만 선택
                    .map(path -> { //파일->Message 객체 변환
                        try(
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis)
                        ){ //역직렬화 수행
                            return (Message) ois.readObject();
                        }catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override //메시지 수정
    public Message update(UUID messageId, String newContent) {
        Message messageNullable = null;
        Path path = resolvePath(messageId); //수정 대상 파일 경로
        if (Files.exists(path)) { //파일 존재 시 읽기
            try (
                    FileInputStream fis = new FileInputStream(path.toFile());
                    ObjectInputStream ois = new ObjectInputStream(fis)
            ) { //Message 객체 복원
                messageNullable = (Message) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } //존재하지 않으면 예외 발생
        Message message = Optional.ofNullable(messageNullable)
                .orElseThrow(()->new NoSuchElementException("Message with id " + messageId + " not found"));
        message.update(newContent);

        // 수정된 객체를 파일에 다시 저장
        try (
                FileOutputStream fis = new FileOutputStream(path.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fis)
        ){
            oos.writeObject(message);
        }catch (IOException e){
            throw new RuntimeException(e);
        }

        return message;
    }

    @Override //메시지 삭제. UUID에 해당하는 .ser 파일 삭제
    public void delete(UUID messageId) {
        Path path = resolvePath(messageId); //삭제 대상 파일 경로
        if (Files.notExists(path)) { //파일이 존재하지 않으면 예외 발생
            throw new NoSuchElementException("Message with id " + messageId + " not found");
        }
        try {
            Files.delete(path); //실제 파일 삭제
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }


}
