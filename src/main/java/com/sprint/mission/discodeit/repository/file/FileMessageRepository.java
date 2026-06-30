package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.config.DiscodeitConfig;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;



@Repository
@ConditionalOnProperty(name = "discodeit.repository.type",havingValue = "file")
@RequiredArgsConstructor
@Slf4j
public class FileMessageRepository extends FileBaseRepository implements MessageRepository {
    private final DiscodeitConfig dic;

    @Override
    public void save(Message msg) {
        write(dic.getFilePath().resolve("message").resolve(msg.getId()+ ".ser"), msg);
    }

    @Override
    public List<Message> find(Predicate<Message> fn){
        return read(fn,dic.getFilePath().resolve("message"));
    }

    @Override
    public Optional<Message> findById(UUID id){
        return find(m -> m.getId().equals(id)).stream().findFirst();
    }

    @Override
    public List<Message> findByChannelID(UUID channelID) {
        return find(m -> m.getChannelId().equals(channelID));
    }

    @Override
    public void delete(UUID id){
        delete(dic.getFilePath().resolve("message").resolve(id.toString() + ".ser"));
    }
}
