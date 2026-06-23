package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.BinaryContentCreate;
import com.sprint.mission.discodeit.dto.input.UserCreateRequest;
import com.sprint.mission.discodeit.dto.input.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.output.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;

import java.time.Instant;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class BasicUserService implements UserService {

    private final UserRepository ur;
    private final UserStatusRepository usr;
    private final BinaryContentRepository bcr;


    private UUID profileIdFromOBCC(Optional<BinaryContentCreate> obcc){
        return obcc.map(bcc -> {
            BinaryContent bc = new BinaryContent(
                    bcc.filename(),
                    bcc.contentType(),
                    bcc.size(),
                    bcc.content()
            );
            bcr.save(bc);
            return bc.getId();
        }).orElse(null);
    }

    @Override
    public User create(UserCreateRequest cui, Optional<BinaryContentCreate> obcc){

        ur.findByEmail(cui.email())
                .orElseThrow(() -> new DiscodeitException(
                        "User with email " + cui.email() + " aready exsists",
                        "User",
                        400
                ));
        ur.findByName(cui.username())
                .orElseThrow(() -> new DiscodeitException(
                        "User with username " + cui.username() + " aready exsists",
                        "User",
                        400
                ));



        User user = new User(
                cui.email(),
                cui.password(),
                cui.username(),
                profileIdFromOBCC(obcc)
        );
        ur.save(user);


        UserStatus ust = new UserStatus(
                user.getId(),
                Instant.now()
        );
        usr.save(ust);

        return user;
    }


    @Override
    public List<UserDto> getUserList(){
        return ur.findAll()
                .stream()
                .map(u -> {
                    UserStatus us =  usr.findByUserID(u.getId()).orElse(null);
                    return UserDto.builder()
                            .id(u.getId())
                            .createdAt(u.getCreatedAt())
                            .updatedAt(u.getUpdatedAt())
                            .username(u.getName())
                            .email(u.getEmail())
                            .online(us != null && us.online())
                            .profileId(
                                    u.getProfileId()
                            )
                            .build();
                })
                .toList();
    }


    @Override
    public User update(UUID id, UserUpdateRequest uui, Optional<BinaryContentCreate> obcc){
        User user = ur.findByID(id).orElseThrow(
            () -> new DiscodeitException(
                    "User with id" + id + " not found",
                    "User",
                    404)
        );

        Optional<User> sameNameChecker = ur.findByName(uui.newUsername());
        if(sameNameChecker.isPresent()){ throw new DiscodeitException(
                "user with name " + uui.newUsername() + " already used",
                "User",
                400
        );}
        Optional<User> sameEmailChecker = ur.findByEmail(uui.newEmail());
        if(sameEmailChecker.isPresent()){ throw new DiscodeitException(
                "user with email " + uui.newEmail() + " already used",
                "User",
                400
        );}

        user.setName(uui.newUsername());
        user.setEmail(uui.newEmail());
        user.setPassword(uui.newPassword());
        user.setProfileId(profileIdFromOBCC(obcc));
        ur.save(user);
        return user;
    }


    @Override
    public void delete(UUID id){
        User user = ur.findByID(id).orElseThrow(
                () -> new DiscodeitException(
                        "User with id" + id + " not found",
                        "User",
                        404)
        );
        Optional<UserStatus> us = usr.findByUserID(id);

        ur.delete(id);

        us.ifPresent(u -> usr.delete(u.getId()));
        if (user.getProfileId() != null) {
            bcr.delete(user.getProfileId());
        }
    }
}
