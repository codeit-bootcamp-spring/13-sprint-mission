package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    public void save(Channel cnl) throws RuntimeException {
        try {
            write(DIRECTORY.resolve(cnl.getId()+ ".ser"), cnl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Channel> find (Predicate<Channel> fn) {
        return rawFind(fn,DIRECTORY);
    }

    @Override
    public List<Channel> findAll() {
        return find(cnl -> true);
    }

    @Override
    public Channel findById(UUID id) {
        return find(cnl -> cnl.getId().equals(id)).get(0);
    }

    @Override
    public void delete(UUID cnl) {
        try {
            Files.delete(DIRECTORY.resolve(cnl.toString() + ".ser"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
