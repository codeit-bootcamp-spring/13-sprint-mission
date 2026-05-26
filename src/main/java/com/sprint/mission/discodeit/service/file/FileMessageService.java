package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.service.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileMessageService implements MessageService, Serializable{

    private static final long serialVersionUID = 1L;
    private final List<Message> messages = new ArrayList<>();
    private final Path messagePath;

    public FileMessageService(Path messagePath) {
        this.messagePath = messagePath;
        loadFromFile();
    }

    @Override
    public Message read(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("메세지 ID가 없습니다.");
        }

        for (Message message : messages) {
            if (message.getId().equals(id)) {
                return message;
            }
        }

        throw new IllegalArgumentException("존재하지 않는 메세지 ID입니다.");
    }

    @Override
    public void create(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("메세지 정보가 없습니다.");
        }

        messages.add(message);
        saveToFile();
    }

    @Override
    public List<Message> readAll() {
        return new ArrayList<>(messages);
    }

    @Override
    public void update(UUID id, Message message) {
        if (id == null) {
            throw new IllegalArgumentException("메세지 ID는 필수입니다.");
        }

        if (message == null || message.getId() == null) {
            throw new IllegalArgumentException("메세지 정보가 없습니다.");
        }

        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getId().equals(id)) {
                messages.set(i, message);
                saveToFile();
                return;
            }
        }

        throw new IllegalArgumentException("존재하지 않는 메세지 ID입니다.");
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("메세지 ID는 필수입니다.");
        }

        boolean removed = messages.removeIf(message -> message.getId().equals(id));

        if (!removed) {
            throw new IllegalArgumentException("존재하지 않는 메세지 ID입니다.");
        }

        saveToFile();
    }

    private void saveToFile() {
        try {
            Path parent = messagePath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (ObjectOutputStream oos =
                         new ObjectOutputStream(
                                 new BufferedOutputStream(
                                         Files.newOutputStream(messagePath)))) {

                oos.writeObject(new ArrayList<>(messages));
            }

        } catch (IOException e) {
            throw new RuntimeException("메세지 파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        if (!Files.exists(messagePath)) {
            return;
        }

        try (ObjectInputStream ois =
                     new ObjectInputStream(
                             new BufferedInputStream(
                                     Files.newInputStream(messagePath)))) {

            messages.addAll((List<Message>) ois.readObject());

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("메세지 파일 불러오기 중 오류가 발생했습니다.", e);
        }
    }
}