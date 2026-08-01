package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.UpdateMessageRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

    private static final Logger log =
            LoggerFactory.getLogger(BasicMessageService.class);

    private static final int PAGE_SIZE = 50;

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final MessageMapper messageMapper;
    private final PageResponseMapper pageResponseMapper;

    @Override
    @Transactional
    public MessageDto create(CreateMessageRequest request) {
        log.debug(
                "메시지 생성 요청: userId={}, channelId={}",
                request.getUserId(),
                request.getChannelId()
        );

        User author = userRepository.findById(request.getUserId())
                .orElseThrow(() -> {
                    log.warn(
                            "메시지 생성 실패 - 사용자 없음: userId={}",
                            request.getUserId()
                    );
                    return new UserNotFoundException(request.getUserId());
                });

        Channel channel = channelRepository.findById(request.getChannelId())
                .orElseThrow(() -> {
                    log.warn(
                            "메시지 생성 실패 - 채널 없음: channelId={}",
                            request.getChannelId()
                    );
                    return new ChannelNotFoundException(request.getChannelId());
                });

        Message message = new Message(
                request.getContent(),
                channel,
                author,
                List.of()
        );

        Message savedMessage = messageRepository.save(message);

        log.info(
                "메시지 생성 완료: messageId={}, userId={}, channelId={}",
                savedMessage.getId(),
                author.getId(),
                channel.getId()
        );

        return messageMapper.toDto(savedMessage);
    }

    @Override
    public MessageDto find(UUID id) {
        log.debug("메시지 조회 요청: messageId={}", id);

        Message message = messageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "메시지 조회 실패 - 메시지 없음: messageId={}",
                            id
                    );
                    return new MessageNotFoundException(id);
                });

        log.debug("메시지 조회 완료: messageId={}", id);

        return messageMapper.toDto(message);
    }

    @Override
    public PageResponse<MessageDto> findAllByChannelId(
            UUID channelId,
            int page
    ) {
        log.debug(
                "채널별 메시지 목록 조회 요청: channelId={}, page={}",
                channelId,
                page
        );

        if (page < 0) {
            log.warn(
                    "메시지 목록 조회 실패 - 잘못된 페이지 번호: channelId={}, page={}",
                    channelId,
                    page
            );

            throw new IllegalArgumentException(
                    "페이지 번호는 0 이상이어야 합니다."
            );
        }

        if (!channelRepository.existsById(channelId)) {
            log.warn(
                    "메시지 목록 조회 실패 - 채널 없음: channelId={}",
                    channelId
            );

            throw new ChannelNotFoundException(channelId);
        }

        Pageable pageable = PageRequest.of(
                page,
                PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Slice<Message> messages =
                messageRepository.findAllByChannelId(channelId, pageable);

        PageResponse<MessageDto> response =
                pageResponseMapper.fromSlice(
                        messages,
                        messageMapper::toDto
                );

        log.debug(
                "채널별 메시지 목록 조회 완료: channelId={}, page={}, count={}",
                channelId,
                page,
                messages.getNumberOfElements()
        );

        return response;
    }

    @Override
    @Transactional
    public MessageDto update(
            UUID id,
            UpdateMessageRequest request
    ) {
        log.debug("메시지 수정 요청: messageId={}", id);

        Message message = messageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "메시지 수정 실패 - 메시지 없음: messageId={}",
                            id
                    );
                    return new MessageNotFoundException(id);
                });

        message.update(
                request.getContent(),
                message.getAttachments()
        );

        log.info("메시지 수정 완료: messageId={}", id);

        return messageMapper.toDto(message);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.debug("메시지 삭제 요청: messageId={}", id);

        Message message = messageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "메시지 삭제 실패 - 메시지 없음: messageId={}",
                            id
                    );
                    return new MessageNotFoundException(id);
                });

        messageRepository.delete(message);

        log.info("메시지 삭제 완료: messageId={}", id);
    }
}