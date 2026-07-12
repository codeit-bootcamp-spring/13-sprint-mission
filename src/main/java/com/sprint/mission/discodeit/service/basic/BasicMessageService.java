package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    @Override
    public MessageResponse create(MessageCreateRequest dto) {
        Channel channel = channelRepository.findById(dto.channelId())
                .orElseThrow(() -> new IllegalArgumentException("채널을 찾을 수 없습니다."));
        User author = userRepository.findById(dto.senderId())
                .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다."));
        Message message = new Message(dto.content(), channel, author);
        messageRepository.save(message);
        return new MessageResponse(
                message.getId(),
                message.getChannel().getId(),
                message.getAuthor().getId(),
                message.getContent(),
                Collections.emptyList(),
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MessageResponse> findById(UUID id) {
        return messageRepository.findById(id)
                .map(m -> new MessageResponse(
                        m.getId(),
                        m.getChannel().getId(),
                        m.getAuthor().getId(),
                        m.getContent(),
                        Collections.emptyList(),
                        m.getCreatedAt(),
                        m.getUpdatedAt()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> findAll(UUID channelId) {
        return findAllByChannelId(channelId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> findAllByChannelId(UUID channelId) {
        return messageRepository.findAllByChannelId(channelId).stream()
                .map(m -> new MessageResponse(
                        m.getId(),
                        m.getChannel().getId(),
                        m.getAuthor().getId(),
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

        message.updateContent(dto.content());

        return new MessageResponse(
                message.getId(),
                message.getChannel().getId(),
                message.getAuthor().getId(),
                message.getContent(),
                Collections.emptyList(),
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }

    @Override
    public void delete(UUID id) {
        messageRepository.deleteById(id);
    }
}
