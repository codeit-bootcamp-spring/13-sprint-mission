package com.sprint.mission.discodeit.repository;


import com.sprint.mission.discodeit.entity.User;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 저장로직 관련 기능 인터페이스 선언
public interface UserRepository {

    User saveUser(User user) throws IOException;

    Optional<User> findUser(UUID id) throws IOException;

    List<User> findUsers() throws IOException;

    void deleteUser(UUID id) throws IOException;


}
