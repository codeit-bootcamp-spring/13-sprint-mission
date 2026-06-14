package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;


import java.io.*;
import java.util.*;

@Repository
public class FileMessageRepository implements MessageRepository {

    private final File messageFileRepo;
    private final Map<UUID, Message> messagesRepo;

    private Map<UUID, Message> loadMessagesRepo()  {
        if (!messageFileRepo.exists()) {
            return new HashMap<UUID, Message>();
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(messageFileRepo))) {
            return (Map<UUID, Message>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new HashMap<UUID, Message>();
        }
    }

    private void saveMessagesRepo() {
        File parent = messageFileRepo.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(messageFileRepo))) {
            out.writeObject(messagesRepo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public FileMessageRepository(
            @Value("${discodeit.repository.file-directory}") String fileDirectory
    ) {
        this.messageFileRepo = new File(fileDirectory, "repository-message.json");
        this.messagesRepo = loadMessagesRepo();
}

    @Override
    public void save(Message message) {
        messagesRepo.put(message.getId(), message);
        saveMessagesRepo();
    }

    @Override
    public Message findById(UUID id) {
        return messagesRepo.get(id);
    }

    @Override
    public Collection<Message> findAll() {
        return new ArrayList<>(messagesRepo.values());
    }

    @Override
    public void delete(UUID id) {
        messagesRepo.remove(id);
        saveMessagesRepo();
    }

    @Override
    public Collection<Message> findAllByChannelId(UUID channelId) {
        List<Message> result = new ArrayList<>();

        for (Message message : messagesRepo.values()) {
            if (message.getChannel().getId().equals(channelId)) {
                result.add(message);
            }
        }

        return result;
    }
}
