package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class BasicUserStatusService implements UserStatusService {

  private final UserStatusRepository userStatusRepository;

  @Override
  public UserStatus create(UserStatusCreateRequest request) {
    // UserStatus 생성은 UserService에서 User 생성 시 자동으로 처리됨
    throw new UnsupportedOperationException("UserStatus는 User 생성 시 자동으로 생성됩니다.");
  }

  @Transactional(readOnly = true)
  @Override
  public UserStatus find(UUID userStatusId) {
    return userStatusRepository.findById(userStatusId)
            .orElseThrow(
                    () -> new NoSuchElementException("UserStatus with id " + userStatusId + " not found"));
  }

  @Transactional(readOnly = true)
  @Override
  public List<UserStatus> findAll() {
    return userStatusRepository.findAll();
  }

  @Override
  public UserStatus update(UUID userStatusId, UserStatusUpdateRequest request) {
    UserStatus userStatus = userStatusRepository.findById(userStatusId)
            .orElseThrow(
                    () -> new NoSuchElementException("UserStatus with id " + userStatusId + " not found"));
    userStatus.update(request.newLastActiveAt());
    return userStatusRepository.save(userStatus);
  }

  @Override
  public UserStatus updateByUserId(UUID userId, UserStatusUpdateRequest request) {
    // findByUserId → findByUser_Id로 변경
    UserStatus userStatus = userStatusRepository.findByUser_Id(userId)
            .orElseThrow(
                    () -> new NoSuchElementException("UserStatus with userId " + userId + " not found"));
    userStatus.update(request.newLastActiveAt());
    return userStatusRepository.save(userStatus);
  }

  @Override
  public void delete(UUID userStatusId) {
    if (!userStatusRepository.existsById(userStatusId)) {
      throw new NoSuchElementException("UserStatus with id " + userStatusId + " not found");
    }
    userStatusRepository.deleteById(userStatusId);
  }
}