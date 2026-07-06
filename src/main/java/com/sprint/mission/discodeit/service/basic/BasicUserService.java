package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.BinaryContentCreate;
import com.sprint.mission.discodeit.dto.input.UserCreateRequest;
import com.sprint.mission.discodeit.dto.input.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.output.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.repository.JPABinaryContentRepository;
import com.sprint.mission.discodeit.repository.JPAUserRepository;
import com.sprint.mission.discodeit.repository.JPAUserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;

import java.time.Instant;
import java.util.*;

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


    private BinaryContent profileIdFromOBCC(Optional<BinaryContentCreate> obcc){
        return obcc.map(bcc -> {
            BinaryContent bc = new BinaryContent(
                    bcc.filename(),
                    bcc.contentType(),
                    bcc.size(),
                    bcc.content()
            );
            return binaryContentRepository.save(bc);
        }).orElse(null);
    }

    @Override
    @Transactional
    public User create(UserCreateRequest cui, Optional<BinaryContentCreate> obcc){


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
                cui.email(),
                cui.password(),
                cui.username(),
                profileIdFromOBCC(obcc),
                null
        );
        UserStatus ust = new UserStatus(
                user,
                Instant.now()
        );
        user.setStatus(ust);

        JPAUserStatusRepository.save(ust);
        JPAUserRepository.save(user);
        return user;
    }


    @Override
    public List<UserDto> getUserList(){
        return JPAUserRepository.findAll()
                .stream()
                .map(u -> {
                    UserStatus us =  JPAUserStatusRepository.findByUserId(u.getId()).stream().findFirst().orElse(null);
                    return UserDto.builder()
                            .id(u.getId())
                            .createdAt(u.getCreatedAt())
                            .updatedAt(u.getUpdatedAt())
                            .username(u.getUsername())
                            .email(u.getEmail())
                            .online(us != null && us.online())
                            .profileId(
                                    u.getProfile() != null ? u.getProfile().getId() : null
                            )
                            .build();
                })
                .toList();
    }


    @Override
    @Transactional
    public User update(UUID id, UserUpdateRequest uui, Optional<BinaryContentCreate> obcc){
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

        if (uui.newUsername() != null) user.setUsername(uui.newUsername());
        if (uui.newEmail() != null) user.setEmail(uui.newEmail());
        if (uui.newPassword() != null) user.setPassword(uui.newPassword());
        if (profileIdFromOBCC(obcc) != null) user.setProfile(profileIdFromOBCC(obcc));
        return user;
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
