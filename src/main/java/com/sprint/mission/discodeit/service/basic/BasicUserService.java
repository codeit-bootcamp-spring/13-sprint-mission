package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreate;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.mapper.UserMapper;
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
    private final UserMapper userMapper;
    private final BinaryContentStorage binaryContentStorage;


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


        if ( !JPAUserRepository.findByEmail(cui.email()).isEmpty() ) {
            throw  new DiscodeitException(
                        "User with email " + cui.email() + " aready exsists",
                        "User",
                        400
                );
        }
        if ( !JPAUserRepository.findByUsername(cui.username()).isEmpty() ) {
                throw new DiscodeitException(
                    "User with username " + cui.username() + " aready exsists",
                    "User",
                    400
            );
        }




        User user = new User(
                cui.username(),
                cui.email(),
                cui.password(),
                profileIdFromOBCC(obcc),
                null
        );
        UserStatus ust = new UserStatus(
                user,
                Instant.now()
        );
        user.setStatus(ust);

        return userMapper.toDto(JPAUserRepository.save(user));
    }


    @Override
    @Transactional
    public List<UserDto> getUserList(){
        return JPAUserRepository.findAllWithProfile()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }


    @Override
    @Transactional
    public UserDto update(UUID id, UserUpdateRequest uui, Optional<BinaryContentCreate> obcc){
        User user = JPAUserRepository.findById(id).orElseThrow(
            () -> new DiscodeitException(
                    "User with id" + id + " not found",
                    "User",
                    404)
        );

        Optional<User> sameNameChecker = JPAUserRepository.findByUsername(uui.newUsername()).stream().findFirst();
        if(sameNameChecker.isPresent()){ throw new DiscodeitException(
                "user with name " + uui.newUsername() + " already used",
                "User",
                400
        );}
        Optional<User> sameEmailChecker = JPAUserRepository.findByEmail(uui.newEmail()).stream().findFirst();
        if(sameEmailChecker.isPresent()){ throw new DiscodeitException(
                "user with email " + uui.newEmail() + " already used",
                "User",
                400
        );}

        System.out.println(obcc);
        System.out.println("call");

        if (uui.newUsername() != null) user.setUsername(uui.newUsername());
        if (uui.newEmail() != null) user.setEmail(uui.newEmail());
        if (uui.newPassword() != null) user.setPassword(uui.newPassword());
        if (profileIdFromOBCC(obcc) != null) {
            // db save check
            BinaryContent bc = profileIdFromOBCC(obcc);
            user.setProfile(bc);
        }
        return userMapper.toDto(user);
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
        if (user.getProfile() != null) {
            binaryContentRepository.delete(user.getProfile());
        }
    }
}
