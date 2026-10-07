package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.config.CacheConfig;
import com.sprint.mission.discodeit.dto.request.MultipartFileDto;
import com.sprint.mission.discodeit.dto.request.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.binarycontent.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.exception.user.UserDuplicatedException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.EntityMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.CertificationChecker;
import com.sprint.mission.discodeit.security.role.Role;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class BasicUserService implements UserService {

    private final String SERVICE_NAME = "UserService";

    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final EntityMapper entityMapper;

    private final PasswordEncoder passwordEncoder;

    private final CertificationChecker certificationChecker;

    private final ApplicationEventPublisher eventPublisher;


    @Override
    @CacheEvict(cacheNames = CacheConfig.USER_CACHE, allEntries = true)
    @Transactional
    public UserDto create(UserCreateRequest userCreateRequest, Optional<MultipartFileDto> multiFileDto){
        String username = nameCheck(userCreateRequest.username());
        String email = emailCheck(userCreateRequest.email());
        String password = passwordEncoder.encode(userCreateRequest.password());

        // 바이너리 컨텐츠 저장 및 이벤트 발행.
        BinaryContent binaryContent = multiFileDto.map(this::getBinaryContent).orElse(null);

        User user = new User(
                username,
                email,
                password,
                binaryContent,
                Role.USER   // default role is User.
        );

        userRepository.save(user);

        // userDto from User
        return entityMapper.toDto(user, certificationChecker.online(user.getId()));
    }

    @Cacheable(CacheConfig.USER_CACHE)
    @Override
    @Transactional
    public List<UserDto> getUserList(){
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> entityMapper.toDto(user, certificationChecker.online(user.getId())))
                .toList();
    }


    @Override
    @CacheEvict(cacheNames = CacheConfig.USER_CACHE, allEntries = true)
    @Transactional
    public UserDto update(UUID id, UserUpdateRequest uui, Optional<MultipartFileDto> multiFileDto){
        User user = getUserOrException(id);

        String newName = nameCheck(uui.newUsername());
        String newEmail = emailCheck(uui.newEmail());
        String newPassword = !uui.newPassword().isBlank()
                ? passwordEncoder.encode(uui.newPassword())
                : null;

        BinaryContent newProfile = multiFileDto.map(this::getBinaryContent).orElse(null);

        // JPA DirtyCheck 으로 별도 save 없이 유저 정보 업데이트
        user.update(newName, newEmail, newPassword, newProfile);

        return entityMapper.toDto(user, certificationChecker.online(user.getId()));
    }


    @Override
    @CacheEvict(cacheNames = CacheConfig.USER_CACHE, allEntries = true)
    @Transactional
    public void delete(UUID id){
        User user =  getUserOrException(id);

        // todo - 실제 데이터 삭제 로직 추가.
        if (user.getProfile() != null) binaryContentRepository.delete(user.getProfile());

        userRepository.delete(user);
    }

    /*
    바이너리 컨텐츠 저장용 매서드.
     */
    private BinaryContent getBinaryContent(MultipartFileDto fileDto){
        BinaryContent binarycontent = binaryContentRepository.save(fileDto.toBinaryContent());
        eventPublisher.publishEvent(new BinaryContentCreatedEvent(
                binarycontent.getId(),
                fileDto.content()
        ));
        return binarycontent;
    }

    private User getUserOrException(UUID id){
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException(
                        "User with id - {} not found",
                        SERVICE_NAME,
                        id
                )
        );

    }

    private String nameCheck(String username){
        Optional<User> sameNameChecker = userRepository.findByUsername(username);
        if(sameNameChecker.isPresent()){
            throw new UserDuplicatedException(
                    "{} - User with name - {} already exists",
                    SERVICE_NAME,
                    username
            );
        }
        return username;
    }

    private String emailCheck(String email){
        Optional<User> sameEmailChecker = userRepository.findByEmail(email);
        if(sameEmailChecker.isPresent()){
            throw new UserDuplicatedException(
                    "{} - User with email - {} already exists",
                    SERVICE_NAME,
                    email
            );
        }
        return email;
    }
}
