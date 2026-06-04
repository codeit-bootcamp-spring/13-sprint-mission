package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class JCFUserRepository implements UserRepository {
    private final HashMap<UUID, User> data;

    private static class JUR{
        private static final JCFUserRepository INSTANCE = new JCFUserRepository();
    }

    private JCFUserRepository() { data = new HashMap<>();}


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
    public void update(UUID id, String name, String pw){
        User user = data.get(id);
        user.setName(name);
        user.setPassword(pw);
        user.setUpdatedAt();
    }

    @Override
    public void delete(UUID id){
        data.remove(id);
    }

}
