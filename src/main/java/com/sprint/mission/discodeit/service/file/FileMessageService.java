package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
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

    private final UserService userService;
    private final ChannelService channelService;

    public FileMessageService(UserService userService, ChannelService channelService) {
        this.userService = userService;
        this.channelService = channelService;
    }

    private final Path filePath = Paths.get("message.ser");

    private List<Message> loadFromFile() {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toString()))) {
         return (List<Message>) ois.readObject();
        }catch (IOException | ClassNotFoundException e){
            throw new RuntimeException("파일 로드 실패", e);
        }
    }
    private void saveToFile(List<Message> messages) {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
            oos.writeObject(messages);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }


    @Override
    public void create(Message message) {

        User foundUser = userService.read(message.getAuthorId());
        if (foundUser == null){
            throw new IllegalArgumentException("유저를 찾을 수 없습니다.");
        }

        Channel foundCh = channelService.read(message.getChannelId());
        if (foundCh == null){
            throw new IllegalArgumentException("채널을 찾을 수 없습니다.");
        }


        List<Message> messages = loadFromFile();
        messages.add(message);
        saveToFile(messages);

    }

    @Override
    public Message read(UUID id) {
        List<Message> messages = loadFromFile();
        for (Message message : messages) {
            if (message.getId().equals(id)) {
                return message;
            }
        }
        return null;
    }

    @Override
    public List<Message> readAll() {
        return loadFromFile();
    }

    @Override
    public void update(UUID id, String message) {
        List<Message> messages = loadFromFile();
        Message foundMessage = read(id);
        if (foundMessage != null) {
            foundMessage.updateMessage(message);
            saveToFile(messages);
        }


    }

    @Override
    public void delete(UUID id) {
        List<Message> messages = loadFromFile();
            messages.remove(read(id));
            saveToFile(messages);
        }

    }

