package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RequiredArgsConstructor //final이 선언된 필드를 대상으로 생성자를 자동 생성하는 Lombok 어노테이션
@Controller //해당 클래스를 spring mvc의 controller(컨트롤러)로 등록함.
@ResponseBody //모든 메서드의 반환값을 view(jsp, thymeleaf 등)가 아닌 HTTP Response body(JSON 등)로 반환하도록 설정함
@RequestMapping("/api/auth") //이 컨트롤러에서 처리하는 모든 요청의 공통 URL을 지정함.
public class AuthController {
    private final AuthService authService;

    //"api/auth/Login" 요청을 처리하는 메서드
    @RequestMapping(path = "login")
    public ResponseEntity<User> login(@RequestParam LoginRequest loginRequest) {
        //전달받은 로그인 요청(LoginRequest)을 AuthService로 전달하여 로그인 인증을 수행함. 인증이 성공하면 해당 사용자 객체를 반환받음.
        User user = authService.login(loginRequest);
        //HTTP 응답(Request)을 생성하고 상태코드는 200(OK)으로 설정하고 로그인에 성공한 사용자 정보를 Response Body에 담아 클라이언트에게 반환
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(user);
    }
}
