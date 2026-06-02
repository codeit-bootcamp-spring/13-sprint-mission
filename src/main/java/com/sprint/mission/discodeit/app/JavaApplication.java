package com.sprint.mission.discodeit.app;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFChannelRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFMessageRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFUserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;

import java.util.List;

public class JavaApplication {

    static User setupUser(UserService userService) {
        return userService.createUser("박경석", "aaa@gmail.com", "1234");
    }

    static Channel setupChannel(ChannelService channelService) {
        return channelService.createChannel("공지채널", "공지채널입니다!");
    }

    static void messageCreateTest(MessageService messageService, Channel channel, User author) {
        Message message = messageService.createContent("안녕하세요!", channel.getChannelId(), author.getUserId());
        System.out.println("메시지 생성: " + message.getMessageId());
    }

    public static void main(String[] args) {
//        //파일 저장
//        UserRepository userRepository = new FileUserRepository(Path.of("data/users.ser"));
//        ChannelRepository channelRepository = new FileChannelRepository(Path.of("data/channels.ser"));
//        MessageRepository messageRepository = new FileMessageRepository(Path.of("data/messages.ser"));
        // 메모리 저장
        UserRepository userRepository = new JCFUserRepository();
        ChannelRepository channelRepository = new JCFChannelRepository();
        MessageRepository messageRepository = new JCFMessageRepository();

        //BasicService 초기화
        UserService userService = new BasicUserService(userRepository);
        ChannelService channelService = new BasicChannelService(channelRepository);
        MessageService messageService = new BasicMessageService(messageRepository, userRepository, channelRepository);

        User user = setupUser(userService);
        Channel channel = setupChannel(channelService);
        messageCreateTest(messageService, channel, user);

        System.out.println("====================================== 각 기능 테스트 ======================================");


        //user 생성, 동일한 이메일 생성 예외처리 테스트
        try{
            User user1 = userService.createUser("박경석", "aaa@gmail.com", "1234");
        }catch (IllegalArgumentException e){
            System.out.println("예외발생: " + e.getMessage());
        }

        //user, channel, message 생성
        User user2 = userService.createUser("손흥민", "son@gmail.com", "7777");
        Channel channel1 = channelService.createChannel("학습채널", "같이 공부 하는 채널");
        Message content1 = messageService.createContent("반가워요!", channel.getChannelId(), user2.getUserId());

        System.out.println("====================================== 단건 조회 ======================================");
        //user 단건 조회
        User findByUser = userService.findByUser(user2.getUserId());
        System.out.println("사용자 조회: " + findByUser);
        //채널 조회
        Channel findByChannel = channelService.findByChannel(channel1.getChannelId());
        System.out.println("채널 조회: " + findByChannel);
        //메시지 조회
        Message findByMessage = messageService.findByMessage(content1.getMessageId());
        System.out.println("메시지 조회:" + findByMessage);


        System.out.println("====================================== 전체 조회 ======================================");
        //유저 전체 저회
        List<User> users = userService.findAllUser();
        System.out.println("전체 유저: " + users.size());
        System.out.println(users);

        //채널 전체 조회
        List<Channel> allChannel = channelService.findAllChannel();
        System.out.println("전체 채널: " + allChannel.size());
        System.out.println(allChannel);

        //채널 메시지 전체 조회
        List<Message> allByMessage = messageService.findAllByMessage(channel.getChannelId());
        System.out.println("공지채널 전체 메시지: " + allByMessage.size());
        System.out.println(allByMessage);

        System.out.println("====================================== 수정 ======================================");
        //유저 수정
        User updateUser = userService.updateUser(user.getUserId(), "박경석2", null, null);
        System.out.println(updateUser);

        //채널 수정
        Channel updateChannel = channelService.updateChannel(channel1.getChannelId(), "4팀 채널", "4팀 소통 채널 입니다.", ChannelType.PRIVATE);
        System.out.println(updateChannel);

        //메시지 수정
        Message updateContent = messageService.updateContent(content1.getMessageId(), "반가워요!!!!!");
        System.out.println(updateContent);


//      ===================================================== 삭제 테스트 =======================================================
        //유저 삭제
        userService.deleteUser(user.getUserId());
        //채널 삭제
        channelService.deleteChannel(channel1.getChannelId());

        //메시지 삭제
        messageService.deleteMessage(content1.getMessageId());


        System.out.println("====================================== 삭제 확인 ======================================");

        List<User> deleteUser = userService.findAllUser();
        System.out.println("총 사용자: " + deleteUser.size());

        List<Channel> deleteChannel = channelService.findAllChannel();
        System.out.println("전체 체널: " +  deleteChannel.size());

        List<Message> deleteMessage = messageService.findAllByMessage(channel.getChannelId());
        System.out.println("공지채널 전체 메시지: " +  deleteMessage.size());

    }
}

