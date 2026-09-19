package com.sprint.mission.discodeit.controller.swagger;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User", description = "사용자 관리 API")
public interface UserApi {

  @Operation(summary = "사용자 등록", description = "프로필 이미지와 함께 신규 사용자를 등록합니다.")
  ResponseEntity<UserResponse> createUser(UserCreateRequest request, MultipartFile profile);

  @Operation(summary = "사용자 정보 수정", description = "사용자 정보와 프로필 이미지를 수정합니다.")
  ResponseEntity<UserResponse> updateUser(
      @Parameter(description = "수정할 사용자 ID") UUID userId,
      UserUpdateRequest request,
      MultipartFile profile
  );

  @Operation(summary = "사용자 삭제", description = "사용자를 삭제합니다.")
  ResponseEntity<Void> deleteUser(
      @Parameter(description = "삭제할 사용자 ID") UUID userId
  );

  @Operation(summary = "전체 사용자 조회", description = "모든 사용자 목록을 조회합니다.")
  ResponseEntity<List<UserResponse>> findAllUser();

}
