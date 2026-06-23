package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;


import java.util.*;
import java.util.function.Predicate;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name="discodeit.repository.type",havingValue = "jcf", matchIfMissing = true)
public class JCFBinaryContentRepository implements BinaryContentRepository {
    private final HashMap<UUID, BinaryContent> data = new HashMap<>();

    @Override
    public void save(BinaryContent bc){
        data.put(bc.getId(),bc);
    }

    @Override
    public List<BinaryContent> find(Predicate<BinaryContent> fn){
        return data.values().stream()
                .filter(fn)
                .toList();
    }

    @Override
    public Optional<BinaryContent> findByID(UUID id){
        return find(bc -> bc.getId().equals(id)).stream().findFirst();
    }

    @Override
    public void delete(UUID id){
        data.remove(id);
    }

}
