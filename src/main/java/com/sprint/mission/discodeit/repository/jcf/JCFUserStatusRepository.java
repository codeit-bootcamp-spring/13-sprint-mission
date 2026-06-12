package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;


@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name="discodeit.repository.type",havingValue = "jcf", matchIfMissing = true)
public class JCFUserStatusRepository implements UserStatusRepository {
    private final Set<UserStatus> data=new HashSet<>();

    @Override
    public void save(UserStatus usr) {
        data.add(usr);
    }

    @Override
    public List<UserStatus> find(Predicate<UserStatus> fn) {
        return data.stream().filter(fn).toList();
    }

    @Override
    public List<UserStatus> findAll() {
        return find(us -> true);
    }

    @Override
    public UserStatus findByID(UUID id) {
        List<UserStatus> res = find(us -> us.getId().equals(id));
        return res.isEmpty() ? null : res.get(0);
    }

    @Override
    public UserStatus findByUserID(UUID userID) {
        List<UserStatus> res = find(us -> us.getUserID().equals(userID));
        return res.isEmpty() ? null : res.get(0);
    }

    @Override
    public void delete(UUID userID) {
        data.remove(findByID(userID));
    }

}
