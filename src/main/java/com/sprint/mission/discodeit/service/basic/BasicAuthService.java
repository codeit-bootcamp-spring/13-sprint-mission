package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ExceptionCode;
import com.sprint.mission.discodeit.exception.UserException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.mapper.MapperMethod;
import com.sprint.mission.discodeit.repository.UserRepository;

import com.sprint.mission.discodeit.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicAuthService implements AuthService {
    private final UserRepository userRepository;
    private final MapStructMapper mapStructMapper;
    private final MapperMethod mapperMethod;

    @Override
    @Transactional
    public UserDto login(LoginRequest loginRequest){
        log.debug("Login Request: {}", loginRequest);

        User user = getUserOrExceptionByName(loginRequest.username());
        checkPassword(user, loginRequest.password());

        return userDto(user);
    }

    private UserDto userDto(User user){
        BinaryContent profile = user.getProfile();
        return mapStructMapper.toDto(user,binaryContentDto(profile),user.online());
    }

    private BinaryContentDto binaryContentDto(BinaryContent bc){
        return mapStructMapper.toDto(bc, mapperMethod.getByteFrom(bc));
    }

    private User getUserOrExceptionByName(String name){
        return userRepository.findByUsername(name).stream().findFirst()
                .orElseThrow(
                        () -> new UserNotFoundException("User not found with name: {} " , name)
                );
    }

    private void checkPassword (User user, String password){
        if(!user.getPassword().equals(password)){
            throw new UserException(ExceptionCode.REQUEST_VALUE_ERROR,"Wrong password");
        }
    }
}
