package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final BinaryContentRepository binaryContentRepository;


    private MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getMessageId(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getContent(),
                message.getChannelId(),
                message.getAuthorId(),
                message.getAttachmentIds()
        );
    }

    //메시지, 첨부파일 생성
    @Override
    public MessageResponse create(MessageCreateRequest request, List<BinaryContentCreateRequest> attachments) {
        channelRepository.findById(request.channelId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 채널입니다."));
        userRepository.findById(request.authorId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 사용자입니다."));

        List<UUID> attachmentIds = new ArrayList<>();
        if (attachments != null && ! attachments.isEmpty()) {
            attachments.forEach(attachment -> {
                BinaryContent binaryContent = new BinaryContent(
                        attachment.fileName(),
                        attachment.fileSize(),
                        attachment.contentType(),
                        attachment.bytes()
                );
                binaryContentRepository.save(binaryContent);
                attachmentIds.add(binaryContent.getId());
            });
        }
        Message message = new Message(request.content(), request.channelId(), request.authorId(),attachmentIds);
        log.info("메시지 생성 완료 - 채널: {}, 작성자: {} 메시지: {}",
                request.channelId(), request.authorId(), request.content());
        messageRepository.save(message);
        return toResponse(message);
    }

    //메시지 조회
    @Override
    public MessageResponse findById(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 메시지 입니다."));

        return toResponse(message);
    }

    //특정 채널 메시지 조회
    @Override
    public List<MessageResponse> findAllByChannelId(UUID channelId) {
        channelRepository.findById(channelId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 채널입니다."));
        List<Message> allByChannelId = messageRepository.findAllByChannelId(channelId);
        log.info("채널id: {}, 전체 메시지 조회 완료: {}개", channelId, allByChannelId.size());
        return allByChannelId.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }



    //메시지 수정
    @Override
    public MessageResponse updateMessage(UUID messageId, MessageUpdateRequest request) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 메시지 입니다."));
        message.updateContent(request.content());
        messageRepository.save(message);
        log.info("메시지 수정 완료 - 메시지: {}", message.getContent());
        return toResponse(message);
    }




    //메시지, 첨부파일 삭제
    @Override
    public void delete(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 메시지 입니다."));
        message.getAttachmentIds()
                .forEach(binaryContentRepository::delete);
        messageRepository.deleteById(messageId);

        log.info("메시지 삭제완료 - 메시지id: {}, 메시지: {}", message.getMessageId(), message.getContent());
    }
}
