package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FileMessageRepository implements MessageRepository {

    private final Path filePath = Paths.get(System.getProperty("user.dir"), "messages.ser");

    @SuppressWarnings("unchecked")
    private List<Message> readFile() {
        if(!Files.exists(filePath)) {
            return new ArrayList<>();
        }
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
            ObjectInputStream ois = new ObjectInputStream(fis)) {
                return (List<Message>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
            return new  ArrayList<>();
        }
    }

    private void saveFile(List<Message> messages) {
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile());
        ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(messages);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message create(Message message) {
        List<Message> messages = readFile();
        messages.add(message);
        saveFile(messages);
        return message;
    }

    @Override
    public Optional<Message> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return readFile().stream()
                .filter(m -> id.equals(m.getId()))
                .findFirst();
    }

    @Override
    public Message findByContent(String content) {
        return readFile().stream()
                .filter(m -> m.getContent().equals(content))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Message> findAll() {
        return readFile();
    }

    @Override
    public void update(Message requestMessage) {
        List<Message> messages = readFile();
        messages.replaceAll(m -> m.getContent()
                .equals(requestMessage.getContent())
                ? requestMessage : m);
        saveFile(messages);
    }

    @Override
    public void delete(UUID id) {
        List<Message> messages = readFile();
        messages.removeIf(m -> m.getId().equals(id));
        saveFile(messages);
    }

    @Override
    public void deleteByChannelId(UUID channelId) {
        if (channelId == null) {
            return;
        }
        List<Message> messages = readFile();
        messages.removeIf(m -> m.getChannelId() != null && m.getChannelId().equals(channelId));
        saveFile(messages);
    }

}
