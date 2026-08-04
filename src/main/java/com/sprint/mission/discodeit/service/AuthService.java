package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.LoginRequest;

//인증(Authentication) 관련 기능을 정의하는 서비스 인터페이스
public interface AuthService {

  UserDto login(LoginRequest loginRequest); //로그인 처리 메서드
}
