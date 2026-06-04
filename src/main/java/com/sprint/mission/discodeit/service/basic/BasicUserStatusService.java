package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.CreateUserStatusInput;
import com.sprint.mission.discodeit.dto.input.UpdateUserStatusInput;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {
    UserStatusRepository usr;
    UserRepository ur;

    @Override
    public void create(CreateUserStatusInput cusi){
        if (
                ur.findByID(cusi.getUserID()) == null
                || usr.findByUserID(cusi.getUserID()) != null
        ) throw new RuntimeException("invalid create.");

        usr.save(
                UserStatus.builder()
                        .userID(cusi.getUserID())
                        .lastLogin(cusi.getLoginTime())
                        .build()
        );
    }

    // Todo - return DTO OBJ
    @Override
    public UserStatus find(UUID id){
        return usr.findByUserID(id);
    }

    @Override
    public List<UserStatus> findAll(){
        return usr.findAll();
    }

    @Override
    public void update(UpdateUserStatusInput uusi){
        UserStatus ust = usr.findByID(uusi.getID());
        ust.setLastLogin(uusi.getLastLoginTime());
    }

    @Override
    public void updateByUserID(UpdateUserStatusInput uusi){
        UserStatus ust = usr.findByUserID(uusi.getID());
        ust.setLastLogin(uusi.getLastLoginTime());
    }

    @Override
    public void delete(UUID id){
        usr.delete(id);
    }


}

