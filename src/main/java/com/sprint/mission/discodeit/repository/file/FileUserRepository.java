package com.sprint.mission.discodeit.repository.file;



import com.sprint.mission.discodeit.config.DiscodeitConfig;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
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
public class FileUserRepository extends FileBaseRepository implements UserRepository {

    public final DiscodeitConfig dic;

    @Override
    public void save(User user){
        write(dic.getFilePath().resolve("user").resolve(user.getId()+ ".ser"), user);
    }

    @Override
    public List<User> find(Predicate<User> fn) throws RuntimeException {
        return read(fn,dic.getFilePath().resolve("user"));
    }

    @Override
    public List<User> findAll(){
        return this.find(c -> true);
    }

    @Override
    public Optional<User> findByID(UUID id){
        return find(u -> u.getId().equals(id)).stream().findFirst();
    }
    @Override
    public Optional<User> findByEmail(String email){
        return find(u -> u.getEmail().equals(email)).stream().findFirst();
    }

    @Override
    public Optional<User> findByName(String name){
        return find(u -> u.getName().equals(name)).stream().findFirst();
    }

    @Override
    public void delete(UUID id){
        delete(dic.getFilePath().resolve("user").resolve(id.toString() + ".ser"));
    }
}
