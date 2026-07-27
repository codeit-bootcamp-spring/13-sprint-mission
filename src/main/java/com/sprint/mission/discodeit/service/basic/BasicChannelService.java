package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.*;
import com.sprint.mission.discodeit.dto.command.channel.ChannelUpdateCommand;
import com.sprint.mission.discodeit.dto.command.channel.PrivateChannelCommand;
import com.sprint.mission.discodeit.dto.command.channel.PublicChannelCommand;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelMapper channelMapper;

    //PUBLIC 채널 생성
    @Override
    public ChannelDto createPublicChannel(PublicChannelCommand command) {
        if (channelRepository.existsByName(command.name())){
            log.warn("채널 생성 실패 - 중복된 채널명 : {}", command.name());
            throw new IllegalArgumentException("동일한 채널명이 존재 합니다.");
        }
        Channel channel = new Channel(ChannelType.PUBLIC, command.name(), command.description());
        channelRepository.save(channel);
        log.info("PUBLIC 채널 생성 - 채널명: {}, 채널설명: {}", channel.getName(), channel.getDescription());
        return channelMapper.toDto(channel);
    }

    //PRIVATE 채널 생성
    @Override
    public ChannelDto createPrivateChannel(PrivateChannelCommand command) {
        Channel channel = new Channel(ChannelType.PRIVATE, null, null);
        channelRepository.save(channel);

        command.participantIds().forEach(userId -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(()-> new NoSuchElementException("존재하지 않는 사용자 입니다."));
            ReadStatus readStatus = new ReadStatus(user ,channel);
            readStatusRepository.save(readStatus);
        });

        log.info("PRIVATE 채널 생성 - 채널 참여자: {}", command.participantIds());
        return channelMapper.toDto(channel);
    }

    @Transactional(readOnly = true)
    @Override
    public ChannelDto findByChannelId(UUID channelId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 채널입니다."));
        log.debug("채널 조회 - 채널명: {}",channel.getName());

        return channelMapper.toDto(channel);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ChannelDto> findAllByUserId(UUID userId) {
        List<Channel> publicChannels = channelRepository.findAll()
                .stream()
                .filter(channel -> channel.getType().equals(ChannelType.PUBLIC))
                .collect(Collectors.toList());

        List<Channel> privateChannels = readStatusRepository.findAllByUserId(userId)
                .stream()
                .map(ReadStatus::getChannel)
                .filter(channel -> channel.getType().equals(ChannelType.PRIVATE))
                .collect(Collectors.toList());

        List<Channel> allChannels = new ArrayList<>();
        allChannels.addAll(publicChannels);
        allChannels.addAll(privateChannels);

        log.debug("전체 채널 조회 완료: {}", allChannels.size());

        return allChannels.stream()
                .map(channelMapper::toDto)
                .collect(Collectors.toList());

    }

    @Override
    public ChannelDto updateChannel(UUID ChannelId, ChannelUpdateCommand command) {
        Channel channel = channelRepository.findById(ChannelId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 채널 입니다."));
        if (channel.getType().equals(ChannelType.PRIVATE)) {
            log.warn("채널 수정 실패 - 채널 타입: {}",channel.getType());
            throw new IllegalArgumentException("비공개 채널은 수정할 수 없습니다.");
        }
        if (command.name() != null && !command.name().isBlank()) channel.updateChannel(command.name());
        if (command.description() != null && !command.description().isBlank()) channel.updateChannelDescription(command.description());


        channelRepository.save(channel);

        log.info("채널 수정 완료- 채널id: {}, 채널명: {} ,채널설명: {}", channel.getId(), channel.getName(), channel.getDescription());
        return channelMapper.toDto(channel);
    }

    @Override
    public void deleteChannel(UUID ChannelId) {
        Channel channel = channelRepository.findById(ChannelId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 채널입니다."));

        channelRepository.deleteById(channel.getId());
        log.info("채널 삭제 완료 - 채널id: {}, 채널명: {}", channel.getId(), channel.getName());
    }
}
