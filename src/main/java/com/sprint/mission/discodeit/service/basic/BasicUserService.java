package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.aspect.LogAction;
import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import com.sprint.mission.discodeit.dto.command.user.UserUpdateCommand;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserEmailDuplicatedException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserUsernameDuplicatedException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BasicUserService implements UserService {
    private final UserRepository userRepository;
    private final BinaryContentService binaryContentService;
    private final ReadStatusService readStatusService;
    private final MessageService messageService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @LogAction(value = "사용자 생성")
    @Override
    public UserDto create(UserCreateCommand command, MultipartFile file) {
        validateCreatableUser(command);

        BinaryContent profile = binaryContentService.create(file).orElse(null);

        User savedUser = userRepository.save(new User(withEncodedPassword(command), profile));

        return userMapper.toDto(savedUser);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto findById(UUID userId) {
        User user = getUserRequireThrow(userId);

        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @LogAction(value = "사용자 수정")
    @Override
    @PreAuthorize("#userId == authentication.principal.userDto.id")
    public UserDto update(UUID userId, UserUpdateCommand command, MultipartFile file) {
        User user = getUserRequireThrow(userId);

        checkUserUpdates(command, user);

        BinaryContent oldImage = user.getProfile();
        BinaryContent newImage = binaryContentService.create(file).orElse(oldImage);

        user.updateInfo(withEncodedPassword(command), newImage);

        User updatedUser = userRepository.save(user);

        if (oldImage != null && !oldImage.getId().equals(newImage.getId())) {
            binaryContentService.delete(oldImage);
        }

        return userMapper.toDto(updatedUser);
    }

    @LogAction(value = "사용자 삭제", idName = "userId", idParamIndex = 0)
    @Override
    @PreAuthorize("#userId == authentication.principal.userDto.id")
    public void delete(UUID userId) {
        User user = getUserRequireThrow(userId);

        readStatusService.deleteByUserId(userId);
        messageService.detachByAuthorId(userId);

        userRepository.deleteById(user.getId());

        if (user.isProfileImageExist()) {
            binaryContentService.delete(user.getProfile());
        }
    }

    private void validateCreatableUser(UserCreateCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new UserEmailDuplicatedException(command.email());
        }
        if (userRepository.existsByUsername(command.username())) {
            throw new UserUsernameDuplicatedException(command.username());
        }
    }

    private User getUserRequireThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private void checkUserUpdates(UserUpdateCommand command, User user) {
        if (user.hasUsername(command.username()) && user.hasEmail(command.email())) {
            return;
        }

        if (!user.hasEmail(command.email()) && userRepository.existsByEmail(command.email())) {
            throw new UserEmailDuplicatedException(command.email());
        }

        if (!user.hasUsername(command.username()) && userRepository.existsByUsername(command.username())) {
            throw new UserUsernameDuplicatedException(command.username());
        }
    }

    private UserCreateCommand withEncodedPassword(UserCreateCommand command) {
        return new UserCreateCommand(
                command.username(),
                passwordEncoder.encode(command.password()),
                command.email()
        );
    }

    private UserUpdateCommand withEncodedPassword(UserUpdateCommand command) {
        if (!StringUtils.hasText(command.password())) {
            return command;
        }

        return new UserUpdateCommand(
                command.username(),
                passwordEncoder.encode(command.password()),
                command.email()
        );
    }
}
