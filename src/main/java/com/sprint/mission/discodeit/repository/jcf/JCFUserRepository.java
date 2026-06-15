package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;


@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name="discodeit.repository.type",havingValue = "jcf", matchIfMissing = true)
public class JCFUserRepository implements UserRepository {
    private final HashMap<UUID, User> data = new HashMap<>();

    @Override
    public void save(User user) {
        data.put(user.getId(), user);
    }

    @Override
    public List<User> find(Predicate<User> fn){
         return data.values().stream()
                .filter(fn)
                .toList();
    }

    @Override
    public User findByID(UUID id){
        return data.get(id);
    }

    @Override
    public User findByEmail(String email){
        List<User> res = find(u -> u.getEmail().equals(email));
        return !res.isEmpty() ?  res.get(0) : null;
    }

    @Override
    public User findByName(String name){
        List<User> res = find(u -> u.getName().equals(name));
        return !res.isEmpty() ?  res.get(0) : null;
    }

    @Override
    public void delete(UUID id){
        data.remove(id);
    }

}
