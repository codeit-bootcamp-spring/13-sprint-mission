package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.LoginRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.repository.UserRepository;

import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {
    private final UserRepository ur;

    @Override
    public User login(LoginRequest loginRequest){

        User user = ur.findByEmail(loginRequest.username()).orElseThrow(
                () -> new DiscodeitException(
                        "User with username " + loginRequest.username() + " not found",
                        "Auth",
                        404
                )
        );

        if(!user.getPassword().equals(loginRequest.password())){
            throw new DiscodeitException(
                    "Wrong password",
                    "Auth",
                    400
            );
        }
        return user;
    }
}
