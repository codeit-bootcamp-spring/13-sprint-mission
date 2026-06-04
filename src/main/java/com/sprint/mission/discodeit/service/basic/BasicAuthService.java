package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.Login;
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
    public UserOutput login(Login login){
        User user = ur.find(c-> c.getEmail().equals(login.getEmail())).get(0);
        if (!user.getPassword().equals(login.getPassword())) throw new RuntimeException("Invalid email or password");


        UserStatus ust = usr.findByUserID(user.getId());
        // login time update to now
        ust.setUpdatedAt();

        return UserOutput.builder()
                .email(user.getEmail())
                .name(user.getName())
                .online(ust.online())
                .build();
    }

}
