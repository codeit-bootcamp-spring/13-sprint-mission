package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStatusRepository extends JpaRepository<UserStatus, UUID> {

//    boolean existsUserStatusByUserId(UUID userId);
//    void createUserStatus(UserStatus userStatus);
//    Optional<UserStatus> findUserStatusById(UUID userStatusId);
//    Optional<UserStatus> findUserStatusByUserId(UUID userId);
//    List<UserStatus> findAllUserStatus();
//    void save();
//    void deleteUserStatus(UUID userStatusId);

    boolean existsByUserId(UUID userId);
    Optional<UserStatus> findByUserId(UUID userId);

}
