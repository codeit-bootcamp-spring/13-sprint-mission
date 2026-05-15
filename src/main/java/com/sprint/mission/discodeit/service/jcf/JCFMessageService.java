package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.service.*;

import java.util.*;

public class JCFMessageService implements MessageService {

    private final ChannelService channelService;
    private final UserService userService;
    private final Map<UUID, Message> data;

    public JCFMessageService(UserService userService, ChannelService channelService) {
        this.userService = userService;
        this.channelService = channelService;
        this.data = new HashMap<>();
    }


    @Override
    public void create(Message message) {

        User author = userService.read(message.getAuthorId());
        Channel channel = channelService.read(message.getChannelId());

        try {
            if (author == null || channel == null) {
                throw new RuntimeException();
            }
            data.put(message.getId(), message);
        } catch (Exception e) {
            System.out.println("채널 또는 유저가 없음.");
        }
    }

    @Override
    public Message read(UUID id) {
        return data.get(id);
    }

    @Override
    public List<Message> readAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void update(UUID id, Message message) {
        data.put(id, message);
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }
}
