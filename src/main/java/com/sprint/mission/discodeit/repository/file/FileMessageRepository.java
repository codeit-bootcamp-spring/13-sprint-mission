package com.sprint.mission.discodeit.repository.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Repository
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

    @Override
    public Optional<Message> findById(String id) {
        return findAll().stream()
                .filter(m -> id.equals(m.getId()))
                .findFirst();
    }

    @Override
    public void update(Message message) {
        List<Message> messages = findAll();
        List<Message> updatedMessages = messages.stream()
                .map(m -> message.getId().equals(m.getId()) ? message : m)
                .toList();
        saveAll(updatedMessages);
    }



    @Override
    public void delete(String id) {
        List<Message> messages = findAll();
        boolean removed = messages.removeIf(message -> id.equals(message.getId()));

        if (removed) {
            saveAll(messages);
        }
    }
}