package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final MessageMapper messageMapper;
    private final BinaryContentStorage binaryContentStorage;


    //메시지, 첨부파일 생성
    @Override
    public MessageDto create(MessageCreateRequest request, List<BinaryContentCreateRequest> attachments) {
        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 채널입니다."));
        User user = userRepository.findById(request.authorId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 사용자입니다."));

        List<BinaryContent> attachmentIds = new ArrayList<>();
        if (attachments != null && ! attachments.isEmpty()) {
            attachments.forEach(attachment -> {
                BinaryContent binaryContent = new BinaryContent(
                        attachment.fileName(),
                        attachment.fileSize(),
                        attachment.contentType()
                );
                binaryContentRepository.save(binaryContent);
                binaryContentStorage.put(binaryContent.getId(), attachment.bytes());
                attachmentIds.add(binaryContent);
            });
        }
        Message message = new Message(request.content(), channel, user, attachmentIds);
        log.info("메시지 생성 완료 - 채널: {}, 작성자: {} 메시지: {}",
                request.channelId(), request.authorId(), request.content());
        messageRepository.save(message);
        return messageMapper.toDto(message);
    }

    //메시지 조회
    @Transactional(readOnly = true)
    @Override
    public MessageDto findById(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 메시지 입니다."));

        return messageMapper.toDto(message);
    }

    //특정 채널 메시지 조회
    @Override
    @Transactional(readOnly = true)
    public List<MessageDto> findAllByChannelId(UUID channelId) {
        channelRepository.findById(channelId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 채널입니다."));
        List<Message> allByChannelId = messageRepository.findAllByChannelId(channelId);
        log.info("채널id: {}, 전체 메시지 조회 완료: {}개", channelId, allByChannelId.size());
        return allByChannelId.stream()
                .map(messageMapper::toDto)
                .collect(Collectors.toList());
    }



    //메시지 수정
    @Override
    public MessageDto updateMessage(UUID messageId, MessageUpdateRequest request) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 메시지 입니다."));
        message.updateContent(request.content());
        messageRepository.save(message);
        log.info("메시지 수정 완료 - 메시지: {}", message.getContent());
        return messageMapper.toDto(message);
    }




    //메시지, 첨부파일 삭제
    @Override
    public void delete(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 메시지 입니다."));
        messageRepository.deleteById(messageId);

        log.info("메시지 삭제완료 - 메시지id: {}, 메시지: {}", message.getId(), message.getContent());
    }
}
