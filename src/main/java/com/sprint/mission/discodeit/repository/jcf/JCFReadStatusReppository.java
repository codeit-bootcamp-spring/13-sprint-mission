package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class JCFReadStatusReppository implements ReadStatusRepository {
    private final Set<ReadStatus> data;

    private JCFReadStatusReppository() {
        data = new HashSet<>();
    }

    private static class JRR{
        private static final JCFReadStatusReppository INSTANCE = new JCFReadStatusReppository();
    }

    public static JCFReadStatusReppository getInstance() {
        return JRR.INSTANCE;
    }

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
        return find(rs -> rs.getId().equals(id)).get(0);
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
