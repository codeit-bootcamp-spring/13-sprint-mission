package com.sprint.mission.discodeit.service.file;


import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

class FileMessageService implements MessageService {

    private final MessageRepository repository;

    public FileMessageService(MessageRepository repository) {
        this.repository = repository;
    }

    @Override
    public MessageResponse create(MessageCreateRequest request) {

        Message message = new Message(
                request.userId(),
                request.channelId(),
                request.content()
        );

        repository.save(message);

        return new MessageResponse(
                message.getId(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getAuthorId(),
                message.getChannelId(),
                message.getContent(),
                List.of()
        );
    }

    @Override
    public List<MessageResponse> findAllByChannelId(UUID channelId) {

        return repository.findAll().stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .map(message -> new MessageResponse(
                        message.getId(),
                        message.getCreatedAt(),
                        message.getUpdatedAt(),
                        message.getAuthorId(),
                        message.getChannelId(),
                        message.getContent(),
                        List.of()
                ))
                .toList();
    }

    @Override
    public MessageResponse update(MessageUpdateRequest request) {

        Message message = repository.findById(request.id())
                .orElseThrow(() ->
                        new NoSuchElementException("Message not found"));

        message.updateContent(request.content());

        repository.save(message);

        return new MessageResponse(
                message.getId(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getAuthorId(),
                message.getChannelId(),
                message.getContent(),
                List.of()
        );
    }

    @Override
    public void delete(UUID id) {
        repository.delete(id);
    }
}
