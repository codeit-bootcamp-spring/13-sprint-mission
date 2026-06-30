package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.config.DiscodeitConfig;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type",havingValue = "file")
@RequiredArgsConstructor
@Slf4j
public class FileUserStatusRepository extends FileBaseRepository implements UserStatusRepository {
    private final DiscodeitConfig dic;

    @Override
    public void save(UserStatus ust) {
        write(dic.getFilePath().resolve("userstatus").resolve(ust.getId()+ ".ser"), ust);
    }

    @Override
    public List<UserStatus> find(Predicate<UserStatus> fn) {
        return read(fn,dic.getFilePath().resolve("userstatus"));
    }

    @Override
    public List<UserStatus> findAll() {
        return find(us -> true);
    }

    @Override
    public Optional<UserStatus> findByID(UUID id) {
        return find(us -> us.getId().equals(id)).stream().findFirst();
    }

    @Override
    public Optional<UserStatus> findByUserID(UUID userID) {
        return find(us -> us.getUserId().equals(userID)).stream().findFirst();
    }

    @Override
    public void delete(UUID userID) {
        delete(dic.getFilePath().resolve("userstatus").resolve(userID.toString() + ".ser"));
    }

}
