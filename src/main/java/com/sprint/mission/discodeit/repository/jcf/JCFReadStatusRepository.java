package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.function.Predicate;



@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name="discodeit.repository.type",havingValue = "jcf", matchIfMissing = true)
public class JCFReadStatusRepository implements ReadStatusRepository {
    private final HashMap<UUID, ReadStatus> data = new HashMap<>();

    @Override
    public void save(ReadStatus readStatus){
        data.put(readStatus.getId(), readStatus);
    }

    @Override
    public List<ReadStatus> find(Predicate<ReadStatus> fn){
        return data.values().stream()
                .filter(fn)
                .toList();
    }

    @Override
    public Optional<ReadStatus> findByID(UUID id){
        return find(rs -> rs.getId().equals(id)).stream().findFirst();
    }

    @Override
    public List<ReadStatus> findByChennalID(UUID id){
        return find(rs -> rs.getChannelId().equals(id));
    }

    @Override
    public List<ReadStatus> findByUserId(UUID id){
        return find(rs -> rs.getUserId().equals(id));
    }

    @Override
    public void delete(UUID id){
        data.remove(id);
    }


}
