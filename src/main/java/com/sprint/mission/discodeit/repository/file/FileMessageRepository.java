package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;



import java.io.*;
import java.util.*;

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


    public FileMessageRepository() {
        this.messageFileRepo = new File("data/repository-message.ser");
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
        return messagesRepo.values();
    }

    @Override
    public void delete(UUID id) {
        messagesRepo.remove(id);
        saveMessagesRepo();
    }
}
