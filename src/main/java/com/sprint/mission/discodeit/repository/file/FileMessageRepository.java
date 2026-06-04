package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.MessageRepository;
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
public class FileMessageRepository extends FileBaseRepository implements MessageRepository {
    private final Path DIRECTORY = Paths.get(System.getProperty("user.dir"),"data","message");

    public FileMessageRepository() {
        super();
    }

    @Override
    public void save(Message msg) {
        try {
            write(DIRECTORY.resolve(msg.getId()+ ".ser"), msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Message> find(Predicate<Message> fn){
        try (
                Stream<Path> paths = Files.list(DIRECTORY)
        ){
            return paths.map(u -> {
                        try {
                            return (Message) read(DIRECTORY.resolve(u));
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
    public void delete(UUID id){
        try {
            Files.delete(DIRECTORY.resolve(id.toString() + ".ser"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
