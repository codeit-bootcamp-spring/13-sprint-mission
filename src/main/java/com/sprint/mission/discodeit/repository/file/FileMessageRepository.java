package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@ConditionalOnProperty(
        prefix = "discodeit.repository",
        name = "type",
        havingValue = "file"
)
public class FileMessageRepository implements MessageRepository {

    private final Path path;

    public FileMessageRepository(@Value("${file.path.message}") String path) {
        this.path = Paths.get(path);

        try {
            Files.createDirectories(this.path.getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveFile(List<Message> messages) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
            oos.writeObject(new ArrayList<>(messages));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Message> loadFile() {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            return (List<Message>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Message message) {
        List<Message> messages = loadFile();

        boolean isUpdated = false;
        for (int i = 0; i < messages.size(); i++) {
            if(messages.get(i).getId().equals(message.getId())) {
                messages.set(i, message);
            }
        }

        if(!isUpdated) {
            messages.add(message);
        }
        saveFile(messages);
    }

    @Override
    public Message findById(UUID id) {
        List<Message> messages = loadFile();

        for (Message message : messages) {
            if(message.getId().equals(id)) {
                return message;
            }
        }
        return null;
    }

    @Override
    public List<Message> findAll() {
        return loadFile();
    }

    @Override
    public void delete(UUID id) {
        List<Message> messages = loadFile();

        if(messages.removeIf(message -> message.getId().equals(id))) {
            saveFile(messages);
        }
    }
}
