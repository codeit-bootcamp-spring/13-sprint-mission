package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

import java.util.List;

import static com.sprint.mission.discodeit.entity.Channel.ChannelType.PRIVATE;
import static com.sprint.mission.discodeit.entity.Channel.ChannelType.PUBLIC;

public class JavaApplication {

    public static void main(String[] args) {

        JCFUserService userService = new JCFUserService();
        JCFChannelService channelService = new JCFChannelService();
        JCFMessageService messageService = new JCFMessageService();

        User user1 = new User("령", "gpfud09@gmail.com", "111@@@");
        User user2 = new User("영경", "smdrma1127@naver.com", "1123@@@");
        User user3 = new User("가나디", "rkskel00@naver.com", "1803@@@");
        System.out.println("user = " + user1.getId());

        // user 생성
        userService.createUser(user1);
        userService.createUser(user2);
        userService.createUser(user3);

        // user 조회
        User findUser = userService.findUser(user1.getId());
        System.out.println("findUser = " + findUser);

        // users 전체 조회
        List<User> allUsers = userService.findAllUsers();
        System.out.println("users = " + allUsers);

        // user 수정
        userService.updateUser(user1.getId(), "줴령", "gpfud9109", "333###");

        // 수정 후 조회
        User updatedUser = userService.findUser(user1.getId());
        System.out.println("updatedUser = " + updatedUser);

        // user 삭제
        userService.deleteUser(user1.getId());

        // users 전체 조회
        List<User> deleteAllUsers = userService.findAllUsers();
        System.out.println("deleteAllUsers = " + deleteAllUsers);


        System.out.println("\n===================================================================\n");

        Channel ch1 = new Channel("코드잇13기", PUBLIC, "코드잇13기 단체 채팅방입니당 ~");

        // channel 생성
        channelService.createChannel(ch1);

        // channel 조회
        Channel channel = channelService.findChannel(ch1.getId());
        System.out.println("channel = " + channel);


        // channel 전체 조회
        List<Channel> allChannels = channelService.findAllChannels();
        System.out.println("allChannels = " + allChannels);

        // channel 수정
        System.out.println("ch1.getId() = " + ch1.getId());
        channelService.updateChannel(ch1.getId(), null, PRIVATE, null);

        // 수정 후 조회
        Channel updatedChannel = channelService.findChannel(ch1.getId());
        System.out.println("updatedChannel = " + updatedChannel);

        // channel 삭제
        channelService.deleteChannel(ch1.getId());

        // 삭제 후 조회
        List<Channel> deleteAllChannels = channelService.findAllChannels();
        System.out.println("deleteAllChannels = " + deleteAllChannels);


        System.out.println("\n===================================================================\n");

        Message m1 = new Message("나랑 노올자 ~", ch1, user1);
        Message m2 = new Message("배 고 픈 데 나 만 인 가", ch1, user3);

        // 메시지 생성
        messageService.createMessage(m1);
        messageService.createMessage(m2);

        // 메시지 조회
        Message message = messageService.findMessage(m1.getId());
        System.out.println("message = " + message);

        // 메시지 전체 조회
        List<Message> allMessages = messageService.findAllMessages();
        System.out.println("allMessages = " + allMessages);

        // 메시지 수정
        messageService.updateMessage(m1.getId(), "왜 나랑 안놀아 !");
        System.out.println("message = " + message);

        // 수정 후 조회
        Message updatedMessage = messageService.findMessage(m1.getId());
        System.out.println("updatedMessage = " + updatedMessage);

        // 메시지 삭제
        messageService.deleteMessage(m1.getId());

        // 메시지 삭제 후 조회
        List<Message> deleteAllMessages = messageService.findAllMessages();
        System.out.println("deleteAllMessages = " + deleteAllMessages);

    }

}
