package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.ChannelTypeException;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.mapper.MapperMethod;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Service Test")
public class ChannelServiceTest {
    @Mock ChannelRepository channelRepository;
    @Mock ReadStatusRepository readStatusRepository;
    @Mock UserRepository userRepository;
    @Mock MessageRepository messageRepository;
    @Mock MapperMethod mapperMethod;
    @Mock MapStructMapper mapStructMapper;
    @InjectMocks
    BasicChannelService channelService;

    private Channel publicChannel(){
        return new Channel("public","dsc", ChannelType.PUBLIC);
    }

    private Channel privateChannel(){
        return new Channel("public","dsc", ChannelType.PRIVATE);
    }


    @Nested
    class CreateTest{
        @Test
        @DisplayName("create Public Channel success")
        void publicSuccess() {
            // given
            PublicChannelCreateRequest request = new PublicChannelCreateRequest("public","dsc");
            // when
            Slice<Message> emptySlice =
                    new SliceImpl<>(Collections.emptyList());

            given(messageRepository.findByChannelIdOrderByCreatedAtDesc(
                    nullable(UUID.class),
                    any(Pageable.class)))
                    .willReturn(emptySlice);
            given(channelRepository.save(any(Channel.class))).willAnswer(i -> privateChannel());
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
            UUID id = UUID.randomUUID();
            PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(new ArrayList<>());
            Slice<Message> emptySlice =
                    new SliceImpl<>(Collections.emptyList());

            given(messageRepository.findByChannelIdOrderByCreatedAtDesc(
                    nullable(UUID.class),
                    any(Pageable.class)))
                    .willReturn(emptySlice);
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
            UUID id = UUID.randomUUID();
            Channel channel = publicChannel();
            PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("public","modified");
            Slice<Message> emptySlice =
                    new SliceImpl<>(Collections.emptyList());

            given(messageRepository.findByChannelIdOrderByCreatedAtDesc(
                    nullable(UUID.class),
                    any(Pageable.class)))
                    .willReturn(emptySlice);
            given(channelRepository.findById(id)).willReturn(Optional.of(channel));

            channelService.update(id,request);

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
            assertThatThrownBy(() -> channelService.update(id,request)).isInstanceOf(ChannelTypeException.class);

        }
    }


    @Nested
    class DeleteTest{
        @Test
        @DisplayName("delete Channel success")
        void success() {
            UUID id = UUID.randomUUID();
            Channel channel = publicChannel();
            given(channelRepository.findById(id)).willReturn(Optional.of(channel));
            channelService.deleteChannel(id);
            verify(channelRepository).deleteById(id);
        }

        @Test
        @DisplayName("delete private Channel fail ")
        void fail() {
            UUID id = UUID.randomUUID();
            given(channelRepository.findById(id)).willReturn(Optional.empty());
            assertThatThrownBy(() ->  channelService.deleteChannel(id)).isInstanceOf(ChannelNotFoundException.class);
        }
    }


    // Todo - 서비스 로직이 readstatus 에서 채널을 조회하는 것 이라 테스트가 난잡함.
    // Todo - 로직 개선과 함꼐 테스트도 작성.
//    @Nested
//    class FindTest{
//        @Test
//        @DisplayName("find Channel success")
//        void success() {
//            UUID id = UUID.randomUUID();
//
//            Channel channel = publicChannel();
//            ReadStatus readStatus = new ReadStatus(null,channel, Instant.now());
//            Slice<Message> emptySlice =
//                    new SliceImpl<>(Collections.emptyList());
//
//            given(messageRepository.findByChannelIdOrderByCreatedAtDesc(
//                    nullable(UUID.class),
//                    any(Pageable.class)))
//                    .willReturn(emptySlice);
//            given(readStatusRepository.findWithDetailByUserId(id)).willReturn(List.of());
//            given(readStatusRepository.findWithDetailByChannelType(ChannelType.PUBLIC)).willReturn(List.of(readStatus));
//            given(mapStructMapper.toDto(any(Channel.class),nullable(List.class),nullable(Instant.class)))
//                    .willReturn(channelDto(id,channel));
//
//
//
//            assertThat(channelService.findAllByUserID(id)).isSameAs(List.of(channelDto(id, channel)));
//
//        }
//    }

    private ChannelDto channelDto (UUID id, Channel channel){
        return new ChannelDto(id,channel.getType(),channel.getName(),channel.getDescription(),List.of(),Instant.now());
    }
}
