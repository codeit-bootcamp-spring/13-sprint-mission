package com.sprint.mission.discodeit.repository.file;


import com.sprint.mission.discodeit.DiscodeitConfig;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
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
@RequiredArgsConstructor
@ConditionalOnProperty(name = "discodeit.repository.type",havingValue = "file")
@Slf4j
public class FileBinaryContentRepository extends FileBaseRepository implements BinaryContentRepository {
    private final Path DIRECTORY = Paths.get(System.getProperty("user.dir"),"data","binarycontent");

    private final DiscodeitConfig dic;

    @Override
    public void save(BinaryContent bc) {
        try {
            write(dic.getFilePath().resolve("binarycontent").resolve(bc.getId()+ ".ser"), bc);
            log.debug("BinaryContent created - {}",bc.getId());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<BinaryContent> find(Predicate<BinaryContent> fn){
        return rawFind(fn,dic.getFilePath().resolve("binarycontent"));
    }

    @Override
    public BinaryContent findByID(UUID id) {
        return find(bc -> bc.getId().equals(id)).get(0);
    }

    @Override
    public List<BinaryContent> findByAuthorID(UUID userID) {
        return find(bc -> bc.getAuthorID().equals(userID));
    }

    @Override
    public void delete(UUID id) {
        try {
            Files.delete(dic.getFilePath().resolve("binarycontent").resolve(id.toString() + ".ser"));
            log.debug("BinaryContent deleted - {}",id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
