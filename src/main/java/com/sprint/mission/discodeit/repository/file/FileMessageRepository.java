package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

public class FileMessageRepository implements MessageRepository {

    // messages 디렉토리 경로
    private final Path directory;

    public FileMessageRepository() {
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
    public void save(Message message) {
        Path filePath = directory.resolve(message.getId() + ".ser");

        try (FileOutputStream fos = new FileOutputStream(filePath.toFile());
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message findById(UUID id) {
        Path filePath = directory.resolve(id + ".ser");

        if (!Files.exists(filePath)) {
            return null;
        }

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             ObjectInputStream ois = new ObjectInputStream(fis))
        {
            return (Message) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Message> findAll() {
        if (!Files.exists(directory)) {
            return List.of();
        }

        try {
            return Files.list(directory).map(path -> {
                try (FileInputStream fis = new FileInputStream(path.toFile());
                     ObjectInputStream ois = new ObjectInputStream(fis))
                {
                    return (Message) ois.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }).toList();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(UUID id) {
        Path filePath = directory.resolve(id + ".ser");
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
