package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
//메시지에 대한 비즈니스 로직을 담당하는 service 계층
public class BasicMessageService implements MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override //메시지 생성
    public Message create(MessageCreateRequest messageCreateRequest, List<BinaryContentCreateRequest> binaryContentCreateRequests) {
        UUID channelId = messageCreateRequest.getChannelId();
        UUID authorId = messageCreateRequest.getAuthorId();

        if (!channelRepository.existsById(channelId)) {
            throw new NoSuchElementException("Channel with id "+ channelId + " does not exist");
        }
        if (!userRepository.existsById(authorId)) {
            throw new NoSuchElementException("Author with id "+ authorId + " does not exist");
        }

        List<UUID> attachmentIds = binaryContentCreateRequests.stream()
                .map(attachmentRequest->{
                    String fileName = attachmentRequest.getFileName();
                    String contentType = attachmentRequest.getContentType();
                    byte[] bytes = attachmentRequest.getBytes();

                    BinaryContent binaryContent = new BinaryContent(fileName, contentType, bytes);
                    BinaryContent createdBinaryContent = binaryContentRepository.save(binaryContent);
                    return createdBinaryContent.getId();
                }).toList();

        String content = messageCreateRequest.getContent();
        Message message = new Message(content, channelId, authorId, attachmentIds);
        return messageRepository.save(message);
    }

    @Override //메시지 단건조회
    public Message find(UUID messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(()-> new NoSuchElementException("Message with id " + messageId + " not found"));
    }

    @Override //전체 메시지 조회
    public List<Message> findAllByChannelId(UUID channelId) {
        return messageRepository.findAllByChannelId(channelId).stream().toList();
    }

    @Override //메시지 수정
    public Message update(UUID messageId, MessageUpdateRequest request) {
        String newContent = request.getNewContent();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(()-> new NoSuchElementException("Message with id " + messageId + " not found"));
        message.update(newContent);
        return messageRepository.save(message);
    }

    @Override //메시지 삭제
    public void delete(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(()-> new NoSuchElementException("Message with id " + messageId + " not found"));
        message.getAttachmentIds()
                .forEach(binaryContentRepository::deleteById);

        messageRepository.deleteById(messageId);
    }
}
