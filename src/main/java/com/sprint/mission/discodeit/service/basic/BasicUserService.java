package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreate;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.mapper.MapperMethod;
import com.sprint.mission.discodeit.repository.JPABinaryContentRepository;
import com.sprint.mission.discodeit.repository.JPAUserRepository;
import com.sprint.mission.discodeit.repository.JPAUserStatusRepository;
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

    private final JPAUserRepository JPAUserRepository;
    private final JPAUserStatusRepository JPAUserStatusRepository;
    private final JPABinaryContentRepository binaryContentRepository;
    private final BinaryContentStorage binaryContentStorage;
    private final MapStructMapper mapStructMapper;
    private final MapperMethod mapperMethod;


    private BinaryContent profileIdFromOBCC(Optional<BinaryContentCreate> obcc){
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
            System.out.println("file save");
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

        JPAUserRepository.save(user);
        return mapStructMapper.toDto(user,toBinaryDto(user),user.online());
    }

    @Override
    @Transactional
    public List<UserDto> getUserList(){
        return JPAUserRepository.findAllWithProfile()
                .stream()
                .map(u -> mapStructMapper.toDto(u,toBinaryDto(u),u.online()))
                .toList();
    }


    // Todo - Profile create 2 times. why????
    @Override
    @Transactional
    public UserDto update(UUID id, UserUpdateRequest uui, Optional<BinaryContentCreate> obcc){
        User user = JPAUserRepository.findById(id).orElseThrow(
            () -> new DiscodeitException(
                    "User with id" + id + " not found",
                    "User",
                    404)
        );

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
        return mapStructMapper.toDto(
                user
                , toBinaryDto(user)
                , user.online()
        );
    }


    @Override
    @Transactional
    public void delete(UUID id){
        User user = JPAUserRepository.findById(id).orElseThrow(
                () -> new DiscodeitException(
                        "User with id" + id + " not found",
                        "User",
                        404)
        );
        Optional<UserStatus> us = JPAUserStatusRepository.findByUserId(id).stream().findFirst();

        JPAUserRepository.delete(user);

        us.ifPresent(JPAUserStatusRepository::delete);
//        if (user.getProfile() != null) {
//            binaryContentRepository.delete(user.getProfile());
//        }
    }

    private void nameCheck(String username){
        Optional<User> sameNameChecker = JPAUserRepository.findByUsername(username).stream().findFirst();
        if(sameNameChecker.isPresent()){ throw new DiscodeitException(
                "user with name " + username + " already used",
                "User",
                400
        );}
    }

    private void emailCheck(String email){
        Optional<User> sameEmailChecker = JPAUserRepository.findByEmail(email).stream().findFirst();
        if(sameEmailChecker.isPresent()){ throw new DiscodeitException(
                "user with email " + email + " already used",
                "User",
                400
        );}
    }

    private BinaryContentDto toBinaryDto(User user){
        BinaryContent bc = user.getProfile();
        return mapStructMapper.toDto(bc, mapperMethod.getByteFrom(bc));
    }

}
