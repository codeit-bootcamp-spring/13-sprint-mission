package com.sprint.mission.discodeit.app;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.*;
import com.sprint.mission.discodeit.service.file.FileUserService;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

import javax.swing.plaf.LabelUI;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import static com.sprint.mission.discodeit.entity.ChannelType.PRIVATE;
import static com.sprint.mission.discodeit.entity.ChannelType.PUBLIC;

public class JavaApplication {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static void main(String[] args) {

        UserService userService = new JCFUserService();
        ChannelService channelService = new JCFChannelService();
        MessageService messageService = new JCFMessageService(userService, channelService);

        //user 생성
        User user1 = userService.createUser("박경석", "aaa@gmail.com","1234");
        User user2 = userService.createUser("사스케", "bbb@gmail.com","0000");
        User user3 = userService.createUser("나루토", "ccc@gmail.com","1111");

        //user 단건 조회
        System.out.println("========== 사용자 단건 조회 ==========");
        User foundUser = userService.findByUser(user1.getId());
        System.out.println("사용자 단건 조회: " + foundUser);

        //user 전체 조회
        System.out.println("========== 사용자 전체 조회 ==========");
        List<User> userList = userService.findAllUser();
        System.out.println("전체 사용자 조회 총: " + userList.size() + "명");
        userList.forEach(user -> System.out.println(
               "사용자 id: " + user.getId() + " 이름: " +user.getName() +
                       ", 이메일: " + user.getEmail() + ", 비밀번호: " + user.getPassword()));

        //user 수정 시간 검증
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("========== 사용자 수정 ==========");
        userService.updateUser(user1.getId(), "박경석2", "pks@naver.com", "1234");
        System.out.println(
                "사용자 id: " + user1.getId() +
                " 사용자: " + user1.getName() +
                ", 생성시간: " + sdf.format(new Date(user1.getCreatedAt())) +
                ", 수정시간: " + sdf.format(new Date(user1.getUpdatedAt())));

        //user 삭제
        System.out.println("========== 사용자 삭제 ==========");
        userService.deleteUser(user1.getId());

        //user 삭제확인
        ////사용자가 없을때 예외처리
        try {
            User foundUsers = userService.findByUser(user1.getId());
        }catch (NoSuchElementException e){
            System.out.println("예외 발생: " + e.getMessage());
        }

        //채널 생성
        Channel channel1 = channelService.createChannel("공지 채널","강의 공지를 참고 해 주세요!", PRIVATE);
        Channel channel2 = channelService.createChannel("소개 채널","본인을 소개해 보세요!");


        //채널 단건 조회
        System.out.println("========== 채널 단건 조회 ==========");
        Channel foundChannel = channelService.findByChannel(channel1.getId());
        System.out.println("채널 단건 조회: " +  foundChannel);

        //채널 전체 조회
        System.out.println("========== 채널 전체 조회 ==========");
        List<Channel> channelList = channelService.findAllChannel();
        System.out.println("전체 채널: " + channelList.size() + "개");
        channelList.forEach(channel -> System.out.println(
                "채널 id: " + channel.getId() + ", 채널명: " + channel.getName() + ", 채널 소개: " + channel.getDescription() +
                        ", 공개범위: " + channel.getChannelType().getDisplayName()));
        //수정 시간 검증
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("========== 채널 수정 ==========");
        channelService.updateChannel(channel1.getId(), "수업 자료 채널", "수업자료를 제공하는 채널 입니다.", PUBLIC);
        System.out.println(
                "채널 id: " + channel1.getId() + ", 채널명: " + channel1.getName() +
                        ", 채널 소개: " + channel1.getDescription() + ", 공개 범위: " + channel1.getChannelType().getDisplayName() +
                        ", 생성시간: " + sdf.format(new Date(channel1.getCreatedAt())) +
                        ", 수정시간: " + sdf.format(new Date(channel1.getUpdatedAt()))
        );
        System.out.println("========== 채널 삭제 후 조회 ==========");
        channelService.deleteChannel(channel1.getId());
        //채널 삭제 확인
        //채널이 없을 경우 예외 처리
        try{
            Channel foundChannel1 = channelService.findByChannel(channel1.getId());
        }catch (NoSuchElementException e){
            System.out.println("예외 발생: " + e.getMessage());
        }

        //메세지 생성
        Message message1 = messageService.createContent("안녕하세요!", channel2.getId(), user2.getId());
        Message message2 = messageService.createContent("반가워요!", channel2.getId(), user3.getId());

        //메세지 단건 조회
        System.out.println("========== 매세지 단건 조회 ==========");
        Message foundMessage =  messageService.findByMessage(message1.getId());
        System.out.println(foundMessage);

        //메세지 전체 조회
        System.out.println("========== 전체 메세지 조회 ==========");
        List<Message> messageList = messageService.findAllByMessage(channel2.getId());
        messageList.forEach(message ->{
            User author = userService.findByUser(message.getAuthorId());
            Channel channel = channelService.findByChannel(message.getChannelId());
            System.out.println(
                    "메시지id: " + message.getId() + ", 채널명: " + channel.getName() +
                            ", 보낸사람: " + author.getName() + ", 메세지: " + message.getContent());
        });

        // 메시지 수정시간 검증 로직
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //메세지 수정
        System.out.println("========== 메세지 수정 ==========");
        messageService.updateContent(message1.getId(), "hello");
        Channel channel = channelService.findByChannel(message1.getChannelId());
        User author = userService.findByUser(message1.getAuthorId());
        System.out.println(
                "메시지id: " + message1.getId() + ", 채널명: " + channel.getName() + ", 보낸사람: " + author.getName() +
                        ", 메시지: " + message1.getContent() + ", 생성시간: " + sdf.format(new Date(message1.getCreatedAt())) +
                        ", 수정시간: " +  sdf.format(new Date(message1.getUpdatedAt()))
        );

        //메세지 삭제
        System.out.println("========== 메시지 삭제 후 조회 ==========");
        messageService.deleteMessage(message1.getId());
        //메세지 삭제 확인
        //메세지 조회했을때 없으면 예외처리
        try {
            Message foundMessage1 =  messageService.findByMessage(message1.getId());
        }catch (NoSuchElementException e){
            System.out.println("예외발생: " + e.getMessage());
        }
    }
}

