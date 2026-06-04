package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class JCFUserStatusRepository implements UserStatusRepository {
    private final Set<UserStatus> data;

    private JCFUserStatusRepository(){
        data = new HashSet<>();
    }

    private static class JSR{
        private static final JCFUserStatusRepository INSTANCE = new JCFUserStatusRepository();
    }
    public static JCFUserStatusRepository getInstance(){
        return JSR.INSTANCE;
    }

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
        return find(us -> us.getId().equals(id)).get(0);
    }

    @Override
    public UserStatus findByUserID(UUID userID) {
        return find(us -> us.getUserID().equals(userID)).get(0);
    }

    @Override
    public void delete(UUID userID) {
        data.remove(findByID(userID));
    }

}
