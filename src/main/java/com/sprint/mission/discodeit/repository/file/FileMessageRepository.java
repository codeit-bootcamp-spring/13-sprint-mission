package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class FileMessageRepository implements MessageRepository {
    private final String filePath = "messages.dat";
    private List<Message> messages = new ArrayList<>();

    public FileMessageRepository() {
        load();
    }

    @Override
    public Message save(Message message) {
        messages.add(message);
        saveToFile();
        return message;
    }

    @Override
    public Message findByld(UUID id){
        for (Message message : messages) {
            if (message.getId().equals(id))
                return message;
        }
        return null;
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))){
            oos.writeObject(messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("uncheked")
    private void load() {
        File file = new File(filePath);
        if (!file.exists()) {
            messages = new ArrayList<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            messages = (List<Message>) ois.readObject();
        } catch (Exception e) {
            messages = new ArrayList<>();
        }
    }

}