package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
@Repository
//UserRepository의 JCF 구현체
public class JCFUserRepository implements UserRepository {
    private final Map<UUID, User> data; //실제 user 객체가 저장되는 메모리 저장소
    public JCFUserRepository() {this.data = new HashMap<>();} //Repository 생성 시 HashMap 저장소를 초기화함.

    @Override //사용자 저장. 신규 생성 또는 수정 저장 모두 처리
    public User save(User user){
        this.data.put(user.getId(), user);
        return user; //저장된 객체 반환
    }

    @Override //사용자 단건 조회. ID로 User 조회
    public Optional<User> findById(UUID id){return Optional.ofNullable(this.data.get(id));}

    @Override //username으로 사용자 조회
    public Optional<User> findByUsername(String username) {
        return this.findAll().stream()
                .filter(user->user.getUsername().equals(username)).findFirst();
    }

    @Override //email으로 사용자 조회
    public Optional<User> findByEmail(String email) {
        return this.findAll().stream()
                .filter(user->user.getEmail().equals(email)).findFirst();
    }

    @Override //전체 사용자 조회. HashMap 내부 모든 User 반환
    public List<User> findAll() {
        return this.data.values().stream().toList();
    }

    @Override //사용자 존재 여부 확인. ID가 저장소에 존재하면 true, 존재하지 않으면 false
    public boolean existsById(UUID id){return this.data.containsKey(id);}

    @Override //사용자 삭제. 지정된 ID의 User 제거
    public void deleteById(UUID id) {this.data.remove(id);}

    @Override //username 존재 여부 확인
    public boolean existsByUsername(String username) {return this.findAll().stream().anyMatch(user->user.getUsername().equals(username));}

    @Override //email 존재 여주 확인
    public boolean existsByEmail(String email) {
        //return this.data.containsKey(email);}
        return this.findAll().stream()
                .anyMatch(user ->
                        user.getEmail().equals(email));
    }
}
