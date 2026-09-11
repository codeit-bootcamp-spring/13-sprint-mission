package com.sprint.mission.discodeit.security.userdetail;


import com.sprint.mission.discodeit.dto.projection.UserProjection;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.mapper.MapperMethod;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiscodeitUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final MapStructMapper mapper;

    //tmp
    private final MapperMethod mapperMethod;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // UserDto, password
        // username 을 통해 username (email) 을 가져온다.

        UserProjection projection = userRepository.getUserFromUsername(username)
                .orElseThrow(RuntimeException::new);

        UserDto dto = mapper.toDto(projection,mapper.toDto(projection,mapperMethod));

        return new DiscodeitUserDetails(dto, projection.password());
    }
}
