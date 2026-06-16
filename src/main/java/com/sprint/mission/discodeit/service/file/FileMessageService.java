package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


public class FileMessageService implements MessageService {

    // 주소 설정, 변수를 대문자로
    private final Path DIRECTORY = Paths.get(System.getProperty("user.dir"), "data");
    private final Path filePath = DIRECTORY.resolve("messages.ser");

    public FileMessageService() {
        try {
            Files.createDirectories(DIRECTORY);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 메세지 목록을 파일에 쾅 구워버리는 직렬화 도구
    private void saveFile(List<Message> messages) {
        try (ObjectOutputStream oos = new ObjectOutputStream
                (new FileOutputStream(filePath.toFile()))) {
            oos.writeObject(messages);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 파일에서 메세지 목록을 통째로 읽어오는 역직렬화 도구
    @SuppressWarnings("unchecked") // Unchecked(검증 안 됨) 경고는 Suppress(억제/패스) 해라!
    private List<Message> readFile() {
        if (!Files.exists(filePath)) {
            return new ArrayList<>(); // 파일이 아직 없으면 빈 리스트 리턴
        }
        try (ObjectInputStream ois = new ObjectInputStream
                (new FileInputStream(filePath.toFile()))) {
            return (List<Message>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // 메세지 생성
    @Override
    public MessageResponse create(MessageCreateRequest dto) {
        List<Message> messages = readFile(); // 파일에서 기존 데이터 꺼냄
        Message message = new Message(dto.content());
        messages.add(message); // 새 메세지 상자 추가
        saveFile(messages); // 최종본 파일에 데이터 저장
        return new MessageResponse(
                message.getId(), dto.channelId(), dto.senderId(),
                dto.content(), Collections.emptyList(), Instant.now(), Instant.now()
        );
    }

    @Override
    public Optional<MessageResponse> findById(UUID id) {// 단건 조회
        List<Message> messages = readFile();
        for (Message foundMessage : messages) {
            if (foundMessage.getId().equals(id)) {
                return Optional.of(new MessageResponse(
                        foundMessage.getId(), foundMessage.getChannelId(), foundMessage.getAuthorId(),
                        foundMessage.getContent(), Collections.emptyList(), Instant.now(), Instant.now()
                ));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<MessageResponse> findAll(UUID channelId) { // 전체 조회
        return readFile().stream()
                .filter(m -> m.getChannelId().equals(channelId))
                .map(m -> new MessageResponse(
                        m.getId(), m.getChannelId(), m.getAuthorId(),
                        m.getContent(), Collections.emptyList(), Instant.now(), Instant.now()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageResponse> findAllByChannelId(UUID channelId) {
        return readFile().stream()
                .filter(m -> m.getChannelId().equals(channelId))
                .map(m -> new MessageResponse(
                        m.getId(), m.getChannelId(), m.getAuthorId(),
                        m.getContent(), Collections.emptyList(), Instant.now(), Instant.now()
                ))
                .collect(Collectors.toList());
    }

    // 메세지 수정
    @Override
    public MessageResponse update(UUID id, MessageUpdateRequest dto) {
        List<Message> messages = readFile();
        for (Message foundMessage : messages) {
            if (foundMessage.getId().equals(id)) {
                foundMessage.updateContent(new Message(dto.content()));
                saveFile(messages);
                break;
            }
        }
        return new MessageResponse(
                id, UUID.randomUUID(), UUID.randomUUID(),
                dto.content(), Collections.emptyList(), Instant.now(), Instant.now()
        );
    }

    // 메세지 삭제
    @Override
    public void delete(UUID id) {
        List<Message> foundMessage = readFile();
        foundMessage.removeIf(m->m.getId().equals(id));
        saveFile(foundMessage);
    }

}



/*
기본 요구사항
File IO를 통한 데이터 영속화
[ ]  JCF 대신 FileIO와 객체 직렬화를 활용해 메소드를 구현하세요.
서비스 구현체 분석
[ ] JCF*Service 구현체와 File*Service 구현체를 비교하여 공통점과 차이점을 발견해보세요.
[ ] "비즈니스 로직"과 관련된 코드를 식별해보세요.
[ ] "저장 로직"과 관련된 코드를 식별해보세요.
 */