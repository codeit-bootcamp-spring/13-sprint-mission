package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Repository
@RequiredArgsConstructor
public class FileUserStatusRepository extends FileBaseRepository implements UserStatusRepository {
    private final Path DIRECTORY = Paths.get(System.getProperty("user.dir"),"data","userstatus");

    @Override
    public void save(UserStatus ust) {
        try {
            write(DIRECTORY.resolve(ust.getId()+ ".ser"), ust);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UserStatus> find(Predicate<UserStatus> fn) {
        return rawFind(fn,DIRECTORY);
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
            Files.delete(DIRECTORY.resolve(userID.toString() + ".ser"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
