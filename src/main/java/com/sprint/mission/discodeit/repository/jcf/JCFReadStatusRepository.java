package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;



@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name="discodeit.repository.type",havingValue = "jcf", matchIfMissing = true)
public class JCFReadStatusRepository implements ReadStatusRepository {
    private final Set<ReadStatus> data = new HashSet<>();

    @Override
    public void save(ReadStatus readStatus){
        data.add(readStatus);
    }

    @Override
    public List<ReadStatus> find(Predicate<ReadStatus> fn){
        return data.stream()
                .filter(fn)
                .toList();
    }

    @Override
    public ReadStatus findByID(UUID id){
        List<ReadStatus> res = find(rs -> rs.getId().equals(id));
        return res.isEmpty() ? null : res.get(0);
    }

    @Override
    public List<ReadStatus> findbyChennalID(UUID id){
        return find(rs -> rs.getChannelID().equals(id));
    }

    @Override
    public void delete(UUID id){
        data.remove(findByID(id));
    }


}
