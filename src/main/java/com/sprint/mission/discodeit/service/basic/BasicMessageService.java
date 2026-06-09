package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final BinaryContentRepository binaryContentRepository;


    //메시지, 첨부파일 생성
    @Override
    public Message create(MessageCreateRequest request) {
        Channel channelById = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 채널입니다."));
        User userById = userRepository.findById(request.authorId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 사용자입니다."));

        List<UUID> attachmentIds = new ArrayList<>();
        if (request.attachments() != null && !request.attachments().isEmpty()) {
            request.attachments().forEach((attachmentId) -> {
                BinaryContent binaryContent = new BinaryContent(
                        null,
                        attachmentId.fileName(),
                        attachmentId.fileSize(),
                        attachmentId.contentType(),
                        attachmentId.bytes()
                );
                binaryContentRepository.save(binaryContent);
                attachmentIds.add(binaryContent.getId());
            });
        }
        Message message = new Message(request.content(), request.channelId(), request.authorId(),attachmentIds);
        messageRepository.save(message);
        return message;

    }

    //메시지 조회
    @Override
    public Message findById(UUID messageId) {
        Message messageById = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("존재 하지 않는 메시지 입니다."));

        return messageById;
    }

    //특정 채널 메시지 조회
    @Override
    public List<Message> findAllByChannelId(UUID channelId) {
        channelRepository.findById(channelId)
                .orElseThrow(()-> new NoSuchElementException("존재하지 않는 채널입니다."));
        return messageRepository.findAllByChannelId(channelId);
    }



    //메시지 수정
    @Override
    public Message update(UUID messageId, MessageUpdateRequest request) {
        Message messageById = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("존재 하지 않는 메시지 입니다."));

        messageById.updateContent(request.content());
        messageRepository.save(messageById);
        return messageById;
    }




    //메시지, 첨부파일 삭제
    @Override
    public void delete(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 메시지 입니다."));

        message.getAttachmentIds()
                .forEach(binaryContentRepository::delete);

        messageRepository.deleteById(messageId);
    }
}
