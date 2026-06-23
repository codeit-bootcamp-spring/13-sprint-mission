package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

//ReadstatusRepository의 JCF 기반 구현체
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
@Repository
public class JCFReadStatusRepository implements ReadStatusRepository {
    private final Map<UUID, ReadStatus> data; //메모리 저장소

    public JCFReadStatusRepository() {this.data = new HashMap<>();} //생성자. HsdMap기반 저장소 초기화

    @Override //Readstatus 저장
    public ReadStatus save(ReadStatus readStatus) {
        this.data.put(readStatus.getId(), readStatus);
        return readStatus;
    }
    @Override //ID로 Readstatus 조회
    public Optional<ReadStatus> findById(UUID id) {return Optional.ofNullable(this.data.get(id));}
    @Override //특정 사용자의 모든 Readstatus 조회
    public List<ReadStatus> findAllByUserId(UUID userId){
        return this.data.values().stream()
                .filter(readStatus -> readStatus.getUserId().equals(userId)).toList();
    }
    @Override //특정 채널의 모든 Readstatus 조회
    public List<ReadStatus> findAllByChannelId(UUID channelId){
        return this.data.values().stream()
                .filter(readStatus -> readStatus.getChannelId().equals(channelId)).toList();
    }
    @Override //특정 Readstatus 존재 여부확인
    public boolean existsById(UUID id) {
        return this.data.containsKey(id);
    }
    @Override //ID 기중 Readstatus 삭제
    public void deleteById(UUID id) {
        this.data.remove(id);
    }
    @Override //특정 채널에 속한 모든 Readstatus 삭제
    public void deleteAllByChannelId(UUID channelId) {
        this.findAllByChannelId(channelId)
                .forEach(readStatus -> this.deleteById(readStatus.getId()));
    }
}
