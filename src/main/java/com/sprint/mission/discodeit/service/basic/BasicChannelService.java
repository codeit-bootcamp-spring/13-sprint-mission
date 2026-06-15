package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelFindResponse;
import com.sprint.mission.discodeit.dto.response.ChannelUpdateResponse;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.ObjectNotFoundException;
import com.sprint.mission.discodeit.exception.WrongTypeException;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    @Override
    public Channel createPrivateChannel(PrivateChannelCreateRequest request) {
        //채널 타입 검증
        if (request.type() != ChannelType.PRIVATE)
            throw new WrongTypeException("에러: 잘못된 접근입니다.");

        //채널 생성
        Channel channel = new Channel(request.type());
        channelRepository.createChannel(channel);
        log.info("채널: {}가 생성됨.", channel.getName());

        //ReadStatus 생성
        for (UUID userId : request.userIdList()) {
            // 유저 존재하는지 검증
            validateUserExists(userId);

            ReadStatus readStatus = new ReadStatus(userId, channel.getId());
            readStatusRepository.createReadStatus(readStatus);
            log.info("ReadStatus가 생성됨.");
        }

        return channel;
    }

    //interface
    @Override
    public Channel createPublicChannel(PublicChannelCreateRequest request) {
        //채널 타입 검증
        if (request.type() != ChannelType.PUBLIC)
            throw new WrongTypeException("에러: 잘못된 접근입니다.");

        //채널 생성
        Channel channel = new Channel(request.type(), request.name(), request.description());
        channelRepository.createChannel(channel);
        log.info("채널: {}가 생성됨.", channel.getName());

        return channel;
    }

    @Override
    public ChannelFindResponse findChannel(UUID channelId) {
        //채널 검색
        Channel channelTemp = channelRepository.findChannelById(channelId)
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 채널은 데이터파일에 존재하지 않습니다."));

        return makeChannelFindResponse(channelTemp);
    }

    @Override
    public List<ChannelFindResponse> findAllByUserId(UUID userId) {
        //반환할 리스트
        List<ChannelFindResponse> channelFindResponseList = new ArrayList<>();

        //해당 유저가 참가해있는 readStatus들 검색
        List<ReadStatus> readStatuses = readStatusRepository.findAllReadStatusByUserId(userId);

        //ChannelType이 PRIVATE인 것만 진행
        for (ReadStatus readStatus : readStatuses) {
            //채널 검색
            Channel channelTemp = channelRepository.findChannelById(readStatus.getChannelId())
                    .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 채널은 데이터파일에 존재하지 않습니다."));

            if (channelTemp.getType() == ChannelType.PUBLIC)
                continue;

            channelFindResponseList.add(makeChannelFindResponse(channelTemp));
        }

        //ChannelType이 PUBLIC인 것만 진행
        List<Channel> publicChannelListTemp = channelRepository.findAllChannelsByChannelType(ChannelType.PUBLIC);
        for (Channel channelTemp : publicChannelListTemp) {
            channelFindResponseList.add(makeChannelFindResponse(channelTemp));
        }

        return channelFindResponseList;
    }

    @Override
    public ChannelUpdateResponse updateChannel(ChannelUpdateRequest request) {
        //ChannelType 검증
        if (request.type() == ChannelType.PRIVATE) {
            throw new WrongTypeException("에러: PRIVATE 채널은 수정할 수 없습니다.");
        }

        //채널 검색
        Channel channelTemp = channelRepository.findChannelById(request.channelId())
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 채널은 데이터파일에 존재하지 않습니다."));

        //채널 업데이트
        channelTemp.updateChannel(request.name(), request.description());
        channelRepository.save();

        return ChannelUpdateResponse.from(channelTemp);
    }

    @Override
    public void deleteChannel(UUID channelId) {
        //채널 검색
        Channel channelTemp = channelRepository.findChannelById(channelId)
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 채널은 데이터파일에 존재하지 않습니다."));

        //채널 내 메시지 삭제
        List<Message> messageList = messageRepository.findAllMessagesByChannelId(channelId);
        for (Message message : messageList) {
            for (UUID attachmentId : message.getAttachmentIds()) {
                binaryContentRepository.deleteBinaryContent(attachmentId);
            }
            messageRepository.deleteMessageById(message.getId());
        }

        //채널 참조하는 ReadStatus 삭제
        readStatusRepository.deleteReadStatusByChannelId(channelTemp.getId());

        //채널 삭제
        channelRepository.deleteChannel(channelTemp.getId());

        log.info("채널: {}가 삭제됨.", channelTemp.getName());
    }


    //ChannelFindResponse DTO를 만들어서 반환해주는 메서드
    private ChannelFindResponse makeChannelFindResponse(Channel channel) {
        //가장 최근 메시지 검색
        List<Message> messageListTemp = messageRepository.findAllMessagesByChannelId(channel.getId());
        Message recentMessage = messageListTemp.stream()
                .max(Comparator.comparing(Message::getCreatedAt))
                .orElse(null);

        //해당 채널에 참여하고 있는 userId들 추출
        List<ReadStatus> readStatusListTemp = readStatusRepository.findAllReadStatusByChannelId(channel.getId());
        List<UUID> usersId = readStatusListTemp.stream()
                .map(ReadStatus::getUserId)
                .toList();

        //가장 최근 메시지가 존재하면 해당 메시지의 시간 정보, 존재하지 않으면 Instant 기본값으로 DTO 생성
        //채널 타입이 PRIVATE이면 usersId를 넣고, 아니면 null을 넣도록 구현
        return ChannelFindResponse.from(channel, (recentMessage != null) ? recentMessage.getCreatedAt() : Instant.EPOCH, (channel.getType() == ChannelType.PRIVATE) ? usersId : new ArrayList<>());
    }


    // 들어온 userId 필드가 레포지터리에 존재하는지 검증하는 메서드
    private void validateUserExists(UUID userId) {
        if (!userRepository.existsUserById(userId)) {
            throw new ObjectNotFoundException("유저: " + userId + "이 존재하지 않습니다.");
        }
    }
}
