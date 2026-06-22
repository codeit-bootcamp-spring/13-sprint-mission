package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.CreateBinaryContentRequest;
import com.sprint.mission.discodeit.dto.request.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.request.UpdateMessageRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentResponse;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public MessageResponse create(CreateMessageRequest request, List<CreateBinaryContentRequest> attachments) {
        Message message = new Message(request.content(), request.channelId(), request.authorId());

        messageRepository.save(message);

        for (CreateBinaryContentRequest file : attachments) {
            BinaryContent binaryContent = new BinaryContent(
                            request.authorId(),
                            message.getId(),
                            file.filename(),
                            file.contentType(),
                            file.bytes()
            );
            binaryContentRepository.save(binaryContent);
        }

        List<BinaryContentResponse> attachmentResponses = binaryContentRepository
                        .findAllByMessageId(message.getId())
                        .stream()
                        .map(BinaryContentResponse::from)
                        .toList();

        return MessageResponse.from(
                message,
                attachmentResponses
        );
    }

    @Override
    public MessageResponse find(UUID id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("메시지를 찾을 수 없습니다."));

        List<BinaryContentResponse> attachments = binaryContentRepository.findAllByMessageId(message.getId())
                        .stream()
                        .map(BinaryContentResponse::from)
                        .toList();

        return MessageResponse.from(
                message,
                attachments
        );
    }

    @Override
    public List<MessageResponse> findAllByChannelId(UUID channelId) {
        return messageRepository.findAllByChannelId(channelId)
                .stream()
                .map(message -> {
                    List<BinaryContentResponse> attachments =
                            binaryContentRepository
                                    .findAllByMessageId(message.getId())
                                    .stream()
                                    .map(BinaryContentResponse::from)
                                    .toList();

                    return MessageResponse.from(
                            message,
                            attachments
                    );
                })
                .toList();
    }

    @Override
    public MessageResponse update(UpdateMessageRequest request) {
        Message message = messageRepository.findById(request.id())
                .orElseThrow(() ->
                    new IllegalArgumentException("메시지를 찾을 수 없습니다."));
        message.update(request.content());

        messageRepository.save(message);

        List<BinaryContentResponse> attachments = binaryContentRepository
                        .findAllByMessageId(message.getId())
                        .stream()
                        .map(BinaryContentResponse::from)
                        .toList();

        return MessageResponse.from(
                message,
                attachments
        );
    }

    @Override
    public void delete(UUID id) {
        List<BinaryContent> attachments = binaryContentRepository.findAllByMessageId(id);

        for (BinaryContent attachment : attachments) {
            binaryContentRepository.delete(attachment.getId());
        }

        messageRepository.delete(id);
    }

}
