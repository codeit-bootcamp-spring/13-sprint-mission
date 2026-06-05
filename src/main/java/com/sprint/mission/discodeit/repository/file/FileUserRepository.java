package com.sprint.mission.discodeit.repository.file;



import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;


@Repository
@Slf4j
public class FileUserRepository extends FileBaseRepository implements UserRepository {
    private final Path DIRECTORY = Paths.get(System.getProperty("user.dir"),"data","user");

    public FileUserRepository() {
        super();
    }

    @Override
    public void save(User user){
        try {
            write(DIRECTORY.resolve(user.getId()+ ".ser"), user);
            log.debug("User create - " + user.getId());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<User> find(Predicate<User> fn) throws RuntimeException {
        return rawFind(fn,DIRECTORY);
    }

    // Todo - handle outOfindex exception
    @Override
    public User findByID(UUID id){
        try{
            return find(u -> u.getId().equals(id)).get(0);
        } catch (IndexOutOfBoundsException e){
            return null;
        }

    }
    @Override
    public User findByEmail(String email){
        try {
            return find(u -> u.getEmail().equals(email)).get(0);
        } catch (IndexOutOfBoundsException e){
            return null;
        }
    }

    @Override
    public void delete(UUID id){
        try {
            Files.delete(DIRECTORY.resolve(id.toString() + ".ser"));
            log.debug("User deleted - " + id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
