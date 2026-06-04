package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.util.*;

public class FileMessageService implements MessageService {

    // 저장로직
    private final File messageFile;
    private final Map<UUID, Message> messages;
    private final UserService userService;
    private final ChannelService channelService;

    private Map<UUID, Message> loadMesaages()  {
        if (!messageFile.exists()) {
            return new HashMap<UUID, Message>();
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(messageFile))) {
            return (Map<UUID, Message>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new HashMap<UUID, Message>();
        }


    }
    private void saveMessages() {
        File parent = messageFile.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(messageFile))) {
            out.writeObject(messages);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public FileMessageService(UserService userService, ChannelService channelService) {
        this.userService = userService;
        this.channelService = channelService;
        this.messageFile = new File("data/message.ser");
        this.messages = loadMesaages();
    }


    //비즈니스로직

    @Override
    public void create(Message message) {
        if (userService.findById(message.getAuthor().getId()) == null) {
            throw new IllegalArgumentException("존재하지 않는 유저의 메시지입니다.");
        }
        if (channelService.findById(message.getChannel().getId()) == null) {
            throw new IllegalArgumentException("존재하지 않는 채널의 메시지입니다.");
        }
        messages.put(message.getId(), message);
        saveMessages();
    }

    @Override
    public Message findById(UUID id) {
        return messages.get(id);
    }

    @Override
    public Collection<Message> findAll() {
        return messages.values();
    }

    @Override
    public void update(UUID id, String content) {
        Message message = this.messages.get(id);
        if (message != null) {
            message.update(content);
        }
        saveMessages();
    }

    @Override
    public void delete(UUID id) {
        messages.remove(id);
        saveMessages();
    }
}
