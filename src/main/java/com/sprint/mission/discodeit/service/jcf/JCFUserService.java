package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;

public class JCFUserService implements UserService {

    private final Map<UUID, User> userMap = new LinkedHashMap<>();

    // userMap 에 put(키와 값을 추가)을 사용함
    @Override
    public User create(String username, String email, String password, UserStatus userStatus) {
        User user = new User(
                username,
                email,
                password,
                userStatus
        );
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
    public User update(UUID id,
                       String username,
                       String password,
                       UserStatus userStatus) {
        User user = userMap.get(id);
        user.updateUsername(username);
        user.updatePassword(password);
        user.updateUserStatus(userStatus);
        return user; // 수정된 유저 정보를 리턴
    }

    public User updateUsername(UUID id, String username) {
        User user = userMap.get(id);
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        user.updateUsername(username);
        return user;
    }

    public User updatePassword(UUID id, String password) {
        User user = userMap.get(id);
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        user.updatePassword(password);
        return user;
    }
    public User updateUserStatus(UUID id, UserStatus userStatus) {
        User user = userMap.get(id);
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        user.updateUserStatus(userStatus);
        return user;
    }


    @Override
    public void delete(UUID id) {
        userMap.remove(id); // 삭제
    }


}
