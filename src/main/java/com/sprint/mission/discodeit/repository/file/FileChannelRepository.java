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
import java.util.stream.Stream;

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
        try (
                Stream<Path> paths = Files.list(DIRECTORY)
                ){
                    return paths.map(c -> {
                        try {
                            return (Channel) read(DIRECTORY.resolve(c));
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }})
                            .filter(fn)
                            .toList();
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
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
