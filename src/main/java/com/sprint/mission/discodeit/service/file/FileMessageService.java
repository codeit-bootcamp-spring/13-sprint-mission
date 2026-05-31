package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileMessageService implements MessageService {
    private final Path path;

    private final UserService userService;
    private final ChannelService channelService;

    public FileMessageService(String path, UserService userService, ChannelService channelService) {
        this.path = Paths.get(path);
        this.userService = userService;
        this.channelService = channelService;
    }

    private void saveMessageFile(List<Message> messages) {
        Path parent = path.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
            oos.writeObject(new ArrayList<>(messages));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Message> loadMessageFile() {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            return (List<Message>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message create(UUID userId, UUID channelId, String content) {
        if (userService.read(userId) == null) {
            System.out.println("계정이 존재하지 않습니다.");
            return null;
        }
        if (channelService.read(channelId) == null) {
            System.out.println("채널이 존재하지 않습니다.");
            return null;
        }

        Message message = new Message(userId, channelId, content);
        List<Message> messages = loadMessageFile();

        messages.add(message);
        saveMessageFile(messages);

        System.out.println("메시지가 작성되었습니다!");
        return message;
    }

    @Override
    public Message read(UUID id) {
        List<Message> messages = loadMessageFile();

        for (Message message : messages) {
            if (message.getId().equals(id)) {
                return message;
            }
        }
        System.out.println("메시지가 작성되지 않았습니다.");
        return null;
    }

    @Override
    public List<Message> readAll() {
        return loadMessageFile();
    }

    @Override
    public void update(UUID id, String content) {
        List<Message> messages = loadMessageFile();

        for (Message message : messages) {
            if (message.getId().equals(id)) {
                message.update(content);
                break;
            }
        }

        saveMessageFile(messages);
    }

    @Override
    public void delete(UUID id) {
        List<Message> messages = loadMessageFile();

        for (Message message : messages) {
            if(message.getId().equals(id)) {
                messages.remove(message);
                break;
            }
        }

        saveMessageFile(messages);
    }
}
