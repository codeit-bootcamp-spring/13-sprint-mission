package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

//User 객체를 저장하고 조회하기 위한 Repository 인터페이스
public interface UserRepository {
    User save(User user); //사용자 저장
    Optional<User> findById(UUID id); //Id를 이용하여 사용자 조회
    List<User> findAll();
    boolean existsById(UUID id);
    void deleteById(UUID id);
}
