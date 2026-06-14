package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JCFUserService implements UserService {
    // Repository를 주입받을 준비 (이 서비스는 이 저장소로 사용)
    private final UserRepository userRepository;

    // 생성자에서 Repository를 연결
    // @Autowired 생성자를 통해 저장소 주입 받기(생성자가 하나라면 생략 가능)
    // 상단에 @RequiredArgsConstructor 를 작성해서 아래 코드는 생략
    /*
    public JCFUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    */

    // 인터페이스의 CRUD 기능을 Repository를 통해 구현
    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void update(User user) {
        userRepository.update(user);
    }

    @Override
    public void delete(String id) {
        userRepository.delete(id);
    }
}