package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public MessageResponse create(MessageCreateRequest dto) {
        Message message = new Message(dto.content());
        // [요구사항] 선택적으로 여러 개의 첨부파일을 같이 등록할 수 있습니다.
//        message.assignBinaryContents(dto.binaryContentIds());
        messageRepository.create(message);
        return new MessageResponse(
                message.getId(),
                message.getChannelId(),
                message.getAuthorId(),
                message.getContent(),
                Collections.emptyList(), // 첨부파일 목록은 일단 빈 리스트로 안전하게 반환
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }

    @Override
    public Optional<MessageResponse> findById(UUID id) {
        return messageRepository.findById(id)
                .map(m -> new MessageResponse(
                        m.getId(),
                        m.getChannelId(),
                        m.getAuthorId(),
                        m.getContent(),
                        Collections.emptyList(),
                        m.getCreatedAt(),
                        m.getUpdatedAt()
                ));
    }

    @Override
    public List<MessageResponse> findAll(UUID channelId) {
        // [요구사항] 특정 Channel의 Message 목록을 조회하도록 조회 조건을 추가하고, 메소드 명을 변경합니다.
        return messageRepository.findAll().stream()
                .filter(m -> m.getChannelId() != null && m.getChannelId().equals(channelId))
                .map(m -> new MessageResponse(
                        m.getId(),
                        m.getChannelId(),
                        m.getAuthorId(),
                        m.getContent(),
                        Collections.emptyList(),
                        m.getCreatedAt(),
                        m.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageResponse> findAllByChannelId(UUID channelId) {
        // [요구사항] 특정 Channel의 Message 목록을 조회하도록 조회 조건을 추가하고, 메소드 명을 변경합니다.
        return messageRepository.findAll().stream()
                .filter(m -> m.getChannelId() != null && m.getChannelId().equals(channelId))
                .map(m -> new MessageResponse(
                        m.getId(),
                        m.getChannelId(),
                        m.getAuthorId(),
                        m.getContent(),
                        Collections.emptyList(),
                        m.getCreatedAt(),
                        m.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public MessageResponse update(UUID id, MessageUpdateRequest dto) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 메시지를 찾을 수 없습니다."));

        message.updateContent(new Message(dto.content()));
        messageRepository.update(message);

        return new MessageResponse(
                message.getId(),
                message.getChannelId(),
                message.getAuthorId(),
                message.getContent(),
                Collections.emptyList(),
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }

    @Override
    public void delete(UUID id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 메시지를 찾을 수 없습니다."));

        // [요구사항] 관련된 도메인도 같이 삭제합니다. (첨부파일 BinaryContent 연쇄 삭제)
        // if (message.getBinaryContentIds() != null) {
        //     for (UUID binaryId : message.getBinaryContentIds()) {
        //         binaryContentRepository.delete(binaryId);
        //     }
        // }

        messageRepository.delete(id);
    }
}
