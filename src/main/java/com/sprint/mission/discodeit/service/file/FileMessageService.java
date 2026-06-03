package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileMessageService implements MessageService {

    // messages 디렉토리 경로
    private final Path directory;

    public FileMessageService() {
        this.directory = Paths.get(
                System.getProperty("user.dir"),
                "data",
                "messages"
        );
        init(directory);
    }

    public static void init(Path directory) {
        // 저장할 경로의 파일 초기화
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void createMessage(Message message) {
        Path filePath =
                directory.resolve(message.getId() + ".ser");

        save(filePath, message);
    }

    @Override
    public Message findMessage(UUID id) {
        Path filePath =
                directory.resolve("Message " + id + ".ser");
        if (!Files.exists(filePath)) {
            return null;
        }
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            return (Message) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Message> findAllMessages() {
        if (Files.exists(directory)) {
            try {
                List<Message> list = Files.list(directory)
                        .map(path -> {
                            try (
                                    FileInputStream fis = new FileInputStream(path.toFile());
                                    ObjectInputStream ois = new ObjectInputStream(fis)
                            ) {
                                Object data = ois.readObject();
                                return (Message) data;
                            } catch (IOException | ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .toList();
                return list;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void updateMessage(UUID id, String content) {
        Message message = this.findMessage(id);
        if (message == null) {
            return;
        }

        message.update(content);

        Path filePath = directory.resolve("Message " + id + ".ser");

        save(filePath, message);
    }

    @Override
    public void deleteMessage(UUID id) {
        Path filePath = directory.resolve("Message " + id + ".ser");

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 객체 저장
    private <T> void save(Path filePath, T data) {
        try(
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
