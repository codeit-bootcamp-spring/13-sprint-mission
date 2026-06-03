package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.util.*;

public class JCFUserRepository implements UserRepository {

    private final Map<UUID, User> data=new HashMap<>();

    @Override
    public User createOne(User user) {
        data.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> readOne(UUID id) {
        return Optional.ofNullable(data.get(id)); // null 반환 또한 저장소의 특성이니 추가해야 함
    }

    @Override
    public List<User> readAll() {
        return new ArrayList<>(data.values());
    }


    @Override
    public void deleteOne(UUID id) {
        data.remove(id);
    }


}
