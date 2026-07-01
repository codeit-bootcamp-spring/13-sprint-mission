package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

//UserStatusRepository의 JCF(Java Collection Framework) 구현체
//실제 DB를 사용하지 않고 HasMap을 이용하여 메모리 상으로 UserStatus 데이터를 관리함.
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
@Repository
public class JCFUserStatusRepository implements UserStatusRepository {
    private final Map<UUID, UserStatus> data; //userStatus 저장소

    public JCFUserStatusRepository() {this.data = new HashMap<>();} //생성자. HasMap을 생성하여 메모리 저장소를 초기화함.

    @Override //UserStatus 저장
    public UserStatus save(UserStatus userStatus){
        this.data.put(userStatus.getId(), userStatus);
        return userStatus;
    }
    @Override //ID로 UserStatus 조회
    public Optional<UserStatus> findById(UUID id) {return Optional.ofNullable(this.data.get(id));}
    @Override //사용자 ID로 UserStatus 조회
    public Optional<UserStatus> findByUserId(UUID userId) {
        return this.findAll().stream()
                .filter(userStatus -> userStatus.getUserId().equals(userId)).findFirst();
    }
    @Override //전체 UserStatus조회
    public List<UserStatus> findAll() {return this.data.values().stream().toList();}
    @Override //특정 ID의 UserStatus 존재 여부 확인
    public boolean existsById(UUID id) {return this.data.containsKey(id);}
    @Override //ID 기준 삭제
    public void deleteById(UUID id) {this.data.remove(id);}
    @Override //사용자 ID 기준 삭제
    public void deleteByUserId(UUID userId) {
        this.findByUserId(userId)
            .ifPresent(userStatus -> this.deleteByUserId(userStatus.getId()));}
}
