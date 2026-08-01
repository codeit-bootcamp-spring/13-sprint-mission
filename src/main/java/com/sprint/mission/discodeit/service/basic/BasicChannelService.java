package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicChannelService implements ChannelService {

    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final ChannelMapper channelMapper;

    @Override
    @Transactional
    public ChannelDto createPublic(PublicChannelCreateRequest request) {
        Channel channel = new Channel(ChannelType.PUBLIC ,request.channelName(), request.description());
        channelRepository.save(channel);
        return channelMapper.toDto(channel);
    }

    @Override
    @Transactional
    public ChannelDto createPrivate(PrivateChannelCreateRequest request) {
        Channel channel = new Channel(ChannelType.PRIVATE, null, null);
        channelRepository.save(channel);

        for (UUID userId : request.userIds()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(()->new IllegalArgumentException("존재하지 않는 계정입니다."));

            ReadStatus readStatus = new ReadStatus(user, channel);
            readStatusRepository.save(readStatus);
        }

        return channelMapper.toDto(channel);
    }

    @Override
    public ChannelDto find(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 채널입니다."));

        return channelMapper.toDto(channel);
    }

    @Override
    public List<ChannelDto> findAllByUserId(UUID userId) {
        List<ChannelDto> responses = new ArrayList<>();
        List<Channel> channels = channelRepository.findAll();

        Set<UUID> joinedChannelIds = readStatusRepository.findAllByUser_Id(userId).stream()
                .map(readStatus -> readStatus.getChannel().getId())
                .collect(Collectors.toSet());

        for (Channel channel : channels) {
            boolean isJoin = channel.getType() == ChannelType.PUBLIC
                    || joinedChannelIds.contains(channel.getId());

            if(isJoin) {
                responses.add(channelMapper.toDto(channel));
            }
        }

        return responses;
    }

    @Override
    @Transactional
    public ChannelDto update(UUID channelId, ChannelUpdateRequest request) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 채널입니다."));
        if(channel.getType()==ChannelType.PRIVATE) {
            throw new IllegalArgumentException("비공개 채널은 수정할 수 없습니다.");
        }

        channel.update(request.channelName(), request.description());
        channelRepository.save(channel);

        return channelMapper.toDto(channel);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 채널입니다."));

        channelRepository.delete(channel);
    }
}
