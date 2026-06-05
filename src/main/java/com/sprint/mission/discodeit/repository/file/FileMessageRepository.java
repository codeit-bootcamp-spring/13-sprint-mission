package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(
        value = "discodeit.repository.type",
        havingValue = "file"
)
public class FileMessageRepository extends FileRepositoryRoot<Message> implements MessageRepository {

    //ctor
    public FileMessageRepository(@Value("${discodeit.repository.file-directory}") String fileDirectory) {
        super(Path.of(fileDirectory).resolve("messages.ser"));
    }

    //interface
    @Override
    public void createMessage(Message message) {
        storage.add(message);

        saveToBinary();
    }

    @Override
    public Optional<Message> findMessageById(UUID id) {
        return storage.stream()
                .filter(message -> message.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Message> findAllMessagesByChannelId(UUID channelId) {
        return storage.stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .toList();
    }

    @Override
    public void save() {
        saveToBinary();
    }

    @Override
    public void deleteMessagesByChannelId(UUID channelId) {
        storage.removeAll(
                storage.stream()
                        .filter(message -> message.getChannelId().equals(channelId))
                        .toList()
        );

        saveToBinary();
    }

    @Override
    public void deleteMessageById(UUID id) {
        storage.remove(
                storage.stream()
                        .filter(message -> message.getId().equals(id))
                        .findFirst()
                        .get()
        );

        saveToBinary();
    }
}
