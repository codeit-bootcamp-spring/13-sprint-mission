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
    private final Set<BinaryContent> data = new HashSet<>();

    @Override
    public void save(BinaryContent bc){
        data.add(bc);
    }

    @Override
    public List<BinaryContent> find(Predicate<BinaryContent> fn){
        return data.stream()
                .filter(fn)
                .toList();
    }

    @Override
    public Optional<BinaryContent> findByID(UUID id){
        return find(bc -> bc.getId().equals(id)).stream().findFirst();
    }

    @Override
    public List<BinaryContent> findByAuthorID(UUID userID){
        return find(bc -> bc.getAuthorID().equals(userID));
    }

    @Override
    public void delete(UUID id){
        data.remove(findByID(id));
    }

}
