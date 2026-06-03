package com.sprint.mission.discodeit.repository.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FileMessageRepository implements MessageRepository {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String filePath = "messages.json";

    @Override
    public void save(Message message) {
        List<Message> messages = findAll();
        messages.add(message);
        saveAll(messages);
    }

    @Override
    public List<Message> findAll() {
        File file = new File(filePath);
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
            objectMapper.writeValue(new File(filePath), messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override public Optional<Message> findById(String id) {
        return findAll().stream().filter(u -> u.getId().equals(id)).findFirst(); }
    @Override public void update(Message message) {}
    @Override public void delete(String id) {}
}