package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class FileMessageRepository implements MessageRepository {
    private final Path path;

    public FileMessageRepository(String path) {
        this.path = Paths.get(path);
    }

    private void saveFile(List<Message> messages) {
        Path parent = path.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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
    public Message create(Message message) {
        List<Message> messages = loadFile();
        messages.add(message);
        saveFile(messages);
        return message;
    }

    @Override
    public Message read(UUID id) {
        List<Message> messages = loadFile();
        for (Message message : messages) {
            if (message.getId().equals(id)) {
                return message;
            }
        }
        return null;
    }

    @Override
    public List<Message> readAll() {
        return loadFile();
    }

    @Override
    public void update(Message message) {
        List<Message> messages = loadFile();

        for (Message m : messages) {
            if (m.getId().equals(message.getId())) {
                m.update(message.getContent());
                break;
            }
        }
        saveFile(messages);
    }

    @Override
    public void delete(UUID id) {
        List<Message> messages = loadFile();

        Iterator<Message> iterator = messages.iterator();
        while (iterator.hasNext()) {
            Message message = iterator.next();
            if (message.getId().equals(id)) {
                iterator.remove();
                break;
            }
        }

        saveFile(messages);
    }
}
