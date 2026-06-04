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

import java.util.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository fur;
    private final UserStatusRepository usr;
    private final BinaryContentRepository bcr;

    // Todo - check logic change.
    @Override
    public void createUser(Login lgn, UserProfile upf){
        boolean check =  fur.find(((c) -> ! c.getEmail().equals(lgn.getEmail())))
                .stream()
                .noneMatch(c -> c.getName().equals(upf.getName()));

        if (!check){
            return;
        }

        User user = User.builder()
                .email(lgn.getEmail())
                .password(lgn.getPassword())
                .name(upf.getName())
                .build();
        fur.save(user);

        usr.save(user.getId());
        if (upf.getThumbnail() != null){
            bcr.save(user.getId(),upf.getThumbnail());
        }
    }

    @Override
    public UserOutput getUserById(UUID id){
        User user;
        try {
            user = fur.find((c) -> c.getId().equals(id)).get(0);
            UserStatus ust = usr.findStatusByUserID(user.getId());
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
                        .online(usr.findStatusByUserID(u.getId()).online())
                        .build())
                .toList();
    }

    @Override
    public BinaryObjectOutput getUserThumbnail(UUID id){
        BinaryContent bct = bcr.findContantByAuthorID(id).orElseThrow(RuntimeException::new);
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

        try{
            bcr.findContantByAuthorID(id)
                    .orElseThrow(RuntimeException::new)
                    .setContentID(upf.getThumbnail());
        } catch (RuntimeException e){
            bcr.save(id, upf.getThumbnail());
        }

    }

    @Override
    public void deleteUser(UUID id){
        fur.delete(id);
        bcr.delete(id);
        usr.delete(id);
    }
}
