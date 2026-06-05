package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFUserRepository;
import com.sprint.mission.discodeit.service.UserService;
import java.util.*;

//UserService를 실제로 동작시키는 JCF(컬렉션) 기방 구현체
public class JCFUserService implements UserService {

    private final UserRepository repository;

    public JCFUserService() {
        this.repository = new JCFUserRepository();
    }

    @Override
    public void create(User user) {
        repository.save(user);
    }
    @Override
    public User read(UUID id) {
        return repository.findById(id);
    }
    @Override
    public List<User> readAll() {
        return repository.findAll();
    }

    @Override
    public void update(User user) {
        repository.save(user);
    }

    @Override
    public void delete(UUID id) {
        repository.delete(id);
    }
    /*
    // 자바에서 데이터를 저장할 곳(여기선 Map 사용), final로 (UUID(키)-User(값) 쌍을 저장하는 Map. 반드시 final로 선언)
    private final Map<UUID, User> data;

    //생성자에서 date(Map) 객체를 초기화
    public JCFUserService() {
            this.data = new HashMap<>();
        }

        //사용자 추가
        @Override
        public void create(User user) {
            data.put(user.getId(), user);
        }

        //사용자 한 명(id로) 조회
        @Override
        public User read(UUID id) {
            return data.get(id);
        }

        //전체 사용자 목록 반환
        @Override
        public List<User> readAll() {
            return new ArrayList<>(data.values());
        }

        //사용자 정보 수정 (id로 덮어쓰기)
        @Override
        public void update(User user) {
            data.put(user.getId(), user);
        }

        //사용자 삭제 (id로)
        @Override
        public void delete(UUID id) {
            data.remove(id);
        } */
}


