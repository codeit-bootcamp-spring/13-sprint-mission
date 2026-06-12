package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.DiscodeitConfig;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
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
        return rawFind(fn,dic.getFilePath().resolve("userstatus"));
    }

    @Override
    public List<UserStatus> findAll() {
        return find(us -> true);
    }

    @Override
    public UserStatus findByID(UUID id) {
        List<UserStatus> res = find(us -> us.getId().equals(id));
        return res.isEmpty() ? null : res.get(0);
    }

    @Override
    public UserStatus findByUserID(UUID userID) {
        List<UserStatus> res = find(us -> us.getUserID().equals(userID));
        return res.isEmpty() ? null : res.get(0);
    }

    @Override
    public void delete(UUID userID) {
        delete(dic.getFilePath().resolve("userstatus").resolve(userID.toString() + ".ser"));
    }

}
