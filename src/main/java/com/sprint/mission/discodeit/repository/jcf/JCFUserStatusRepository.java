package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@NoArgsConstructor
public class JCFUserStatusRepository implements UserStatusRepository {

    //필드
    private final List<UserStatus> userStatuses = new ArrayList<>();

    //interface
    @Override
    public boolean existsUserStatusByUserId(UUID userId) {
        return userStatuses.stream()
                .anyMatch(userStatus -> userStatus.getUserId().equals(userId));
    }

    @Override
    public void createUserStatus(UserStatus userStatus) {
        userStatuses.add(userStatus);
    }

    @Override
    public Optional<UserStatus> findUserStatusById(UUID userStatusId) {
        return userStatuses.stream()
                .filter(userStatus -> userStatus.getId().equals(userStatusId))
                .findFirst();
    }

    @Override
    public Optional<UserStatus> findUserStatusByUserId(UUID userId) {
        return userStatuses.stream()
                .filter(userStatus -> userStatus.getUserId().equals(userId))
                .findFirst();
    }

    @Override
    public List<UserStatus> findAllUserStatus() {
        return userStatuses;
    }

    @Override
    public void save() {

    }

    @Override
    public void deleteUserStatus(UUID userStatusId) {
        userStatuses.remove(
                userStatuses.stream()
                        .filter(userStatus -> userStatus.getId().equals(userStatusId))
                        .findFirst()
                        .get()

        );
    }
}
