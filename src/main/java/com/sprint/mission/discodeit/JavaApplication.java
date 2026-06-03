package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.file.FileChannelService;
import com.sprint.mission.discodeit.service.file.FileMessageService;
import com.sprint.mission.discodeit.service.file.FileUserService;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

import java.util.List;
import java.util.UUID;

import static com.sprint.mission.discodeit.entity.Channel.ChannelType.PRIVATE;
import static com.sprint.mission.discodeit.entity.Channel.ChannelType.PUBLIC;

public class JavaApplication {

    public static void main(String[] args) {

//        JCFUserService userService = new JCFUserService();
//        JCFChannelService channelService = new JCFChannelService();
//        JCFMessageService messageService = new JCFMessageService();
        UserService fileUserService = new FileUserService();
        ChannelService fileChannelService = new FileChannelService();
        MessageService fileMessageService = new FileMessageService();

        User user1 = new User("령", "gpfud09@gmail.com", "111@@@");
        User user2 = new User("영경", "smdrma1127@naver.com", "1123@@@");
        User user3 = new User("가나디", "rkskel00@naver.com", "1803@@@");
        System.out.println("user = " + user1.getId());

        // user 생성
//        userService.createUser(user1);
//        userService.createUser(user2);
//        userService.createUser(user3);
//
//        // user 조회
//        User findUser = userService.findUser(user1.getId());
//        System.out.println("findUser = " + findUser);
//
//        // users 전체 조회
//        List<User> allUsers = userService.findAllUsers();
//        System.out.println("users = " + allUsers);
//
//        // user 수정
//        userService.updateUser(user1.getId(), "줴령", "gpfud9109", "333###");
//
//        // 수정 후 조회
//        User updatedUser = userService.findUser(user1.getId());
//        System.out.println("updatedUser = " + updatedUser);
//
//        // user 삭제
//        userService.deleteUser(user1.getId());
//
//        // users 전체 조회
//        List<User> deleteAllUsers = userService.findAllUsers();
//        System.out.println("deleteAllUsers = " + deleteAllUsers);


        System.out.println("\n===================================================================\n");

        Channel ch1 = new Channel("코드잇13기", PUBLIC, "코드잇13기 단체 채팅방입니당 ~");
        Channel ch2 = new Channel("제메추", PUBLIC, "저녁 머머글래용 ~");
//
//        // channel 생성
//        channelService.createChannel(ch1);
//
//        // channel 조회
//        Channel channel = channelService.findChannel(ch1.getId());
//        System.out.println("channel = " + channel);
//
//
//        // channel 전체 조회
//        List<Channel> allChannels = channelService.findAllChannels();
//        System.out.println("allChannels = " + allChannels);
//
//        // channel 수정
//        System.out.println("ch1.getId() = " + ch1.getId());
//        channelService.updateChannel(ch1.getId(), null, PRIVATE, null);
//
//        // 수정 후 조회
//        Channel updatedChannel = channelService.findChannel(ch1.getId());
//        System.out.println("updatedChannel = " + updatedChannel);
//
//        // channel 삭제
//        channelService.deleteChannel(ch1.getId());
//
//        // 삭제 후 조회
//        List<Channel> deleteAllChannels = channelService.findAllChannels();
//        System.out.println("deleteAllChannels = " + deleteAllChannels);
//

        System.out.println("\n===================================================================\n");

        Message m1 = new Message("나랑 노올자 ~", ch2, user1);
        Message m2 = new Message("배 고 픈 데 나 만 인 가", ch1, user3);

//        // 메시지 생성
//        messageService.createMessage(m1);
//        messageService.createMessage(m2);
//
//        // 메시지 조회
//        Message message = messageService.findMessage(m1.getId());
//        System.out.println("message = " + message);
//
//        // 메시지 전체 조회
//        List<Message> allMessages = messageService.findAllMessages();
//        System.out.println("allMessages = " + allMessages);
//
//        // 메시지 수정
//        messageService.updateMessage(m1.getId(), "왜 나랑 안놀아 !");
//        System.out.println("message = " + message);
//
//        // 수정 후 조회
//        Message updatedMessage = messageService.findMessage(m1.getId());
//        System.out.println("updatedMessage = " + updatedMessage);
//
//        // 메시지 삭제
//        messageService.deleteMessage(m1.getId());
//
//        // 메시지 삭제 후 조회
//        List<Message> deleteAllMessages = messageService.findAllMessages();
//        System.out.println("deleteAllMessages = " + deleteAllMessages);

        System.out.println("\n===================================================================\n");

        // 유저 생성
        fileUserService.createUser(user1);
        fileUserService.createUser(user2);
        fileUserService.createUser(user3);

        // 유저 단일 조회
        User findUser2 = fileUserService.findUser(user1.getId());
        System.out.println(findUser2);

        // 유저 전체 조회
        System.out.println(fileUserService.findAllUsers());

        // 유저 수정 후 조회
        fileUserService.updateUser(
                user1.getId(),
                "에베벱",
                "new@egmail.com",
                "5678@@"
        );
        System.out.println(fileUserService.findUser(user1.getId()));

        // 유저 삭제
        fileUserService.deleteUser(user1.getId());
        System.out.println(fileUserService.findAllUsers());

        System.out.println("\n===================================================================\n");

        // channel 생성
        fileChannelService.createChannel(ch1);
        fileChannelService.createChannel(ch2);

        // channel 단일 조회
        Channel findCh1 = fileChannelService.findChannel(ch1.getId());
        System.out.println(findCh1);

        // channel 전체 조회
        System.out.println(fileChannelService.findAllChannels());

        // channel 수정 후 조회
        fileChannelService.updateChannel(ch1.getId(), "점메추", PRIVATE, "오늘 점심 머머글래요 ?");
        System.out.println(fileChannelService.findChannel(ch1.getId()));

        // channel 삭제
        fileChannelService.deleteChannel(ch1.getId());
        System.out.println(fileChannelService.findAllChannels());

        System.out.println("\n===================================================================\n");

        // 메시지 생성
        fileMessageService.createMessage(m1);
        fileMessageService.createMessage(m2);

        // 메시지 단일 조회
        Message findM1 = fileMessageService.findMessage(m1.getId());
        System.out.println(findM1);

        // 메시지 전체 조회
        System.out.println(fileMessageService.findAllMessages());

        // 메시지 수정 후 조회
        fileMessageService.updateMessage(m2.getId(), "김밥 어때요 ?");
        System.out.println(fileMessageService.findMessage(m2.getId()));

        // 메시지 삭제
        fileMessageService.deleteMessage(m1.getId());
        System.out.println(fileMessageService.findAllMessages());

    }

}
