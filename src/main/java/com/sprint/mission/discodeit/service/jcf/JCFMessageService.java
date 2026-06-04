package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.List;
import java.util.UUID;

public class JCFMessageService implements MessageService {

    private final MessageRepository repository;
    private final ChannelService channelService;
    private final UserService userService;

    public JCFMessageService(
            MessageRepository repository,
            UserService userService,
            ChannelService channelService
    ) {
        this.repository = repository;
        this.userService = userService;
        this.channelService = channelService;
    }

    @Override
    public Message create(String content, UUID channelId, UUID authorId) {
        if (channelId == null) {
            throw new IllegalArgumentException("채널 ID는 필수입니다.");
        }

        if (authorId == null) {
            throw new IllegalArgumentException("작성자 ID는 필수입니다.");
        }

        userService.read(authorId);
        channelService.read(channelId);

        Message message = new Message(content, channelId, authorId);

        repository.create(message);

        return message;
    }

    @Override
    public Message read(UUID messageId) {
        if (messageId == null) {
            throw new IllegalArgumentException("메시지 ID는 필수입니다.");
        }

        if (!repository.exists(messageId)) {
            throw new IllegalArgumentException("존재하지 않는 메시지 ID입니다.");
        }

        return repository.read(messageId);
    }

    @Override
    public List<Message> readAll() {
        return repository.readAll();
    }

    @Override
    public Message update(UUID id, String content) {
        if (id == null) {
            throw new IllegalArgumentException("메시지 ID는 필수입니다.");
        }

        if (!repository.exists(id)) {
            throw new IllegalArgumentException("수정할 메시지가 존재하지 않습니다.");
        }

        Message message = repository.read(id);
        message.updateContent(content);

        repository.update(id, message);

        return message;
    }

    @Override
    public void delete(UUID messageId) {
        if (messageId == null) {
            throw new IllegalArgumentException("메시지 ID는 필수입니다.");
        }

        if (!repository.exists(messageId)) {
            throw new IllegalArgumentException("삭제할 메시지가 존재하지 않습니다.");
        }

        repository.delete(messageId);
    }
}