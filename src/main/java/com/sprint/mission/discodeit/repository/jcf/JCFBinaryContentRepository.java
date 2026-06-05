package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class JCFBinaryContentRepository implements BinaryContentRepository {
    private final Set<BinaryContent> data;

    private static class JBC{
        private static final JCFBinaryContentRepository INSTANCE = new JCFBinaryContentRepository();
    }

    private JCFBinaryContentRepository() {
        data = new HashSet<>();
    }

    public static JCFBinaryContentRepository getInstance() {
        return JBC.INSTANCE;
    }

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
    public BinaryContent findByID(UUID id){
        return find(bc -> bc.getId().equals(id)).get(0);
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
