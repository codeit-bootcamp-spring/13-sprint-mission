package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

import java.util.UUID;

public class JavaApplication {

    public static void main(String[] args) {

        UserService userService = new JCFUserService();
        ChannelService channelService = new JCFChannelService();
        MessageService messageService = new JCFMessageService(userService, channelService);

        // 유저 생성
        User user = new User("최성웅", "test@test.com", "123456");
        userService.create(user);

        System.out.println(userService.findAll());

        System.out.println("==================================================");

        // 채널 생성
        Channel channel = new Channel("testChannel", "test", Channel.ChannelType.PUBLIC);
        channelService.create(channel);

        System.out.println(channelService.findAll());

        System.out.println("==================================================");

        // 메세지 생성
        Message message = messageService.create(user.getId(), channel.getId(), "test message");

        System.out.println(messageService.findAll());

        System.out.println("==================================================");

        // 유저, 채널, 메세지 수정
        userService.update(user.getId(), "이병건");
        channelService.update(channel.getId(), "testChannel3", "test3", Channel.ChannelType.PUBLIC);
        messageService.update(message.getId(), "testMessage3");

        System.out.println(userService.findAll());
        System.out.println(channelService.findAll());
        System.out.println(messageService.findById(message.getId()));

        System.out.println("==================================================");

        // 유저, 채널, 메세지 삭제
        userService.delete(user.getId());
        channelService.delete(channel.getId());
        messageService.delete(message.getId());

        System.out.println(userService.findAll());
        System.out.println(channelService.findAll());
        System.out.println(messageService.findAll());

    }

}
