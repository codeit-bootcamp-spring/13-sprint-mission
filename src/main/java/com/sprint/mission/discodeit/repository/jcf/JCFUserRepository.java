package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//java collection Framework(ArrayList)를 이용한 User 저장소 구현체
public class JCFUserRepository implements UserRepository {
    //사용자 데이터를 메모리에 저장하는 리스트
    private final List<User> users = new ArrayList<>();

    //사용자 저장
    public User save(User user){
        users.add(user);
        return user;
    }

    //id로 사용자 조회
    public User findById(UUID id){
        //모든 사용자르 순회하며 id 비교
        for (User user : users) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users);
    }

    @Override
    public void delete(UUID id) {

        users.removeIf(
                user -> user.getId().equals(id)
        );
    }


}
