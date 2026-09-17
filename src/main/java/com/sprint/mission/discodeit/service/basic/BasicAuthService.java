package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.projection.UserProjection;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.mapper.MapperMethod;
import com.sprint.mission.discodeit.repository.UserRepository;

import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.role.Role;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicAuthService implements AuthService {
    private final UserRepository userRepository;
    private final MapStructMapper mapper;
    private final MapperMethod mapperMethod;

    private final SessionRegistry sessionRegistry;

    public UserDto roleUpdate(UUID userId, Role role){
        // update query
        User user = userRepository.findById(userId)
                .orElseThrow(RuntimeException::new);

        user.updateRole(role);

        userRepository.save(user);

        // find after update
        UserProjection projection = userRepository.getUserFromId(userId)
                .orElseThrow(RuntimeException::new);

        return mapper.toDto(projection,mapper.toDto(projection,mapperMethod),userOnline(projection.username()));

    }

    private Boolean userOnline(String username){
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (
                    principal instanceof DiscodeitUserDetails details
                            && details.getUsername().equals(username)
            ){
                return true;
            }
        }
        return false;
    }
}
