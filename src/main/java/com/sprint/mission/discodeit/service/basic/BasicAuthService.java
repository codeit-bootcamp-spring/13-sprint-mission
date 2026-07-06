package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.LoginRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.repository.JPAUserRepository;

import com.sprint.mission.discodeit.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {
    private final JPAUserRepository JPAUserRepository;

    @Override
    public User login(LoginRequest loginRequest){

        User user = JPAUserRepository.findByEmail(loginRequest.username()).stream().findFirst()
                .orElseThrow(
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
