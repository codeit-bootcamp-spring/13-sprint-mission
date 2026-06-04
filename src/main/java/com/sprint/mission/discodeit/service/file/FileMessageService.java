package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileMessageService implements MessageService {

    public static final Path MESSAGE_PATH = Paths.get("data/messages.ser");

    private final Map<UUID, Message> messagesMap;

    public FileMessageService() {
        this.messagesMap = loadFromFile();
    }


    @Override
    public Message create(Message message) {
        messagesMap.put(message.getId(), message);
        saveToFile();
        return message;
    }

    @Override
    public Message read(UUID id) {
        Message message = messagesMap.get(id);
        if (message == null) {
            throw new IllegalArgumentException("메세지 내역이 존재하지 않습니다.");
        }
        return message;
    }

    @Override
    public List<Message> readAll() {
        return new ArrayList<>(messagesMap.values());
    }

    @Override
    public Message update(UUID id, String content) {
        Message message = messagesMap.get(id);
        if (message == null) {
            throw new IllegalArgumentException("존재하지 않는 메세지 입니다.");
        }
        messagesMap.get(id).updateContent(content);
        saveToFile();
        return messagesMap.get(id);
    }

    @Override
    public void delete(Message message) {
        messagesMap.remove(message.getId());
        saveToFile();
    }

    private void saveToFile() {
        try {
            Files.createDirectories(MESSAGE_PATH.getParent());
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(MESSAGE_PATH)))) {
                oos.writeObject(new LinkedHashMap<>(messagesMap));
            }
            System.out.println("메세지 저장 완료: " + MESSAGE_PATH.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("메세지 저장에 실패했습니다.", e);
        }
    }

    private Map<UUID, Message> loadFromFile() {
        if (!Files.exists(MESSAGE_PATH)) {
            System.out.println("저장된 메세지 파일 없음. 빈 Map으로 시작합니다.");
            return new LinkedHashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(MESSAGE_PATH)))) {
            Map<UUID, Message> map = (Map<UUID, Message>) ois.readObject();
            System.out.println("메세지 불러오기 완료: " + MESSAGE_PATH.toAbsolutePath());
            return map;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("메세지 불러오기에 실패했습니다.", e);
        }
    }
}
