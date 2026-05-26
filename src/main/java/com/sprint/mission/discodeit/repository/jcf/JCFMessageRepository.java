package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;

import java.util.*;

public class JCFMessageRepository implements MessageRepository {

    private final Map<UUID, Message> data;
    private final ChannelService channelService;
    private final UserService userService;

    public JCFMessageRepository(UserService userService,
                             ChannelService channelService) {

        this.data = new HashMap<>();
        this.userService = userService;
        this.channelService = channelService;
    }

    @Override
    public void create(Message message) {

        if (message == null) {
            throw new IllegalArgumentException("메시지 정보가 없습니다.");
        }

        userService.read(message.getAuthorId());
        channelService.read(message.getChannelId());

        if (data.containsKey(message.getId())) {
            throw new IllegalArgumentException("이미 존재하는 메시지입니다.");
        }

        data.put(message.getId(), message);
    }

    @Override
    public Message read(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("메시지 ID는 필수입니다.");
        }

        if (!data.containsKey(id)) {
            throw new IllegalArgumentException("존재하지 ID입니다.");
        }

        return data.get(id);
    }

    @Override
    public List<Message> readAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void update(UUID id, Message message) {
        if (id == null) {
            throw new IllegalArgumentException("메세지 ID는 필수입니다.");
        }

        if (message == null) {
            throw new IllegalArgumentException("메세지 내용이 없습니다.");
        }

        if (!data.containsKey(id)) {
            throw new IllegalArgumentException("수정할 메시지가 존재하지 않습니다.");
        }

        data.put(id, message);
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("메세지 ID는 필수입니다.");
        }
        if (!data.containsKey(id)) {
            throw new IllegalArgumentException("메세지 ID가 존재하지 않습니다.");
        }
        data.remove(id);
    }
}
