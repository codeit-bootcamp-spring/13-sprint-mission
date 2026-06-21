package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import lombok.NoArgsConstructor;

import java.util.*;

@NoArgsConstructor
public class JCFBinaryContentRepository implements BinaryContentRepository {

    private static final Map<UUID, BinaryContent> data=new HashMap<>(); // 인스턴스를 생성할 때 마다 새로운 Map을 만들지 않도록 한다

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
