package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;



@Repository
@ConditionalOnProperty(name = "discodeit.repository.type",havingValue = "file")
@RequiredArgsConstructor
@Slf4j
public class FileMessageRepository extends FileBaseRepository implements MessageRepository {
    private final Path DIRECTORY = Paths.get(System.getProperty("user.dir"),"data","message");

//    public FileMessageRepository() {
//        super();
//    }

    @Override
    public void save(Message msg) {
        try {
            write(DIRECTORY.resolve(msg.getId()+ ".ser"), msg);
            log.debug("Message created - {}", msg.getId());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Message> find(Predicate<Message> fn){
        return rawFind(fn,DIRECTORY);
    }

    @Override
    public List<Message> findByChannelID(UUID channelID) {
        return find(m -> m.getChannelID().equals(channelID));
    }

    @Override
    public void delete(UUID id){
        try {
            Files.delete(DIRECTORY.resolve(id.toString() + ".ser"));
            log.debug("Message deleted - {}", id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
