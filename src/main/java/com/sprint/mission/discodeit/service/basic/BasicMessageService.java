package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
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
import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
@Service // Basic*Service 구현체를 Service 인터페이스의 Bean으로 등록
public class BasicMessageService implements MessageService {
    // 의존성 주입
    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final BinaryContentRepository contentRepository;

    @Override
    public MessageResponse create(MessageCreateRequest request) {
        channelRepository.findById(request.getChannelId())
                .orElseThrow(()->new NoSuchElementException(request.getChannelId()+" 를 찾지 못했습니다."));
        userRepository.findById(request.getAuthorId())
                .orElseThrow(()->new NoSuchElementException(request.getAuthorId()+" 를 찾지 못했습니다."));
        // 선택적으로 여러 개의 첨부파일 같이 등록 가능
        List<UUID> attachmentIds=new ArrayList<>();
        if (request.getAttachmentIds() != null){
            for (BinaryContentCreateRequest file : request.getAttachmentIds()) {
                BinaryContent binaryContent=new BinaryContent(
                        file.getFileName(),
                        file.getContentType(),
                        file.getFileSize(),
                        file.getBytes()
                );
                contentRepository.save(binaryContent);
                attachmentIds.add(binaryContent.getId());
            }
        }
        Message message=new Message(request.getContent(), request.getChannelId(), request.getAuthorId(), attachmentIds);
        messageRepository.save(message);
        return MessageResponse.from(message);
    }

    @Override
    public MessageResponse find(UUID messageId) {
        Message foundMessage = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException(messageId + " 를 찾지 못했습니다."));
        return MessageResponse.from(foundMessage);
    }

    @Override
    public List<MessageResponse> findAllByChannelId(UUID channelId) {
        // 특정 Channel의 Message 목록 조회 조건 추가
        return messageRepository.findAllByChannelId(channelId).stream()
                .map(MessageResponse::from)
                .toList();
    }

    @Override
    public MessageResponse update(UUID messageId, MessageUpdateRequest request) {
        Message message=messageRepository.findById(messageId)
                .orElseThrow(()->new NoSuchElementException(messageId+" 를 찾을 수 없습니다."));
        message.update(request.getNewContent());
        messageRepository.save(message);
        return MessageResponse.from(message);
    }

    @Override
    public void delete(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException(messageId + " 를 찾을 수 없습니다."));
        // 관련된 도메인 BinaryContent도 같이 삭제
        contentRepository.findAllByIdIn(message.getAttachmentIds())
                        .forEach(content->contentRepository.deleteById(content.getId()));
        messageRepository.deleteById(messageId);
    }
}
