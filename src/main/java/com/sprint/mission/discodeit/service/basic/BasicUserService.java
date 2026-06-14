package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
//Service 계층 구현체. 실제 데이터 저장은 Repository에 위임함
public class BasicUserService implements UserService {
    private final UserRepository userRepository; //사용자 저장소

    @Override //사용자 생성
    public User create(String username, String email,String password) {
        User user = new User(username, email, password); //새로운 사용자 생성
        return userRepository.save(user); //Repository 저장
    }

    @Override //사용자 단건 조회
    public User find(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(()-> new NoSuchElementException("User with id " + userId + " not found")); //조회 실패 시 예외발생
    }

    @Override //전체 사용자 조회. Repository가 가진 모든 User 반환
    public List<User> findAll() {return userRepository.findAll();}

    @Override //사용자 정보 수정
    public User update(UUID userId, String newUsername, String newEmail, String newPassword) {
        User user = userRepository.findById(userId) //기존 사용자 조회
                .orElseThrow(()-> new NoSuchElementException("User with id " + userId + " not found"));
        user.update(newUsername, newEmail, newPassword); //User 엔티티 내부 update 메서드 실행
        return userRepository.save(user); //변경된 객체 재저장
    }

    @Override //사용자 삭제
    public void delete(UUID userId) {
        if (!userRepository.existsById(userId)) { //존재 여부 확인
            throw new NoSuchElementException("User with id " + userId + " not found");
        }
        userRepository.deleteById(userId); //Repository 삭제
    }
}
