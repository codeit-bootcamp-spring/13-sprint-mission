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
    public void delete(UUID id){
        data.remove(id);
    }

}
