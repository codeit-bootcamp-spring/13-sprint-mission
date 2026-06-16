package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.CreateUserInput;
import com.sprint.mission.discodeit.dto.input.UpdateUserInput;
import com.sprint.mission.discodeit.dto.output.BinaryObjectOutput;
import com.sprint.mission.discodeit.dto.output.UserDto;
import com.sprint.mission.discodeit.dto.output.UserOutput;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.DiscodeitUserException;
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

    private final UserRepository fur;
    private final UserStatusRepository usr;
    private final BinaryContentRepository bcr;

    @Override
    public void createUser(CreateUserInput cui){

        fur.findByEmail(cui.getEmail()).orElseThrow(() -> new DiscodeitUserException("Email already exists",400));
        fur.findByName(cui.getName()).orElseThrow(() -> new DiscodeitUserException("Name already exists",400));

        User user = User.builder()
                .email(cui.getEmail())
                .password(cui.getPassword())
                .name(cui.getName())
                .profileID(cui.getThumbnail())
                .build();

        if (cui.getThumbnail() != null){
            BinaryContent bc = BinaryContent.builder()
                    .authorID(user.getId())
                    .contentID(cui.getThumbnail())
                    .build();
            bcr.save(bc);
            user.setProfileID(bc.getId());
            log.debug("\n >> " + cui.getThumbnail().toString() + " Thumbnail created");
        }

        fur.save(user);
        log.debug("\n >> " + user.getId().toString() + " User created");

        UserStatus ust = UserStatus.builder()
                .userID(user.getId())
                .lastLogin(Instant.now())
                .build();
        usr.save(ust);
        log.debug("\n >> " + ust.getId().toString() + " UserStatus created");
    }

    @Override
    public UserOutput getUserById(UUID id){
        User user = fur.findByID(id).orElseThrow(() -> new DiscodeitUserException("User by id " + id  + " not found",400));
        UserStatus ust = usr.findByUserID(id).orElseThrow(
                () -> new DiscodeitUserException("UserStatus by id " + id + " not found",400)
        );

        return UserOutput.builder()
                .name(user.getName())
                .email(user.getEmail())
                .online(ust.online())
                .build();

    }

    @Override
    public List<UserDto> getUserList(){
        return fur.findAll()
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
                                    u.getProfileID()
                            )
                            .build();
                })
                .toList();
    }

    @Override
    public BinaryObjectOutput getUserThumbnail(UUID id){
        BinaryContent bct = bcr.findByID(id).orElseThrow(
                () -> new DiscodeitUserException("Thumbnail not found",400)
        );
        return BinaryObjectOutput.builder()
                .contentID(bct.getContentID())
                .build();
    }


    @Override
    public void update(UpdateUserInput uui){
        // name duplicate check.
        if (fur.findByName(uui.name()).isPresent()) {
            throw new DiscodeitUserException("Not exist User on Request",400);
        }

        User user = updateUserProfile(uui.id(), uui.name(), uui.pw());
        if (uui.thumbnail() != null) updateThumbnail(uui.id(),uui.thumbnail());
        fur.save(user);
    }

    private User updateUserProfile(UUID id, String name, String password){
        User user = fur.findByID(id).orElseThrow(
                () -> new DiscodeitUserException("User not found",400)
        );
        if (name != null) user.setName(name);
        if (password != null) user.setPassword(password);
        return user;
    }

    private void updateThumbnail(UUID authorID, UUID thumbID){
        if (bcr.findByID(authorID).isEmpty()) throw new DiscodeitUserException("Thumbnail not existed",400);
        bcr.delete(authorID);
        bcr.save(
                BinaryContent.builder()
                        .authorID(authorID)
                        .contentID(thumbID)
                        .build()
        );
    }

    @Override
    public void delete(UUID id){
        User user = fur.findByID(id).orElseThrow(
                () -> new DiscodeitUserException("User not found",400)
        );
        UserStatus us = usr.findByUserID(id).orElseThrow(
                () -> new DiscodeitUserException("UserStatus not found",400)
        );

        fur.delete(id);
        usr.delete(us.getId());
        if (user.getProfileID() != null) {
            bcr.delete(user.getProfileID());
        }
    }
}
