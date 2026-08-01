package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final BinaryContentStorage binaryContentStorage;

    @Override
    @Transactional
    public MessageDto create(MessageCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 계정입니다."));
        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 채널입니다."));

        List<BinaryContent> savedFiles = new ArrayList<>();
        if(request.attachment()!=null && !request.attachment().isEmpty()) {
            for (BinaryContentCreateRequest fileDto : request.attachment()) {
                BinaryContent binaryContent = new BinaryContent(fileDto.fileName(), fileDto.contentType(), fileDto.size());
                binaryContentStorage.put(binaryContent.getId(), fileDto.bytes());
                savedFiles.add(binaryContent);
            }
        }

        Message message = new Message(user, channel, request.content(), savedFiles);
        messageRepository.save(message);
        return messageMapper.toDto(message);
    }

    @Override
    public List<MessageDto> findAllByChannelId(UUID channelId) {
        if (!channelRepository.existsById(channelId)) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }

        List<Message> messages = messageRepository.findAllByChannel_Id(channelId);
        List<MessageDto> responses = new ArrayList<>();

        for (Message message : messages) {
            responses.add(messageMapper.toDto(message));
        }
        return responses;
    }

    @Override
    @Transactional
    public MessageDto update(MessageUpdateRequest request) {
        Message message = messageRepository.findById(request.id())
                        .orElseThrow(()->new IllegalArgumentException("존재하지 않는 메시지입니다."));

        message.update(request.content());
        messageRepository.save(message);

        return messageMapper.toDto(message);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 메시지입니다."));

        messageRepository.delete(message);
    }
}
