package com.sprint.mission.discodeit.repository.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Repository
public class FileMessageRepository implements MessageRepository {
    private final ObjectMapper objectMapper = new ObjectMapper();
    static final String FilePath = "messages.json";

    @Override
    public void save(Message message) {
        List<Message> messages = findAll();
        messages.add(message);
        saveAll(messages);
    }

    @Override
    public List<Message> findAll() {
        File file = new File(FilePath);
        if (!file.exists()) return new ArrayList<>();
        try {
            return objectMapper.readValue(file,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Message.class));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void saveAll(List<Message> messages) {
        try {
            objectMapper.writeValue(new File(FilePath), messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<Message> findById(String id) {
        return findAll().stream().filter(m -> m.getId().equals(id)).findFirst(); }

    @Override
    public void update(Message message) {
        List<Message> messages = findAll();
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getId().equals(message.getId())) {
                messages.set(i, message);
                break;
            }
        }
        saveAll(messages);
    }
    @Override
    public void delete(String id) {
        List<Message> messages = findAll();
        boolean removed = messages.removeIf(message -> message.getId().equals(id));

        if (removed) {
            saveAll(messages);
        }
    }
}