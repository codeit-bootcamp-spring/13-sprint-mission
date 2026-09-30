package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.projection.UserProjection;
import com.sprint.mission.discodeit.dto.request.MultipartFileDto;
import com.sprint.mission.discodeit.dto.request.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.UserDuplicatedException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.mapper.MapperMethod;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.SessionService;
import com.sprint.mission.discodeit.security.role.Role;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;

import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentStorage binaryContentStorage;
    private final MapStructMapper mapStructMapper;
    private final MapperMethod mapperMethod;

    private final PasswordEncoder passwordEncoder;

    private final SessionService sessionService;

    @Override
    @Transactional
    public UserDto create(UserCreateRequest userCreateRequest, Optional<MultipartFileDto> multiFileDto){
        String username = nameCheck(userCreateRequest.username());
        String email = emailCheck(userCreateRequest.email());
        String password = passwordEncoder.encode(userCreateRequest.password());
        BinaryContent bc = multiFileDto.map(binaryContentRepository::saveWithMultipartCommand).orElse(null);

        User user = new User(
                username,
                email,
                password,
                bc,
                Role.USER   // default role is User.
        );

        userRepository.save(user);

        // userDto from User
        return mapStructMapper.toDto(
                user,
                getProfileToDto(user.getProfile()),
                sessionService.userOnline(username)
        );
    }

    @Override
    @Transactional
    public List<UserDto> getUserList(){
        List<UserProjection> users = userRepository.getAllUsers().values().stream().toList();
        Map<UUID, BinaryContentDto> profiles = binaryContentRepository.getBinaryContentsInIdList(
                users.stream()
                        .map(UserProjection::profileId)
                        .filter(Objects::nonNull)   // null Point Exception 방지.
                        .toList()
        );

        // userDto from UserProfile
        return users.stream().map(
                user -> mapStructMapper.toDto(
                            user,
                            profiles.get(user.profileId()),
                            sessionService.userOnline(user.username())
                    )
        ).toList();

    }


    @Override
    @Transactional
    public UserDto update(UUID id, UserUpdateRequest uui, Optional<MultipartFileDto> multiFileDto){
        User user = getUserOrException(id);

        String newName = nameCheck(uui.newUsername());
        String newEmail = emailCheck(uui.newEmail());
        String newPassword = !uui.newPassword().isBlank()
                ? passwordEncoder.encode(uui.newPassword())
                : null;
        BinaryContent newProfile = multiFileDto.map(binaryContentRepository::saveWithMultipartCommand).orElse(null);

        // JPA DirtyCheck 으로 별도 save 없이 유저 정보 업데이트
        user.update(newName, newEmail, newPassword, newProfile);

        return mapStructMapper.toDto(
                user,
                getProfileToDto(user.getProfile()),
                sessionService.userOnline(user.getUsername())
        );
    }


    @Override
    @Transactional
    public void delete(UUID id){
        User user =  getUserOrException(id);

        if (user.getProfile() != null) binaryContentRepository.deleteBinaryContent(user.getProfile());

        userRepository.delete(user);
    }

    private User getUserOrException(UUID id){
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User with id - {} not found", id)
        );

    }

    private String nameCheck(String username){
        Optional<User> sameNameChecker = userRepository.findByUsername(username).stream().findFirst();
        if(sameNameChecker.isPresent()){
            throw new UserDuplicatedException("User with name - {} already exists", username);
        }
        return username;
    }

    private String emailCheck(String email){
        Optional<User> sameEmailChecker = userRepository.findByEmail(email).stream().findFirst();
        if(sameEmailChecker.isPresent()){
            throw new UserDuplicatedException("User with email - {} already exists", email);
        }
        return email;
    }

    private BinaryContentDto getProfileToDto(BinaryContent profile){
        if (profile == null) return null;
        return binaryContentRepository.getBinaryContentById(profile.getId()).orElse(null);
    }

}
