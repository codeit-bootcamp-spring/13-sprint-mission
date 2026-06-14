package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ChannelPrivateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelPublicRequest;
import com.sprint.mission.discodeit.dto.request.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;

//    @Override
//    public Channel create (Channel channel) {
//        return channelRepository.create(channel);
//    }

    @Override
    public ChannelResponse createPrivateChannel (ChannelPrivateRequest dto) {
        // [요구사항] PRIVATE 채널 생성 시 name과 description 속성은 생략
        Channel channel = new Channel("PRIVATE_CHANNEL", "비공개 채널", true);
        if (dto.channelIds() != null) {
            channel.assignUsers(dto.channelIds());
        }
        channelRepository.create(channel);
        // [요구사항] 채널에 참여하는 User의 정보를 받아 User별 ReadStatus 정보를 생성
        if (dto.channelIds() != null) {
            for (UUID targetUserId : dto.channelIds()) {
                ReadStatus readStatus = new ReadStatus(channel.getId(), targetUserId);
                readStatusRepository.create(readStatus);
            }
        }
        return new ChannelResponse(channel.getId(), channel.getChannelTitles(), channel.getDescription());
    }

    @Override
    public ChannelResponse createPublicChannel (ChannelPublicRequest dto) {
        // [요구사항] PUBLIC 채널을 생성할 때에는 기존 로직(이름, 설명 반영)을 유지
        Channel channel = new Channel(dto.name(), dto.description());
        channelRepository.create(channel);
        return new ChannelResponse(channel.getId(), channel.getChannelTitles(), channel.getDescription());
    }

    @Override
    public Optional<ChannelResponse> findById(UUID id) {
        Channel channel = channelRepository.findById(id);
        return Optional.ofNullable(channel)
                // [요구사항] DTO를 활용하여 해당 채널의 가장 최근 메시지의 시간 정보를 포함
                .map(c -> new ChannelResponse
                        (c.getId(),
                         c.getChannelTitles(),
                         c.getDescription()));
    }

    @Override
    public List<ChannelResponse> findAll(UUID userId){
        // [요구사항] 특정 User가 볼 수 있는 Channel 목록을 조회하도록 조회 조건 추가
        return channelRepository.findAll().stream()
                .filter(channel -> {
                    // [요구사항] PUBLIC 채널 목록은 전체 조회
                    if (!channel.isPrivate()) {
                        return true;
                    }
                    // [요구사항] PRIVATE 채널은 조회한 User가 참여한 채널만 조회
                    return channel.getUserIds() != null && channel.getUserIds().contains(userId);
                })
                .map(channel -> new ChannelResponse(channel.getId(), channel.getChannelTitles(), channel.getDescription()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId){
        // [요구사항] 특정 User가 볼 수 있는 Channel 목록을 조회하도록 조회 조건 추가
        return channelRepository.findAll().stream()
                .filter(channel -> {
                    // [요구사항] PUBLIC 채널 목록은 전체 조회
                    if (!channel.isPrivate()) {
                        return true;
                    }
                    // [요구사항] PRIVATE 채널은 조회한 User가 참여한 채널만 조회
                    return channel.getUserIds() != null && channel.getUserIds().contains(userId);
                })
                .map(channel -> new ChannelResponse(channel.getId(), channel.getChannelTitles(), channel.getDescription()))
                .collect(Collectors.toList());
    }

    @Override
    public ChannelResponse update(UUID uuid, ChannelPublicRequest dto) {
        Channel channel = channelRepository.findById(uuid);
        if (channel == null) {
            throw new IllegalArgumentException("해당 채널을 찾을 수 없습니다.");
        }

        // [요구사항] PRIVATE 채널은 수정할 수 없음
        if (channel.isPrivate()) {
            throw new IllegalArgumentException("PRIVATE 채널은 수정할 수 없습니다.");
        }

        // [요구사항] DTO를 활용해 파라미터를 그룹화하여 기존 수평 구조 유지
        channel.updateTitles(new Channel(dto.name(), dto.description()));
        channelRepository.update(channel);

        return new ChannelResponse(channel.getId(), channel.getChannelTitles(), channel.getDescription());
    }

    @Override
    public void delete(UUID id) {
        // [요구사항] 관련된 도메인도 같이 삭제 (Message, ReadStatus)
        messageRepository.deleteByChannelId(id);
        readStatusRepository.deleteByChannelId(id);

        channelRepository.delete(id);
    }

}
