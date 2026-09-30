package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.UserDuplicatedException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.SessionService;
import com.sprint.mission.discodeit.security.role.Role;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

    @ExtendWith(MockitoExtension.class)
    @DisplayName("User Service Test")
public class UserServiceTest {
    @Mock
    UserRepository userRepository;
    @Mock
    BinaryContentRepository binaryContentRepository;
    @Mock
    UserCreateRequest userCreateRequest;
    @Spy
    MapStructMapper mapStructMapper = Mappers.getMapper(MapStructMapper.class);
    @Spy
    PasswordEncoder passwordEncoder;
    @InjectMocks
    BasicUserService userService;
    @Mock
    SessionService sessionService;

    @Nested
    class Create{

        @Test
        @DisplayName("userCreate")
        void success() {
            // given
            UserCreateRequest req = new UserCreateRequest("김숙키", "ksik@email.com","password");
            given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

            userService.create(req,Optional.empty());
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            verify(userRepository).save(captor.capture());
            User saved = captor.getValue();

            // then
            assertThat(saved.getUsername()).isEqualTo(req.username());
            assertThat(saved.getEmail()).isEqualTo(req.email());
        }

        @Test
        @DisplayName("user create fail")
        void fail() {
            // given
            UserCreateRequest req = new UserCreateRequest("김숙희", "ksk@email.com","password");
            given(userRepository.findByUsername("김숙희"))
                    .willReturn(
                            Collections
                                    .singletonList(
                                            new User(
                                                    "김숙희",
                                                    "ksk@email.com",
                                                    "password",
                                                    null,
                                                    null
                                            )
                                    )
                    );
            assertThatThrownBy(() -> userService.create(req,Optional.empty()))
                    .isInstanceOf(UserDuplicatedException.class);

        }
    }

    @Nested
    class Update{

        User setUp() {
            return new User("김숙희", "ksk@email.com","password",null, Role.USER);
        }

        @Test
        @DisplayName("update Success")
        void success() {
            // given
            UserUpdateRequest uur = new UserUpdateRequest("최둘리","cdr@email.com","password");
            UUID id = UUID.randomUUID();
            User user = setUp();
            List<User> ls = new ArrayList<>();
            // when
            given(userRepository.findById(id)).willReturn(Optional.of(user));
            given(userRepository.findByUsername("최둘리")).willReturn(ls);
            given(userRepository.findByEmail("cdr@email.com")).willReturn(ls);

            userService.update(id,uur,Optional.empty());
            // then
            assertThat(user.getUsername()).isEqualTo("최둘리");

        }


        @Test
        @DisplayName("update Failed")
        void fail() {
            // given
            UserUpdateRequest uur = new UserUpdateRequest("최둘리","cdr@email.com","password");
            UUID id = UUID.randomUUID();
            // when
            given(userRepository.findById(id)).willReturn(Optional.empty());

            // then

            assertThatThrownBy(() -> userService.update(id,uur,Optional.empty())).isInstanceOf(UserNotFoundException.class);
        }
    }


    @Nested
    class Delete{


        @Test
        @DisplayName("delete success")
        void delete() {
            // given
            UUID id = UUID.randomUUID();
            User user = new User("김숙희", "ksk@email.com","password",null,Role.USER);
            given(userRepository.findById(id)).willReturn(Optional.of(user));

            // when
            userService.delete(id);


            // then
            verify(userRepository).delete(user);
        }

        @Test
        @DisplayName("delete fail")
        void fail() {
            // given
            UUID id = UUID.randomUUID();
            User user = new User("김숙희", "ksk@email.com","password",null,Role.USER);

            given(userRepository.findById(id)).willReturn(Optional.empty());
//            given(userStatusRepository.delete(status)).willReturn();
            // when


            // then
            assertThatThrownBy(() -> userService.delete(id)).isInstanceOf(UserNotFoundException.class);

        }


    }

}
