package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.DiscodeitConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;


@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
@RequiredArgsConstructor
@Slf4j
public class FileChannelRepository extends FileBaseRepository implements ChannelRepository {
    private final DiscodeitConfig dic;

    @Override
    public void save(Channel cnl) throws RuntimeException {
        write(dic.getFilePath().resolve("channel").resolve(cnl.getId()+ ".ser"), cnl);
    }

    @Override
    public List<Channel> find (Predicate<Channel> fn) {
        return rawFind(fn,dic.getFilePath().resolve("channel"));
    }

    @Override
    public List<Channel> findAll() {
        return find(cnl -> true);
    }

    @Override
    public Channel findById(UUID id) {
        List<Channel> res = find(cnl -> cnl.getId().equals(id));
        return res.isEmpty() ? null : res.get(0);
    }

    @Override
    public void delete(UUID cnl) {
        delete(dic.getFilePath().resolve("channel").resolve(cnl.toString() + ".ser"));
    }
}
