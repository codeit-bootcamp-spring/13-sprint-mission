package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
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
public class FileReadStatusRepository extends FileBaseRepository implements ReadStatusRepository {
    private final Path DIRECTORY = Paths.get(System.getProperty("user.dir"),"data","readstatus");

    @Override
    public void save(ReadStatus rs) {
        try {
            write(DIRECTORY.resolve(rs.getId()+ ".ser"), rs);
            log.debug("ReadStatus create - {}", rs.getId());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ReadStatus> find(Predicate<ReadStatus> fn) {
        return rawFind(fn,DIRECTORY);
    }

    @Override
    public ReadStatus findByID(UUID id) {
        return find(rs -> rs.getId().equals(id)).get(0);
    }

    @Override
    public List<ReadStatus> findbyChennalID(UUID id){
        return find(rs -> rs.getChannelID().equals(id));
    }

    @Override
    public void delete(UUID id) {
        try {
            Files.delete(DIRECTORY.resolve(id.toString() + ".ser"));
            log.debug("ReadStatus delete - {}", id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
