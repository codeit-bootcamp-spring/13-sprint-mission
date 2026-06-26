package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public MessageResponse create(MessageCreateRequest request) {
        Channel channel = channelRepository.findById(request.channelId());
        User author = userRepository.findById(request.userId());

        if (request.content() == null) {
            throw new IllegalArgumentException("존재하지 않은 내용입니다.");
        }
        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널의 메시지입니다.");
        }
        if (author == null) {
            throw new IllegalArgumentException("존재하지 않는 유저의 메시지입니다.");
        }

        List<UUID> attachmentIds = new ArrayList<>();

        if (request.attachments() != null) {
            for (BinaryContentCreateRequest attachmentRequest : request.attachments()) {
                BinaryContent attachment = new BinaryContent(
                        attachmentRequest.fileName(),
                        attachmentRequest.contentType(),
                        attachmentRequest.bytes()
                );

                binaryContentRepository.save(attachment);
                attachmentIds.add(attachment.getId());
            }
        }

        Message message = new Message(
                request.content(),
                author,
                channel,
                attachmentIds);

        messageRepository.save(message);
        return toResponse(message) ;
    }



    @Override
    public MessageResponse findById(UUID id) {
        Message message = messageRepository.findById(id);
        if (message == null) {
            throw new IllegalArgumentException("존재하지 않은 메세지 입니다.") ;
        }
        return toResponse(message);
    }

    @Override
    public Collection<MessageResponse> findAllByChannelId(UUID channelId) {
        return messageRepository.findAllByChannelId(channelId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public MessageResponse update(MessageUpdateRequest request) {
        if (request.content() == null) {
            throw new IllegalArgumentException("존재하지 않은 메세지 입니다.");
        }

        Message message = messageRepository.findById(request.id());
        message.update(request.content());
        messageRepository.save(message);
        return toResponse(message);
    }

    @Override
    public void delete(UUID id) {
        Message message = messageRepository.findById(id);

        if (message == null) {
            throw new IllegalArgumentException("존재하지 않은 메세지 입니다.");
        }

        if (message.getAttachmentIds() != null) {
            for (UUID attachmentId : message.getAttachmentIds()) {
                binaryContentRepository.delete(attachmentId);
            }
        }

        messageRepository.delete(id);
    }

    private MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getContent(),
                message.getChannel().getId(),
                message.getAuthor().getId(),
                message.getAttachmentIds()
        );
    }
}
