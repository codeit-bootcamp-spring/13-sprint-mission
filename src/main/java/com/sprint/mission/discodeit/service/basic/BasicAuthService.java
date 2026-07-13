package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.mapper.MapperMethod;
import com.sprint.mission.discodeit.repository.JPAUserRepository;

import com.sprint.mission.discodeit.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicAuthService implements AuthService {
    private final JPAUserRepository JPAUserRepository;
    private final MapStructMapper mapStructMapper;
    private final MapperMethod mapperMethod;

    @Override
    @Transactional
    public UserDto login(LoginRequest loginRequest){

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
        return userDto(user);
    }

    private UserDto userDto(User user){
        BinaryContent profile = user.getProfile();
        return mapStructMapper.toDto(user,binaryContentDto(profile),user.online());
    }

    private BinaryContentDto binaryContentDto(BinaryContent bc){
        return mapStructMapper.toDto(bc, mapperMethod.getByteFrom(bc));
    }
}
