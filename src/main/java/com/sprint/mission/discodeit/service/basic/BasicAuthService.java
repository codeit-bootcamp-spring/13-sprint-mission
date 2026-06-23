package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.LoginRequest;
import com.sprint.mission.discodeit.dto.output.UserOutput;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.DiscodeitException;
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
    public UserOutput login(LoginRequest loginRequest){

        User user = ur.findByEmail(loginRequest.email()).orElse(null);
        if (user == null || !user.getPassword().equals(loginRequest.password())) {
            throw new DiscodeitException("not valid password or id","AuthService",400);
        }

        // update userState.updatedAt time
        UserStatus ust = usr.findByUserID(user.getId()).orElseThrow(
                () -> new DiscodeitException("no UserState on User","AuthService",500)
        );
        ust.setUpdatedAt();
        return UserOutput.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .online(ust.online())
                .build();
    }
}
