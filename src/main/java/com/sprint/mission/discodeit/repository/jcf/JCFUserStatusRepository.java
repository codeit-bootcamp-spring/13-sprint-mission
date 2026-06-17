package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.stereotype.*;

import java.util.*;
@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", matchIfMissing = true, havingValue = "jcf")
public class JCFUserStatusRepository implements UserStatusRepository {

    private final Map<UUID, UserStatus> data;

    public JCFUserStatusRepository() {
        this.data = new HashMap<>();
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }

    @Override
    public UserStatus findByUserId(UUID id) {
        return data.values().stream()
                .filter(userStatus -> userStatus.getUserId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void create(UserStatus userStatus) {
        data.put(userStatus.getId(), userStatus);
    }

    @Override
    public UserStatus find(UUID id) {
        return data.get(id);
    }

    @Override
    public List<UserStatus> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void update(UserStatus userStatus) {
        data.put(userStatus.getId(), userStatus);
    }
}
