package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.io.*;
import java.util.*;

public class FileChannel implements ChannelRepository {

    private final String FILE_PATH = "channels.dat";
    private Map<UUID, Channel> data;

    public FileChannel() {

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
            data = (Map<UUID, Channel>) ois.readObject();

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
    public void create(Channel channel) {

        data.put(channel.getId(), channel);
        save();
    }

    @Override
    public Channel read(UUID id) {
        return data.get(id);
    }

    @Override
    public List<Channel> readAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void delete(UUID id) {

        data.remove(id);
        save();
    }
}
