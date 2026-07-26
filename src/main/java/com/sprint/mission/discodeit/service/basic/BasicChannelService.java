package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.ChannelFindResponse;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicChannelService implements ChannelService {

    //필드
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ReadStatusRepository readStatusRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final ChannelMapper channelMapper;

    //interface
    @Override
    @Transactional
    public ChannelDto createPrivateChannel(PrivateChannelCreateRequest request) {
        log.debug("Private 채널 생성 시작");

        //채널 생성
        Channel channel = new Channel(ChannelType.PRIVATE);
        channel = channelRepository.save(channel);
        log.info("채널: {}가 생성됨.", channel.getName());

        //ReadStatus 생성
        for (UUID userId : request.participantIds()) {
            // 유저 검색
            User userTemp = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));

            ReadStatus readStatus = new ReadStatus(userTemp, channel);
            readStatus = readStatusRepository.save(readStatus);
            log.info("ReadStatus가 생성됨.");
        }

        log.info("Private 채널 생성 완료");

        return channelMapper.toDto(channel);
    }

    @Override
    @Transactional
    public ChannelDto createPublicChannel(PublicChannelCreateRequest request) {
        log.debug("Public 채널 생성 시작");

        //채널 생성
        Channel channel = new Channel(ChannelType.PUBLIC, request.name(), request.description());
        channel = channelRepository.save(channel);
        log.info("채널: {}가 생성됨.", channel.getName());

        log.info("Public 채널 생성 완료");

        return channelMapper.toDto(channel);
    }

    @Override
    @Transactional(readOnly = true)
    public ChannelDto getChannel(UUID channelId) {
        //채널 검색
        Channel channelTemp = channelRepository.findById(channelId)
                .orElseThrow(() -> new ChannelNotFoundException(channelId));

        return channelMapper.toDto(channelTemp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelDto> getChannelsByUserId(UUID userId) {
        //유저 검증
        validateUserExists(userId);

        //반환할 리스트
        List<Channel> channelFindList = new ArrayList<>();

        //해당 유저가 참가해있는 readStatus들 검색
        List<ReadStatus> readStatuses = readStatusRepository.findAllByUserId(userId);

        //ChannelType이 PRIVATE인 것만 진행
        for (ReadStatus readStatus : readStatuses) {
            //채널 검색
            Channel channelTemp = channelRepository.findById(readStatus.getChannel().getId())
                    .orElseThrow(() -> new ChannelNotFoundException(readStatus.getChannel().getId()));

            if (channelTemp.getType() == ChannelType.PUBLIC)
                continue;

            channelFindList.add(channelTemp);
        }

        //ChannelType이 PUBLIC인 것만 진행
        List<Channel> publicChannelListTemp = channelRepository.findAllByType(ChannelType.PUBLIC);
        channelFindList.addAll(publicChannelListTemp);

        return channelFindList.stream()
                .map(channelMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ChannelDto updateChannel(UUID channelId, PublicChannelUpdateRequest request) {
        log.debug("채널 수정 시작");

        //채널 검색
        Channel channelTemp = channelRepository.findById(channelId)
                .orElseThrow(() -> {
                    log.warn("채널 수정 실패: 해당 채널은 데이터파일에 존재하지 않습니다.");
                    return new ChannelNotFoundException(channelId);
                });

        if (channelTemp.getType() != ChannelType.PUBLIC) {
            log.warn("수정하려는 채널: {}이 Public 채널이 아닙니다.", channelTemp.getName());
            throw new PrivateChannelUpdateException(channelId);
        }

        //채널 업데이트
        channelTemp.updateChannel(request.newName(), request.newDescription());

        log.info("채널 수정 완료");

        return channelMapper.toDto(channelTemp);
    }

    @Override
    @Transactional
    public void deleteChannel(UUID channelId) {
        log.debug("채널 삭제 시작");

        //채널 검색
        Channel channelTemp = channelRepository.findById(channelId)
                .orElseThrow(() -> {
                    log.warn("채널 삭제 실패: 해당 채널은 데이터파일에 존재하지 않습니다.");
                    return new ChannelNotFoundException(channelId);
                });

        //채널 내 메시지 삭제
        List<Message> messageList = messageRepository.findAllByChannelId(channelId);
        for (Message message : messageList) {
            for (BinaryContent attachment : message.getAttachments()) {
                binaryContentRepository.deleteById(attachment.getId());
            }
            messageRepository.deleteById(message.getId());
        }

        //채널 참조하는 ReadStatus 삭제
        readStatusRepository.deleteAllByChannelId(channelTemp.getId());

        //채널 삭제
        channelRepository.deleteById(channelTemp.getId());

        log.info("채널: {}가 삭제됨.", channelTemp.getName());

        log.info("채널 삭제 완료");
    }


    //ChannelFindResponse DTO를 만들어서 반환해주는 메서드
    private ChannelFindResponse makeChannelFindResponse(Channel channel) {
        //가장 최근 메시지 검색
        List<Message> messageListTemp = messageRepository.findAllByChannelId(channel.getId());
        Message recentMessage = messageListTemp.stream()
                .max(Comparator.comparing(Message::getCreatedAt))
                .orElse(null);

        //해당 채널에 참여하고 있는 userId들 추출
        List<ReadStatus> readStatusListTemp = readStatusRepository.findAllByChannelId(channel.getId());
        List<UUID> usersId = readStatusListTemp.stream()
                .map(readStatus -> readStatus.getUser().getId())
                .toList();

        //가장 최근 메시지가 존재하면 해당 메시지의 시간 정보, 존재하지 않으면 Instant 기본값으로 DTO 생성
        //채널 타입이 PRIVATE이면 usersId를 넣고, 아니면 null을 넣도록 구현
        return ChannelFindResponse.from(channel, (recentMessage != null) ? recentMessage.getCreatedAt() : Instant.EPOCH, (channel.getType() == ChannelType.PRIVATE) ? usersId : new ArrayList<>());
    }


    // 들어온 userId 필드가 레포지터리에 존재하는지 검증하는 메서드
    private void validateUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            log.warn("유저: {}이 존재하지 않습니다.", userId);
            throw new UserNotFoundException(userId);
        }
    }
}
