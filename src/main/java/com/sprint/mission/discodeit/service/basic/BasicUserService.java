package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreate;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.UserDuplicatedException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.mapper.MapperMethod;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;

import java.time.Instant;
import java.util.*;

import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentStorage binaryContentStorage;
    private final MapStructMapper mapStructMapper;
    private final MapperMethod mapperMethod;


    private BinaryContent profileIdFromOBCC(Optional<BinaryContentCreate> obcc){
        // duble running?
        return obcc.map(bcc -> {
            // add for Compatibility DB with localstorage.
            byte[] dummy = {0x40};

            BinaryContent bc = new BinaryContent(
                    bcc.filename(),
                    bcc.contentType(),
                    bcc.size(),
                    dummy
            );
            binaryContentRepository.save(bc);
            binaryContentStorage.put(bc.getId(),obcc.get().content());
            log.info(bc.getId() + "file saved");
            return bc;
        }).orElse(null);
    }

    @Override
    @Transactional
    public UserDto create(UserCreateRequest cui, Optional<BinaryContentCreate> obcc){

        nameCheck(cui.username());
        emailCheck(cui.email());

        BinaryContent bc = profileIdFromOBCC(obcc);

        User user = new User(
                cui.username(),
                cui.email(),
                cui.password(),
                bc,
                null
        );
        UserStatus ust = new UserStatus(user, Instant.now());
        user.setStatus(ust);

        log.debug("user created - {}", user.toString());

        userRepository.save(user);
        log.info("user created - id: {}, username: {}", user.getId(), cui.username());
        return mapStructMapper.toDto(user,toBinaryDto(user),user.online());
    }

    @Override
    @Transactional
    public List<UserDto> getUserList(){
        return userRepository.findAllWithProfile()
                .stream()
                .map(u -> mapStructMapper.toDto(u,toBinaryDto(u),u.online()))
                .toList();
    }


    // Todo - Profile create 2 times. why????
    @Override
    @Transactional
    public UserDto update(UUID id, UserUpdateRequest uui, Optional<BinaryContentCreate> obcc){
        User user = getUserOrException(id);

        nameCheck(uui.newUsername());
        emailCheck(uui.newEmail());

        if (uui.newUsername() != null) user.setUsername(uui.newUsername());
        if (uui.newEmail() != null) user.setEmail(uui.newEmail());
        if (uui.newPassword() != null) user.setPassword(uui.newPassword());
        if (profileIdFromOBCC(obcc) != null) {
            // db save check
            BinaryContent bc = profileIdFromOBCC(obcc);
            user.setProfile(bc);
        }

        user = userRepository.save(user);
        log.info("user with id - {} updated", id);

        return mapStructMapper.toDto(
                user
                , toBinaryDto(user)
                , user.online()
        );
    }


    @Override
    @Transactional
    public void delete(UUID id){
        User user =  getUserOrException(id);
        Optional<UserStatus> us = userStatusRepository.findByUserId(id).stream().findFirst();

        userRepository.delete(user);
        us.ifPresent(userStatusRepository::delete);

        log.info("user with id - {} deleted", id);

    }

    private User getUserOrException(UUID id){
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User with id - {} not found", id)
        );

    }

    private void nameCheck(String username){
        Optional<User> sameNameChecker = userRepository.findByUsername(username).stream().findFirst();
        if(sameNameChecker.isPresent()){
            throw new UserDuplicatedException("User with name - {} already exists", username);
        }
    }

    private void emailCheck(String email){
        Optional<User> sameEmailChecker = userRepository.findByEmail(email).stream().findFirst();
        if(sameEmailChecker.isPresent()){
            throw new UserDuplicatedException("User with email - {} already exists", email);
        }
    }

    private BinaryContentDto toBinaryDto(User user){
        if (user.getProfile() == null) return null;
        BinaryContent bc = user.getProfile();
        return mapStructMapper.toDto(bc, mapperMethod.getByteFrom(bc));
    }

}
