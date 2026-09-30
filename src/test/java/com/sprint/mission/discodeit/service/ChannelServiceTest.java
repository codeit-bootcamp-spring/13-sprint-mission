package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.projection.ChannelProjection;
import com.sprint.mission.discodeit.dto.projection.UserProjection;
import com.sprint.mission.discodeit.dto.request.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.ChannelTypeException;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.security.SessionService;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
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
import org.springframework.data.domain.*;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Channel Service Test")
public class ChannelServiceTest {
    @Mock ChannelRepository channelRepository;
    @Mock ReadStatusRepository readStatusRepository;
    @Mock UserRepository userRepository;
    @Mock MessageRepository messageRepository;
    @Mock
    BinaryContentRepository binaryContentRepository;
    @Mock
    SessionService sessionService;
    @Mock
    RoleHierarchy roleHierarchy;

    @Spy
    MapStructMapper mapStructMapper = Mappers.getMapper(MapStructMapper.class);

    @InjectMocks
    BasicChannelService channelService;

    private Authentication auth;

    private Channel publicChannel(){
        Channel channel = new Channel("public","dsc", ChannelType.PUBLIC);

        ReflectionTestUtils.setField(channel, "id", UUID.randomUUID());

        return channel;
    }

    private Channel privateChannel(){
        Channel channel = new Channel("public","dsc", ChannelType.PRIVATE);

        ReflectionTestUtils.setField(channel, "id", UUID.randomUUID());

        return channel;
    }

    private User user(){
        User user = new User("ksk","ksk@email.com",null,null,null);

        ReflectionTestUtils.setField(user,"id", UUID.randomUUID());

        return user;
    }

    private void setAuthority(){
        auth = new UsernamePasswordAuthenticationToken(
                "test-user",
                null,
                List.of(
                        new SimpleGrantedAuthority("CHANNEL_MANAGER")
                )
        );

        given(roleHierarchy.getReachableGrantedAuthorities(anyCollection()))
                .willAnswer( a -> {
                    return a.getArgument(0);
                });

    }

    private void givenMethodGetChannelDtoFromChannel(Channel channel, List<User> users){
        given(channelRepository.getChannelById(any(UUID.class)))
                .willReturn(Optional.of(new ChannelProjection(
                        channel.getId(),
                        channel.getType(),
                        channel.getName(),
                        channel.getDescription(),
                        users.stream().map(User::getId).toList(),
                        null
                )));


        given(readStatusRepository.findUserIdsByChannelId(any()))
                .willReturn(users.stream().map(User::getId).toList());


        given(userRepository.getUsersFromIds(anyList()))
                .willReturn(
                        users.stream().collect(
                                Collectors.toMap(
                                        BaseEntity::getId,
                                        u -> new UserProjection(
                                                u.getId(),
                                                u.getUsername(),
                                                u.getEmail(),
                                                u.getPassword(),
                                                u.getRole(),
                                                null
                                        )

                                )
                        )
                );

        // no profile or files return
        given(binaryContentRepository.getBinaryContentsInIdList(anyList()))
                .willReturn(Map.of());

        given(sessionService.userOnline(any())).willReturn(false);
    }


    @Nested
    class CreateTest{



        @Test
        @DisplayName("create Public Channel success")
        void publicSuccess() {
            // given
            PublicChannelCreateRequest request = new PublicChannelCreateRequest("public","dsc");
            // when
            given(channelRepository.save(any(Channel.class))).willAnswer(i -> privateChannel());

            givenMethodGetChannelDtoFromChannel(
                    privateChannel(),
                    List.of(user())
            );

            channelService.createPublicChannel(request);

            // then
            ArgumentCaptor<Channel> captor = ArgumentCaptor.forClass(Channel.class);

            verify(channelRepository).save(captor.capture());

            Channel saved = captor.getValue();

            assertThat(saved.getName()).isEqualTo(request.name());
            assertThat(saved.getDescription()).isEqualTo(request.description());
            assertThat(saved.getType()).isEqualTo(ChannelType.PUBLIC);
        }

        @Test
        @DisplayName("create Private Channel success")
        void privateSuccess() {
            // given
            PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(new ArrayList<>());
            givenMethodGetChannelDtoFromChannel(
                    publicChannel(),
                    List.of(user())
            );

            // when
            given(channelRepository.save(any(Channel.class))).willAnswer(i -> privateChannel());
            channelService.createPrivateChannel(request);
            // then
            ArgumentCaptor<Channel> captor = ArgumentCaptor.forClass(Channel.class);
            verify(channelRepository).save(captor.capture());

            Channel saved = captor.getValue();
            assertThat(saved.getType()).isEqualTo(ChannelType.PRIVATE);
        }
    }


    @Nested
    class UpdateTest{
        @Test
        @DisplayName("update Channel success")
        void success() {
            setAuthority();
            UUID id = UUID.randomUUID();
            Channel channel = publicChannel();
            PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("public","modified");


            givenMethodGetChannelDtoFromChannel(
                    publicChannel(),
                    List.of(user())
            );

            given(channelRepository.findById(id)).willReturn(Optional.of(channel));

            channelService.update(id,request,auth);

            assertThat(channel.getDescription()).isEqualTo("modified");
        }

        @Test
        @DisplayName("update private Channel fail ")
        void fail() {
            // given
            UUID id = UUID.randomUUID();
            Channel channel = privateChannel();
            PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("public","modified");
            // when
            given(channelRepository.findById(id)).willReturn(Optional.of(channel));
            // then
            assertThatThrownBy(() -> channelService.update(id,request,any())).isInstanceOf(ChannelTypeException.class);

        }
    }


    @Nested
    class DeleteTest{

        @Test
        @DisplayName("delete Channel success")
        void success() {
            setAuthority();
            UUID id = UUID.randomUUID();
            Channel channel = publicChannel();
            given(channelRepository.findById(id)).willReturn(Optional.of(channel));
            channelService.deleteChannel(id,auth);
            verify(channelRepository).deleteById(id);
        }


        @Test
        @DisplayName("delete private Channel fail ")
        void fail() {
            UUID id = UUID.randomUUID();
            given(channelRepository.findById(id)).willReturn(Optional.empty());
            assertThatThrownBy(() ->  channelService.deleteChannel(id,auth))
                    .isInstanceOf(ChannelNotFoundException.class);
        }
    }

}
