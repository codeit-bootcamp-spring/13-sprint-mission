package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class FileMessageRepository extends FileRepositoryRoot<Message> implements MessageRepository {

    public FileMessageRepository() {
        super(Path.of("data/messages.ser"));
    }

    //interface
    @Override
    public void save() {
        saveToBinary();
    }

    @Override
    public void createMessage(Message message) {
        storage.add(message);
        saveToBinary();
    }

    @Override
    public Optional<Message> findMessage(Message message) {
        if (storage.contains(message)){
            return Optional.of(message);
        }
        return Optional.empty();
    }

    @Override
    public List<Message> findAll() {
        return storage;
    }

    @Override
    public void deleteMessage(Message message) {
        storage.remove(message);
        saveToBinary();
    }
}
