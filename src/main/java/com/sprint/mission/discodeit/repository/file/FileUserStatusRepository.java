package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.DiscodeitConfig;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type",havingValue = "file")
@RequiredArgsConstructor
@Slf4j
public class FileUserStatusRepository extends FileBaseRepository implements UserStatusRepository {
    private final Path DIRECTORY = Paths.get(System.getProperty("user.dir"),"data","userstatus");

    private final DiscodeitConfig dic;

    @Override
    public void save(UserStatus ust) {
        try {
            write(dic.getFilePath().resolve("userstatus").resolve(ust.getId()+ ".ser"), ust);
            log.debug("UserStatus create - by UserID : {}" ,ust.getUserID());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        return find(us -> us.getId().equals(id)).get(0);
    }

    @Override
    public UserStatus findByUserID(UUID userID) {
        return find(us -> us.getUserID().equals(userID)).get(0);
    }

    @Override
    public void delete(UUID userID) {
        try {
            Files.delete(dic.getFilePath().resolve("userstatus").resolve(userID.toString() + ".ser"));
            log.debug("UserStatus delete - by UserID : {} ", userID);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
