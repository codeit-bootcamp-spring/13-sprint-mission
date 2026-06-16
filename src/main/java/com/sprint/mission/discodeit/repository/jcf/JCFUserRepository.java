package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
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
    public List<User> findAll(){
        return this.find(c -> true);
    }

    @Override
    public Optional<User> findByID(UUID id){
        return Optional.ofNullable(data.get(id));
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
        data.remove(id);
    }

}
