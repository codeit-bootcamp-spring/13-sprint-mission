package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.CreateUserInput;
import com.sprint.mission.discodeit.dto.input.UpdateUserInput;
import com.sprint.mission.discodeit.dto.output.BinaryObjectOutput;
import com.sprint.mission.discodeit.dto.output.UserDto;
import com.sprint.mission.discodeit.dto.output.UserOutput;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.ServiceLayerException;
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

        if (fur.findByEmail(cui.getEmail()) != null) throw new ServiceLayerException("Email already exists","UserService");
        if (fur.findByName(cui.getName()) != null) throw new ServiceLayerException("Name already exists","UserService");

        try {
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
                log.debug("\n" + cui.getThumbnail().toString() + " Thumbnail created");
                user.setProfileID(bc.getId());
            }
            fur.save(user);
            log.debug("\n" + user.getId().toString() + " User created");

            UserStatus ust = UserStatus.builder()
                    .userID(user.getId())
                    .lastLogin(Instant.now())
                    .build();
            usr.save(ust);
            log.debug("\n" + ust.getId().toString() + " UserStatus created");

        } catch (RuntimeException e) {
            throw new ServiceLayerException(e.getMessage(), "UserService");
        }
    }

    @Override
    public UserOutput getUserById(UUID id){
        try {
            User user = fur.findByID(id);
            UserStatus ust = usr.findByUserID(user.getId());

            return UserOutput.builder()
                    .name(user.getName())
                    .email(user.getEmail())
                    .online(ust.online())
                    .build();

        } catch (IndexOutOfBoundsException e) {
            log.warn(e.getMessage());
            return null;
        }
    }

    @Override
    public List<UserDto> getUserList(){
        return fur.findAll()
                .stream()
                .map(u -> UserDto.builder()
                        .id(u.getId())
                        .createdAt(u.getCreatedAt())
                        .updatedAt(u.getUpdatedAt())
                        .userName(u.getName())
                        .email(u.getEmail())
                        .online(usr.findByUserID(u.getId()).online())
                        .profileId(
                                u.getProfileID()
                                )
                        .build())
                .toList();
    }

    @Override
    public BinaryObjectOutput getUserThumbnail(UUID id){
        BinaryContent bct = bcr.findByID(id);
        return BinaryObjectOutput.builder()
                .contentID(bct.getContentID())
                .build();
    }


    /**
     * update functions
     *
     */

    @Override
    public void update(UpdateUserInput uui){
        // name duplicate check.
        if (fur.findByName(uui.getName()) != null) return;

        updateUserProfile(uui.getId(), uui.getName(), uui.getPw());
        if (uui.getThumbnail() != null) updateThumbnail(uui.getId(),uui.getThumbnail());
    }

    private void updateUserProfile(UUID id, String name, String password){
        if (fur.findByName(name) != null) throw new IllegalArgumentException(name + " is existed.");

        User user = fur.findByID(id);
        if (name != null) user.setName(name);
        if (password != null) user.setPassword(password);
        fur.save(user);
    }

    private void updateThumbnail(UUID authorID, UUID thumbID){
        if (fur.findByID(authorID) != null) return;
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
        fur.delete(id);
        usr.delete(usr.findByUserID(id).getId());
        if (!bcr.findByAuthorID(id).isEmpty()) {
            bcr.delete(bcr.findByAuthorID(id).get(0).getId());
        }
    }
}
