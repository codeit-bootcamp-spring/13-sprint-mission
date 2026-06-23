package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.IDRequest;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {
    private final UserStatusRepository usr;
    private final UserRepository ur;

    @Override
    public void create(IDRequest id){
        ur.findByID(id.id()).orElseThrow(
                () -> new DiscodeitException("no User by id" + id.id(),"UserStatus",400)
        );
        usr.findByUserID(id.id()).orElseThrow(
                () -> new DiscodeitException("no UserStatus by User id" + id.id(),"UserStatus",400)
        );
        usr.save(
                UserStatus.builder()
                        .userID(id.id())
                        .lastLogin(Instant.now())
                        .build()
        );
    }

    @Override
    public UserStatus find(UUID id){
        return usr.findByUserID(id).orElseThrow(
                () -> new DiscodeitException("no UserStatus by User id" + id,"UserStatus",400)
        );
    }

    @Override
    public List<UserStatus> findAll(){
        return usr.findAll();
    }

    @Override
    public void update(IDRequest uusi){
        UserStatus ust = usr.findByID(uusi.id()).orElseThrow(
                () -> new DiscodeitException("no UserStatus by User id" + uusi.id(),"UserStatus",400)
        );
        ust.setLastLogin(Instant.now());
    }

    @Override
    public void updateByUserID(UUID userID){
        UserStatus ust = usr.findByUserID(userID).orElseThrow(
                () -> new DiscodeitException("no UserStatus by User id" + userID,"UserStatus",400)
        );
        ust.setLastLogin(Instant.now());
    }

    @Override
    public void delete(UUID id){
        usr.delete(id);
    }


}

