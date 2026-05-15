package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;

public class JCFUserService implements UserService {

    private final Map<UUID, User> userMap = new HashMap<>();

    // userMap 에 put(키와 값을 추가)을 사용함
    @Override
    public User create(User user) {
        userMap.put(user.getId(), user);
        return user;
    }

    // UUID id에 유저의 모든 정보가 들어있으니 id 만 넣으면 됨!
    @Override
    public User read(UUID id) {
        return userMap.get(id);
    }

    // 저장된 유저 정보값을 모두 읽기 위해 List 사용, 정보값을 담을 new ArrayList 만듬
    // value() -> map 안의 모든 값을 리턴할 때 사용
    @Override
    public List<User> readAll() {
        return new ArrayList<>(userMap.values());
    }

    @Override
    public User update(UUID id, User user) {
        userMap.get(id).updateUsername(user.getUsername());
        return userMap.get(id); // 수정된 유저 정보를 리턴
    }

    @Override
    public void delete(UUID id) {
        userMap.remove(id); // 삭제
    }


}
