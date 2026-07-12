package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final MessageMapper messageMapper;
    private final PageResponseMapper pageResponseMapper;

    @Override
    @Transactional
    public MessageDto create(MessageCreateRequest request) {
        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널의 메시지입니다."));

        User author = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저의 메시지입니다."));

        if ((request.content() == null || request.content().isBlank())
                && (request.attachments() == null || request.attachments().isEmpty())) {
            throw new IllegalArgumentException("메시지 내용 또는 첨부파일이 필요합니다.");
        }

        List<BinaryContent> attachments = new ArrayList<>();

        if (request.attachments() != null) {
            for (BinaryContentCreateRequest attachmentRequest : request.attachments()) {
                BinaryContent attachment = new BinaryContent(
                        attachmentRequest.fileName(),
                        attachmentRequest.contentType(),
                        (long) attachmentRequest.bytes().length
                );

                binaryContentRepository.save(attachment);
                attachments.add(attachment);
            }
        }

        Message message = new Message(
                request.content(),
                author,
                channel,
                attachments
        );

        messageRepository.save(message);
        return messageMapper.toDto(message);
    }



    @Override
    public MessageDto findById(UUID id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 메세지 입니다."));

        return messageMapper.toDto(message);
    }

    @Override
    public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant cursor, Pageable pageable) {
        Slice<Message> messages = cursor == null
                ? messageRepository.findAllByChannel_Id(channelId, pageable)
                : messageRepository.findAllByChannel_IdAndCreatedAtLessThan(channelId, cursor, pageable);

        Slice<MessageDto> messageDtos = messages.map(messageMapper::toDto);

        return pageResponseMapper.fromSlice(messageDtos, MessageDto::createdAt);
    }


    @Override
    @Transactional
    public MessageDto update(UUID messageId, MessageUpdateRequest request) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 메세지 입니다."));

        message.update(request.content());
        messageRepository.save(message);
        return messageMapper.toDto(message);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 메세지 입니다."));

        for (BinaryContent attachment : message.getAttachments()) {
            binaryContentRepository.delete(attachment);
        }

        messageRepository.delete(message);
    }

}
