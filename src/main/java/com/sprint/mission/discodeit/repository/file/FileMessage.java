package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;


import java.io.*;
import java.util.*;

public class FileMessage implements MessageRepository {

    private final String FILE_PATH = "messages.dat";
    private Map<UUID, Message> data;

    public FileMessage() {

        load();
    }

    private void load() {

        File file = new File(FILE_PATH);
        if (!file.exists()) {

            data = new HashMap<>();
            return;
        }

        try (
                ObjectInputStream ois =
                        new ObjectInputStream(
                                new FileInputStream(file)
                        )
        ) {
            data = (Map<UUID, Message>) ois.readObject();

        } catch (Exception e) {

            data = new HashMap<>();
        }
    }

    private void save() {

        try (
                ObjectOutputStream oos =
                        new ObjectOutputStream(
                                new FileOutputStream(FILE_PATH)
                        )
        ) {
            oos.writeObject(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void create(Message message) {

        data.put(message.getId(), message);
        save();
    }

    @Override
    public Message read(UUID id) {

        return data.get(id);
    }

    @Override
    public List<Message> readAll() {

        return new ArrayList<>(data.values());
    }

    @Override
    public void delete(UUID id) {

        data.remove(id);
        save();
    }
}
