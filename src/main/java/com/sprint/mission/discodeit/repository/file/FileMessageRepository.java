package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Repository
public class FileMessageRepository implements MessageRepository {

    private final List<Message> messages = new ArrayList<>();
    private final Path messagePath;

    public FileMessageRepository() {
        this(Paths.get("data/messages.ser"));
    }

    public FileMessageRepository(Path messagesPath) {
        this.messagePath = messagesPath;
        loadFromFile();
    }

    @Override
    public void create(Message message) {
        messages.add(message);
        saveToFile();
    }

    public boolean exists(UUID id) {
        return messages.stream().anyMatch(message -> message.getId().equals(id));
    }

    @Override
    public Message find(UUID id) {
        for (Message message : messages) {
            if (message.getId().equals(id)) {
                return message;
            }
        }

        return null;
    }

    @Override
    public List<Message> findAll() {
        return new ArrayList<>(messages);
    }

    @Override
    public void update(UUID id, Message message) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getId().equals(id)) {
                messages.set(i, message);
                saveToFile();
                return;
            }
        }
    }

    @Override
    public void delete(UUID id) {
        messages.removeIf(message -> message.getId().equals(id));
        saveToFile();
    }

    private void saveToFile() {
        try {
            Path parent = messagePath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(messagePath)))) {

                oos.writeObject(messages);
            }

        } catch (IOException e) {
            throw new RuntimeException("메시지 파일 저장 중 오류가 발생했습니다.", e);
        }
    }


    private void loadFromFile() {
        if (!Files.exists(messagePath)) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(messagePath)))) {

            List<Message> loadedMessages = (List<Message>) ois.readObject();

            messages.clear();
            messages.addAll(loadedMessages);

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("메시지 파일 불러오기 중 오류가 발생했습니다.", e);
        }
    }
}
