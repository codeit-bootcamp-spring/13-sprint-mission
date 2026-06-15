package com.sprint.mission.discodeit.repository.file;



import com.sprint.mission.discodeit.config.DiscodeitConfig;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
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
public class FileUserRepository extends FileBaseRepository implements UserRepository {

    public final DiscodeitConfig dic;

    @Override
    public void save(User user){
        write(dic.getFilePath().resolve("user").resolve(user.getId()+ ".ser"), user);
    }

    @Override
    public List<User> find(Predicate<User> fn) throws RuntimeException {
        return rawFind(fn,dic.getFilePath().resolve("user"));
    }

    @Override
    public User findByID(UUID id){
        List<User> res =  find(u -> u.getId().equals(id));
        return res.isEmpty() ? null : res.get(0);
    }
    @Override
    public User findByEmail(String email){
        List<User> res = find(u -> u.getEmail().equals(email));
        return res.isEmpty() ? null : res.get(0);
    }

    @Override
    public User findByName(String name){
        List<User> res =  find(u -> u.getName().equals(name));
        return res.isEmpty() ? null : res.get(0);
    }

    @Override
    public void delete(UUID id){
        delete(dic.getFilePath().resolve("user").resolve(id.toString() + ".ser"));
    }
}
