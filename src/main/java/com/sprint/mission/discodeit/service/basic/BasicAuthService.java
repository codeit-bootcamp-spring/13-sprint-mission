package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

//AuthService 기본 구현체 (로그인 기능을 담당하는 서비스)
@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository; //사용자 데이터 접근 계층( DB/File,JCF 교체 가능)

  //로그인 처리 로직
  @SneakyThrows
  @Override
  public UserDto login(LoginRequest loginRequest) {
    //1.요청 데이터 분리
    String username = loginRequest.getUsername();
    String password = loginRequest.getPassword();

    //2.username으로 사용자 조회
    User user = userRepository.findByUsername(username)
        .orElseThrow(
            () -> new NoSuchElementException("User with username " + username + " not found"));

    //3.비밀번호 검증
    if (!user.getPassword().equals(password)) {
      //throw new AuthenticationException("Wrong password");
      throw new IllegalAccessException("Wrong password"); //인증 실패 처리
    }
    //4. 로그인 성공->User 반환
    return user;
  }
}

