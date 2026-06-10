package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;
import java.util.*;

//UserService를 실제로 동작시키는 JCF(컬렉션) 기방 구현체
public class JCFUserService implements UserService {

    private final Map<UUID, User> data; //사용자의 데이터를 저장하는 map

    public JCFUserService() {this.data = new HashMap<>();} //서비스 객체 생성 시 사용자 저장소(HashMapa)를 초기화함

    @Override //사용자 생성
    public User create(String username, String email, String password) {
        User user = new User(username, email, password); //새로운 User 객체 생성
        this.data.put(user.getId(), user); //UUID를 key로 사용하여 저장
        return user; //생성된 사용자 반환
    }

    @Override //사용자 단건조회
    public User find(UUID UserId) {
        User UserNullable = this.data.get(UserId); //map에서 사용자 조회

        return Optional.ofNullable(UserNullable)
                .orElseThrow(() -> new NoSuchElementException("User with id " +  UserId + " not found"));
    }

    @Override //전체 사용자 조회
    public List<User> findAll() {
        return this.data.values().stream().toList();
    }

    @Override //사용자 수정
    public User update(UUID userId, String newUsername, String newEmail, String newPassword) {
        User userNullable = this.data.get(userId);
        User user = Optional.ofNullable(userNullable)
                .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));
        user.update(newUsername,newEmail,newPassword);

        return user;
    }

    @Override //사용자 삭제
    public void delete(UUID userId) {
        if(!this.data.containsKey(userId)){ //존재 여부 확인
            throw new NoSuchElementException("User with id " + userId + " not found");
        }
        this.data.remove(userId);
    }
}



