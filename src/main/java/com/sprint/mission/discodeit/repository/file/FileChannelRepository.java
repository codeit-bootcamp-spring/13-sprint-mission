package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.nio.file.Path;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Predicate;

public class FileChannelRepository extends FileBaseRepository implements ChannelRepository {
    private final HashMap<UUID, Channel> data = new HashMap<>();
    private final Path path;

    private FileChannelRepository(Path path) {
        super();
        this.path = path;
        HashMap<UUID,Channel>  fData = load(path);
        for (UUID k : fData.keySet()){
            this.data.put(k, fData.get(k));
        }
    }

    @Override
    public void create(String name, String description, ChannelType type) {
        Channel channel = new Channel(name, description, type);
        for (int i = 0; i < 3; i++){
            if (data.containsKey(channel.getId()))
                channel = new Channel(name, description, type);
        }
        data.put(channel.getId(),channel);
    }

    @Override
    public ArrayList<Channel> select (Predicate<Channel> fn) {
        return new ArrayList<>(data.values().stream()
                .filter(fn)
                .toList());
    }

    @Override
    public void update(UUID pcnl, String name,String description,ChannelType type) {
        Channel cnl = data.get(pcnl);
        cnl.setName(name);
        cnl.setDescription(description);
        cnl.setType(type);
        cnl.setUpdatedAt(System.currentTimeMillis());
    }

    @Override
    public void delete(UUID cnl) {
        data.remove(cnl);
    }

    public static FileChannelRepository open(Path path) {
        return new FileChannelRepository(path);
    }

    public void close(){
        save(data,path);
    }
}
