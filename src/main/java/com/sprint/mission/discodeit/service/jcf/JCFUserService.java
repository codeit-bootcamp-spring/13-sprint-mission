package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.dto.request.UserRequest;
import com.sprint.mission.discodeit.dto.request.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
// [구현체] 기획서대로 실제로 일하는 주체
public class JCFUserService implements UserService {

    // [요구사항] data 필드를 final로 선언하세요!
    private final List<User> data; // 창고(data) 선언

    // [요구사항] 생성자에서 초기화하세요!
    public JCFUserService() {
        this.data = new ArrayList<User>(); // 창고 생성 (생성자)
    }

    @Override
    public UserResponse create(UserRequest dto) { // 매개변수 선언, 유저 생성하는 기능 구현
        User user = new User(dto.username(),  dto.password(), dto.email());
        data.add(user); // 창고에 넣기 (진짜 등록)
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), true);
    }

    @Override
    public Optional<UserResponse> findById(UUID id) { // 단건 조회
        for (User foundUser : data) {
            if (foundUser.getId().equals(id)) {
                return Optional.of(new UserResponse(foundUser.getId(), foundUser.getUsername(), null, true));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<UserResponse> findAll() { // 전체 조회
        List<UserResponse> responses = new ArrayList<>();
        for (User foundUser : data) {
            responses.add(new UserResponse(foundUser.getId(), foundUser.getUsername(), null, true));
        }
        return responses;
    }

    @Override
    public UserResponse update(UUID id, UserRequest dto) {
        for (User foundUser : data) {
            if (foundUser.getId().equals(id)) {
                foundUser.update(dto.username(),  dto.email(), dto.password());
                return new UserResponse(foundUser.getId(), foundUser.getUsername(),null, true);
            }
        }
        throw new IllegalArgumentException("User not found");

    }

    @Override
    public void delete(UUID id) {
        data.removeIf(user -> user.getId().equals(id));
    }

}
