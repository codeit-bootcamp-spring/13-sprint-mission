package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;

import java.util.*;

public class JCFBinaryContentRepository implements BinaryContentRepository {

    private final Map<UUID, BinaryContent> data;
    public JCFBinaryContentRepository(){
        this.data=new HashMap<>();
    }

    @Override
    public BinaryContent save(BinaryContent content) {
        data.put(content.getId(), content);
        return content;
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
        Set<UUID> idsSet=new HashSet<>(ids); // set으로 변환해 탐색시간 개선
        return data.values().stream()
                .filter(content -> idsSet.contains(content.getId()))
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        data.remove(id);
    }

    @Override
    public boolean existById(UUID id) {
        return data.containsKey(id);
    }

}
