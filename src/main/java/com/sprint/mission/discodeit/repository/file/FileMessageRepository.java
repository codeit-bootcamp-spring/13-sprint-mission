package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileMessageRepository implements MessageRepository {

    private List<Message> storage;
    private final Path filePath;

    public FileMessageRepository() {

        this.filePath = Path.of("data/messages.ser");

        try {
            Files.createDirectories(filePath.getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        storage = load();
    }


    @Override
    public Message save(Message message) {

        for (int i = 0; i < storage.size(); i++) {
            if (storage.get(i).getId().equals(message.getId())) {
                storage.remove(i);
                break;
            }
        }

        storage.add(message);
        saveToFile();
        return message;
    }

    @Override
    public Message findById(UUID id) {
        for (Message message : storage) {
            if (message.getId().equals(id)) {
                return message;
            }
        }

        return null;
    }

    @Override
    public List<Message> findAll() {
        return new ArrayList<>(storage);
    }

    @Override
    public void delete(UUID id) {
        for (int i = 0; i < storage.size(); i++) {
            if (storage.get(i).getId().equals(id)) {
                storage.remove(i);
                break;
            }
        }

        saveToFile();

    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(filePath))) {
            oos.writeObject(storage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Message> load() {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(filePath))) {
            return (List<Message>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
