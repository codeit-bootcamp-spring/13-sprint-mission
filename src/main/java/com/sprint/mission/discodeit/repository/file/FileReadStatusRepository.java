package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.config.DiscodeitConfig;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
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
public class FileReadStatusRepository extends FileBaseRepository implements ReadStatusRepository {
    private final DiscodeitConfig dic;

    @Override
    public void save(ReadStatus rs) {
        write(dic.getFilePath().resolve("readstatus").resolve(rs.getId()+ ".ser"), rs);
    }

    @Override
    public List<ReadStatus> find(Predicate<ReadStatus> fn) {
        return read(fn,dic.getFilePath().resolve("readstatus"));
    }

    @Override
    public Optional<ReadStatus> findByID(UUID id) {
        return find(rs -> rs.getId().equals(id)).stream().findFirst();
    }

    @Override
    public List<ReadStatus> findbyChennalID(UUID id){
        return find(rs -> rs.getChannelID().equals(id));
    }

    @Override
    public void delete(UUID id) {
        delete(dic.getFilePath().resolve("readstatus").resolve(id.toString() + ".ser"));
    }
}
