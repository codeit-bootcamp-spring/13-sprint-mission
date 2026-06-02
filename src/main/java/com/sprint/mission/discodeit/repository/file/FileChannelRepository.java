package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Repository
public class FileChannelRepository extends FileBaseRepository implements ChannelRepository {
    private final Path DIRECTORY = Paths.get(System.getProperty("user.dir"),"data","channel");

    public FileChannelRepository() {
        super();
    }

    @Override
    public void create(String name, String description, ChannelType type) {
        Channel channel = new Channel(name, description, type);
        this.save(DIRECTORY.resolve(channel.getId()+ ".ser"), channel);
    }

    @Override
    public ArrayList<Channel> select (Predicate<Channel> fn) {
        try {
            List<Channel> d = Files.list(DIRECTORY)
                    .map(c -> (Channel) load(DIRECTORY.resolve(c)))
                    .toList();
            return new ArrayList<>(d.stream()
                            .filter(fn)
                            .toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public void update(UUID pcnl, String name,String description,ChannelType type) {
        try {
            Files.list(DIRECTORY)
                    .filter(path -> path.equals(DIRECTORY.resolve(pcnl.toString() + ".ser")))
                    .map(c -> {
                        Channel cnl= load(DIRECTORY.resolve(c));
                        cnl.setName(name);
                        cnl.setDescription(description);
                        cnl.setType(type);
                        cnl.setUpdatedAt(Instant.now());
                        save(DIRECTORY.resolve(cnl.getId().toString() + ".ser"),cnl);
                        return null;
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(UUID cnl) {
        try {
            Files.delete(DIRECTORY.resolve(cnl.toString() + ".ser"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
