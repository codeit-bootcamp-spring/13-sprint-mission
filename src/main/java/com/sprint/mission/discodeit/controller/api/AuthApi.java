package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.entity.User;
import javax.swing.text.html.HTML.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Auth", description = "인증 API")
@RequestMapping("/api/auths")
public interface AuthApi {

  @Operation(summary = "로그인")
  @ApiResponses({
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "400")
  })

  @PostMapping("/login")
  ResponseEntity<User> login(@RequestBody LoginRequest request);
}
