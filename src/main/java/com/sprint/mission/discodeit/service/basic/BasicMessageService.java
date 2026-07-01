package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public MessageResponse create(MessageCreateRequest request) {
        if (userRepository.findById(request.userId()) == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        if (channelRepository.findById(request.channelId()) == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }

        List<UUID> savedFileIds = new ArrayList<>();
        if(request.attachment()!=null && !request.attachment().isEmpty()) {
            for (BinaryContentCreateRequest fileDto : request.attachment()) {
                BinaryContent binaryContent = new BinaryContent(fileDto.fileName(), fileDto.contentType(), fileDto.size(), fileDto.bytes());
                binaryContentRepository.save(binaryContent);

                savedFileIds.add(binaryContent.getId());
            }
        }

        Message message = new Message(request.userId(), request.channelId(), request.content(), savedFileIds);
        messageRepository.save(message);
        return new MessageResponse(message.getId(), message.getUserId(), message.getChannelId(), message.getContent(), request.attachment());
    }

    @Override
    public List<MessageResponse> findAllByChannelId(UUID channelId) {
        if(channelRepository.findById(channelId)==null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }

        List<MessageResponse> responses = new ArrayList<>();
        List<Message> messages = messageRepository.findAll();

        List<BinaryContent> allFiles = binaryContentRepository.findAll();
        for (Message message : messages) {
            if(message.getChannelId().equals(channelId)) {
                responses.add(returnResponse(message, allFiles));
            }
        }
        return responses;
    }

    @Override
    public MessageResponse update(MessageUpdateRequest request) {
        Message message = messageRepository.findById(request.id());
        if(message==null) {
            throw new IllegalArgumentException("존재하지 않는 메시지입니다.");
        }

        message.update(request.content());
        messageRepository.save(message);

        List<BinaryContent> allFiles = binaryContentRepository.findAll();
        return returnResponse(message, allFiles);
    }

    @Override
    public void delete(UUID id) {
        Message message = messageRepository.findById(id);
        if(message==null) {
            throw new IllegalArgumentException("존재하지 않는 메시지입니다.");
        }

        if (message.getAttachmentIds() != null && !message.getAttachmentIds().isEmpty()) {
            for (UUID attachmentId : message.getAttachmentIds()) {
                binaryContentRepository.delete(attachmentId);
            }
        }
        messageRepository.delete(id);
    }

    private List<BinaryContentCreateRequest> getAttachment(Message message, List<BinaryContent> allFiles) {
        List<BinaryContentCreateRequest> attachments = new ArrayList<>();

        if(message.getAttachmentIds()==null || message.getAttachmentIds().isEmpty()) {
            return attachments;
        }

        for (BinaryContent file : allFiles) {
            if (message.getAttachmentIds().contains(file.getId())) {
                attachments.add(new BinaryContentCreateRequest(
                        file.getFileName(), file.getContentType(), file.getSize(), file.getBytes()
                ));
            }
        }
        return attachments;
    }

    private MessageResponse returnResponse(Message message, List<BinaryContent> allFiles) {
        return new MessageResponse(message.getId(), message.getUserId(), message.getChannelId(), message.getContent(), getAttachment(message, allFiles));
    }
}
