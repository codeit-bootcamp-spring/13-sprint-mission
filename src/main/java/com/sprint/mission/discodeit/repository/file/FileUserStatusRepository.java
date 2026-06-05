package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FileUserStatusRepository extends FileRepositoryRoot<UserStatus> implements UserStatusRepository {

    //ctor
    public FileUserStatusRepository() {
        super(Path.of("data/userStatuses.ser"));
    }

    //interface
    @Override
    public boolean existsUserStatusByUserId(UUID userId) {
        return storage.stream()
                .anyMatch(userStatus -> userStatus.getUserId().equals(userId));
    }

    @Override
    public void createUserStatus(UserStatus userStatus) {
        storage.add(userStatus);

        saveToBinary();
    }

    @Override
    public Optional<UserStatus> findUserStatusById(UUID userStatusId) {
        return storage.stream()
                .filter(userStatus -> userStatus.getId().equals(userStatusId))
                .findFirst();
    }

    @Override
    public Optional<UserStatus> findUserStatusByUserId(UUID userId) {
        return storage.stream()
                .filter(userStatus -> userStatus.getUserId().equals(userId))
                .findFirst();
    }

    @Override
    public List<UserStatus> findAllUserStatus() {
        return storage;
    }

    @Override
    public void save() {
        saveToBinary();
    }

    @Override
    public void deleteUserStatus(UUID userStatusId) {
        storage.remove(
                storage.stream()
                        .filter(userStatus -> userStatus.getId().equals(userStatusId))
                        .findFirst()
                        .get()

        );

        saveToBinary();
    }
}
