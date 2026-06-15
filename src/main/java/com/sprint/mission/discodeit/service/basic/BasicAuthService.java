package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.LoginInput;
import com.sprint.mission.discodeit.dto.output.UserOutput;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;

import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {
    private final UserRepository ur;
    private final UserStatusRepository usr;

    @Override
    public UserOutput login(LoginInput loginInput){
        User user;
        try {
            user = ur.findByEmail(loginInput.getEmail());
            if (!user.getPassword().equals(loginInput.getPassword())) throw new RuntimeException("Invalid email or password");
        } catch (NullPointerException e){
            throw new RuntimeException("no user in database");
        }

        // update userState.updatedAt time
        UserStatus ust = usr.findByUserID(user.getId());
        ust.setUpdatedAt();

        return UserOutput.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .online(ust.online())
                .build();
    }

}
