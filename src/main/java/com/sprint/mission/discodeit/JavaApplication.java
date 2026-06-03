package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;

import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;

import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;

import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import com.sprint.mission.discodeit.service.file.FileChannelService;
import com.sprint.mission.discodeit.service.file.FileMessageService;
import com.sprint.mission.discodeit.service.file.FileUserService;

public class JavaApplication {

    public static void main(String[] args) {

        UserRepository userRepo =
                new FileUserRepository("users.dat");

        MessageRepository messageRepo =
                new FileMessageRepository("messages.dat");

        ChannelRepository channelRepo =
                new FileChannelRepository("channels.dat");

        UserService userService =
                new FileUserService(userRepo);

        MessageService messageService =
                new FileMessageService(messageRepo);

        ChannelService channelService =
                new FileChannelService(channelRepo);

        User user = new User("kim", "kim@test.com");
        Channel channel = new Channel("general", "general chat");
        Message message = new Message("hello", user.getId(), channel.getId());

        userService.create(user);
        channelService.create(channel);
        messageService.create(message);

        System.out.println(userService.findAll());
        System.out.println(channelService.findAll());
        System.out.println(messageService.findAll());

        userService.update(user.getId(), "kim-updated", "new@test.com");
        channelService.update(channel.getId(), "random", "random chat");
        messageService.update(message.getId(), "updated message");

        System.out.println(userService.find(user.getId()).getUsername());
        System.out.println(channelService.find(channel.getId()).getName());
        System.out.println(messageService.find(message.getId()).getContent());

        userService.delete(user.getId());
        channelService.delete(channel.getId());
        messageService.delete(message.getId());

        System.out.println(userService.findAll());
        System.out.println(channelService.findAll());
        System.out.println(messageService.findAll());
    }
}