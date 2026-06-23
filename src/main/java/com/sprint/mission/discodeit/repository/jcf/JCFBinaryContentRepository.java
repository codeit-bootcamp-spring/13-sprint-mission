package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

//BinaryContentRepository의 JCF 기반 구현체
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
@Repository
public class JCFBinaryContentRepository implements BinaryContentRepository {
    private final Map<UUID, BinaryContent> data; //메모리 저장소

    public JCFBinaryContentRepository() {this.data = new HashMap<>();} //생성자. HasMap 초기화

    @Override //BinaryContent 저장
    public BinaryContent save(BinaryContent binaryContent) {
        this.data.put(binaryContent.getId(), binaryContent);
        return binaryContent;
    }
    @Override //ID로 BinaryContent 조회
    public Optional<BinaryContent> findById(UUID id) {
        return Optional.ofNullable(this.data.get(id));
    }

    @Override //여러 ID에 해당하는 BinaryContent 조회
    public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
        return this.data.values().stream()
                .filter(content -> ids.contains(content.getId()))
                .toList();
    }

    @Override //BinaryContent 존재 여부 확인
    public boolean existsById(UUID id) {
        return this.data.containsKey(id);
    }

    @Override //BinaryContent 삭제
    public void deleteById(UUID id) {
        this.data.remove(id);
    }
}