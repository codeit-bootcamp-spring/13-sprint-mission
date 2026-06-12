package com.sprint.mission.discodeit.repository.file;


import com.sprint.mission.discodeit.DiscodeitConfig;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "discodeit.repository.type",havingValue = "file")
@Slf4j
public class FileBinaryContentRepository extends FileBaseRepository implements BinaryContentRepository {
    private final DiscodeitConfig dic;

    @Override
    public void save(BinaryContent bc) {
            write(dic.getFilePath()
                    .resolve("binarycontent")
                    .resolve(bc.getId()+ ".ser"),
                    bc);
    }

    @Override
    public List<BinaryContent> find(Predicate<BinaryContent> fn){
        return rawFind(fn,dic.getFilePath().resolve("binarycontent"));
    }

    @Override
    public BinaryContent findByID(UUID id) {
        List<BinaryContent> res = find(bc -> bc.getId().equals(id));
        return res.isEmpty() ? null : res.get(0);
    }

    @Override
    public List<BinaryContent> findByAuthorID(UUID userID) {
        return find(bc -> bc.getAuthorID().equals(userID));
    }

    @Override
    public void delete(UUID id) {
        delete(dic.getFilePath().resolve("binarycontent").resolve(id.toString() + ".ser"));
    }

}
