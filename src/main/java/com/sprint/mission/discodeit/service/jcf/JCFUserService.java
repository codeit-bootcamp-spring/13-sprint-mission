package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
// [구현체] 기획서대로 실제로 일하는 주체
public class JCFUserService implements UserService {

    // [요구사항] data 필드를 final로 선언하세요!
    private final List<User> data; // 창고(data) 선언

    // [요구사항] 생성자에서 초기화하세요!
    public JCFUserService() {
        this.data = new ArrayList<User>(); // 창고 생성 (생성자)
    }

    public User create(User user) { // 매개변수 선언, 유저 생성하는 기능 구현
        data.add(user); // 창고에 넣기 (진짜 등록)
        return user;
    }

    public User findById(UUID id) { // 단건 조회
        for (User foundUser : data) {
            if (foundUser.getId().equals(id)) {
                return foundUser;
            }
        }
        return null;
    }

    public List<User> findAll() { // 전체 조회
        return data;
    }

    public void update(User requestUser) {
        User foundUser = findById(requestUser.getId());
        if (foundUser != null) {
            foundUser.updateName(requestUser);
        }
    }

    public void delete(UUID id) {
        User foundUser = findById(id);
        if (foundUser != null) {
            data.remove(foundUser);
        }
    }

}
