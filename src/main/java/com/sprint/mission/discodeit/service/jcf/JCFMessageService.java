package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;

public class JCFMessageService implements MessageService {
    private final Map<UUID, Message> data;

    private final UserService userService;
    private final ChannelService channelService;

    public JCFMessageService(UserService userService, ChannelService channelService) {
        this.data = new HashMap<>();
        this.userService = userService;
        this.channelService = channelService;
    }

    @Override
    public Message create(UUID userId, UUID channelId, String content) {
        if(userService.read(userId)==null) {
            throw new IllegalArgumentException("존재하지 않는 계정입니다.");
        }
        if(channelService.read(channelId)==null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }
        Message message = new Message(userId, channelId, content);
        data.put(message.getId(), message);
        System.out.println("메시지가 작성되었습니다!");
        return message;
    }

    @Override
    public Message read(UUID id) {
        if (!data.containsKey(id)) {
            System.out.println("메시지가 작성되지 않았습니다.");
        }
        return data.get(id);
    }

    @Override
    public List<Message> readAll() {
        return data.values().stream()
                .toList();
    }

    @Override
    public void update(UUID id, String content) {
        if(data.containsKey(id)){
            Message message = data.get(id);
            message.update(content);
        }
    }

    @Override
    public void delete(UUID id) {
        if(!data.containsKey(id)){
            System.out.println("메시지가 작성되지 않았습니다.");
        }
        data.remove(id);
    }
}
