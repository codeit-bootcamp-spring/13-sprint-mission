package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final UserMapper userMapper;

  @Override
  @Transactional
  public UserDto create(String username, String email, String password, UUID profileId) {
    log.info("유저 생성 요청 - username: {}, email: {}", username, email);

    if (userRepository.findByUserName(username).isPresent()) {
      throw new IllegalArgumentException("이미 사용중인 userName입니다. ");
    }
    if (userRepository.findByEmail(email).isPresent()) {
      throw new IllegalArgumentException("이미 사용중인 email입니다: " + email);
    }

    User user = new User(username, password, email);
    if (profileId != null) {
      user.updateProfileId(binaryContentRepository.getReferenceById(profileId));
    }

    userRepository.save(user);
    userStatusRepository.save(new UserStatus(user, Instant.now()));
    //@Transactional사용시 새로 만드는 객체는 save필요. 영속성 콘텍스트에 없기 때문에
    //(조회시에는 이미 영속성 콘택스트에 있는 걸 보니까 save 필요 없음.)

    log.info("유저 생성 완료 - userId: {}", user.getId());
    return userMapper.toDto(user);
  }

  @Override
  @Transactional(readOnly = true)
  public UserDto find(UUID userId) {
    log.info("유저 단건 조회 요청 - userId: {}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(()
            -> new NoSuchElementException("존재하지 않는 userId입니다."));

    log.info("유저 단건 조회 완료 - userId: {}", userId);
    return userMapper.toDto(user);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserDto> findAll() {
    log.info("유저 전체 조회 요청");

    List<UserDto> result = userRepository.findAll().stream()
        .map(userMapper::toDto)
        .toList();

    log.info("유저 전체 조회 완료 - 유저 수: {}", result.size());
    return result;
  }

  @Override
  @Transactional
  public UserDto update(UUID userId, String newUsername, String newEmail, String newPassword,
      UUID newProfileId) {
    log.info("유저 수정 요청 - userId: {}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 userId입니다."));

    user.update(newUsername, newEmail, newPassword);
    if (newProfileId != null) {
      user.updateProfileId(binaryContentRepository.getReferenceById(newProfileId));
    }

    log.info("유저 수정 완료 - userId: {}", userId);
    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  public void delete(UUID userId) {
    log.info("유저 삭제 요청 - userId: {}", userId);
    User user = userRepository.findById(userId)
        .orElseThrow(()
            -> new NoSuchElementException("User with id " + userId + " not found"));

    if (user.getProfile() != null) {
      binaryContentRepository.deleteById(user.getProfile().getId());
    }

    if (user.getUserStatus() != null) {
      userStatusRepository.deleteById(user.getUserStatus().getId());
    }

    userRepository.deleteById(userId);
    log.info("유저 삭제 완료 - userId: {}", userId);
  }
}