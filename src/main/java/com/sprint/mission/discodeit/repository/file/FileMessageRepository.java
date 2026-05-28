package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileMessageRepository implements MessageRepository {

    // 주소 설정
    private final Path directory = Paths.get(System.getProperty("message.dir"), "data");
    private final Path filePath = directory.resolve("messages.ser");

    public FileMessageRepository() {
        try {
            Files.createDirectories(directory);
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
    public Message create(Message message) {
        List<Message> messages = readFile(); // 파일에서 기존 데이터 꺼냄
        messages.add(message); // 새 메세지 상자 추가
        saveFile(messages); // 최종본 파일에 데이터 저장
        return message;
    }

    @Override
    public Message findByContent(String content) { // 단건 조회
        List<Message> messages = readFile();
        for (Message foundMessage : messages) {
            if (foundMessage.getContent().equals(content)) {
                return foundMessage;
            }
        }
        return null;
    }

    public List<Message> findAll() { // 전체 조회
        return readFile();
    }

    // 메세지 수정
    @Override
    public void update(Message requestMessage) {
        List<Message> messages = readFile();
        for (Message foundMessage : messages) {
            if (foundMessage.getContent().equals(requestMessage.getContent())) {
                foundMessage.updateContent(requestMessage);
                break;
            }
        }
    }

    // 메세지 삭제
    @Override
    public void delete(String content) {
        List<Message> foundMessage = readFile();
        foundMessage.removeIf(m->m.getContent().equals(content));
        saveFile(foundMessage);
    }
}
/*
레포지토리 설계 및 구현
[ ] 다음의 조건을 만족하는 레포지토리 인터페이스의 구현체를 작성하세요.
[ ] 기존에 구현한 File*Service 구현체의 "저장 로직"과 관련된 코드를 참고하여 구현하세요.
 */