package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Primary
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public MessageResponse create(MessageCreateRequest request) {
        if (userRepository.findById(request.userId()) == null) {
            throw new IllegalArgumentException("유저를 찾을 수 없습니다.");
        }
        if (channelRepository.findById(request.channelId()) == null) {
            throw new IllegalArgumentException("채널을 찾을 수 없습니다.");
        }

        Message message = new Message(request.userId(), request.channelId(), request.content());
        messageRepository.save(message);

        if (request.binaryContentIds() != null && !request.binaryContentIds().isEmpty()) {
            for (UUID binaryId : request.binaryContentIds()) {
                BinaryContent binaryContent = binaryContentRepository.findById(binaryId);
                if (binaryContent != null) {
                    binaryContent.updateMessageId(message.getId());
                    binaryContentRepository.save(binaryContent);
                }
            }
        }

        return convertToResponse(message);

    }

    @Override
    public List<MessageResponse> findAllByChannelId(UUID channelId) {
        return messageRepository.findAll().stream()
                .filter(m -> m.getChannelId().equals(channelId))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MessageResponse update(MessageUpdateRequest request) {

        Message message = messageRepository.findById(request.id());

        if (message == null) {
            throw new NoSuchElementException("존재하지 않는 메세지입니다.");
        }

        message.updateContent(request.content());
        messageRepository.save(message);

        return convertToResponse(message);

    }

    @Override
    public void delete(UUID messageId) {

        List<BinaryContent> attachments = binaryContentRepository.findAll().stream()
                .filter(b -> messageId.equals(b.getMessageId()))
                .toList();

        for (BinaryContent binaryContent : attachments) {
            binaryContentRepository.delete(binaryContent.getId());
        }

        messageRepository.delete(messageId);
    }

    private MessageResponse convertToResponse(Message message) {
        List<UUID> binaryContentIds = binaryContentRepository.findAll().stream()
                .filter(b -> message.getId().equals(b.getMessageId()))
                .map(BinaryContent::getId)
                .collect(Collectors.toList());

        return new MessageResponse(
                message.getId(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getAuthorId(),
                message.getChannelId(),
                message.getContent(),
                binaryContentIds
        );
    }

}
