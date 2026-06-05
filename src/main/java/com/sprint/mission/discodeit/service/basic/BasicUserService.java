package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.Login;
import com.sprint.mission.discodeit.dto.input.UserProfile;
import com.sprint.mission.discodeit.dto.output.BinaryObjectOutput;
import com.sprint.mission.discodeit.dto.output.UserOutput;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
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
    public void createUser(Login lgn, UserProfile upf){
        boolean check =  fur.findByEmail(lgn.getEmail()) != null
                || fur.find(u -> u.getName().equals(upf.getName())) != null;

        if (!check){
            log.debug("User create cancel - same name or email detached.");
            return;
        }

        User user = User.builder()
                .email(lgn.getEmail())
                .password(lgn.getPassword())
                .name(upf.getName())
                .build();
        fur.save(user);


        UserStatus ust = UserStatus.builder()
                .userID(user.getId())
                .lastLogin(Instant.now())
                .build();
        usr.save(ust);


        if (upf.getThumbnail() != null){
            bcr.save(
                    BinaryContent.builder()
                            .authorID(user.getId())
                            .contentID(upf.getThumbnail())
                            .build()
            );
        }
    }

    @Override
    public UserOutput getUserById(UUID id){
        User user;
        try {
            user = fur.find((c) -> c.getId().equals(id)).get(0);
            UserStatus ust = usr.findByUserID(user.getId());
            return UserOutput.builder()
                    .name(user.getName())
                    .email(user.getEmail())
                    .online(ust.online())
                    .build();
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UserOutput> getUserList(){
        return fur.find(((c) -> true))
                .stream()
                .map(u -> UserOutput.builder()
                        .name(u.getName())
                        .email(u.getEmail())
                        .online(usr.findByUserID(u.getId()).online())
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

    @Override
    public void updateProfileInfo(UUID id, String name, String pw){
        // name duplicate check.
        if (!fur.find(c -> c.getName().equals(name)).isEmpty()) return;

        User user = fur.find(c -> c.getId().equals(id)).get(0);
        user.setName(name);
        user.setPassword(pw);
        user.setUpdatedAt();

        fur.save(user);
    }

    @Override
    public void updateProfileImage(UUID id, UserProfile upf) {
        // check user exist.
        if (!fur.find(c -> c.getId().equals(id)).isEmpty()) return;

        bcr.delete(bcr.findByAuthorID(id).get(0).getId());
        bcr.save(
                BinaryContent.builder()
                        .contentID(upf.getThumbnail())
                        .authorID(id)
                        .build()
        );
    }

    @Override
    public void deleteUser(UUID id){
        fur.delete(id);
        usr.delete(usr.findByUserID(id).getId());
        if (!bcr.findByAuthorID(id).isEmpty()) {
            bcr.delete(bcr.findByAuthorID(id).get(0).getId());
        }
    }
}
