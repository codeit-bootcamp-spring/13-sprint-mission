package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.file.FileChannelService;
import com.sprint.mission.discodeit.service.file.FileMessageService;
import com.sprint.mission.discodeit.service.file.FileUserService;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

public class JavaApplication {

    public static void main(String[] args) {

        System.out.println("\n\nThis is Discodeit Application!");
        System.out.println("========= JCF 테스트 ==========\n================================");

        //JCF Service 객체들 생성
        JCFUserService userService = new JCFUserService();
        JCFChannelService channelService =  new JCFChannelService();
        JCFMessageService messageService = new JCFMessageService();


        //임시 객체들 생성
        User userTemp;
        Channel channelTemp;
        Message messageTemp;


        //유저 객체들 생성
        System.out.println("유저 객체들 생성\n");
        User user1 = userService.createUser("KimJH", "KJH@gmail.com");
        User user2 = userService.createUser("ParkMJ", "PMJ@gmail.com");
        User user3 = userService.createUser("YuuHJ", "YHJ@gmail.com");
        User user4 = userService.createUser("LeeHB", "LHB@gmail.com");
        User user5 = userService.createUser("JangHS", "JHS@gmail.com");
        System.out.println("============================\n");


        //채널 객체들 생성
        System.out.println("채널 객체들 생성\n");
        Channel channel1 = channelService.createChannel("Java", user1);
        Channel channel2 = channelService.createChannel("Spring", user1);
        Channel channel3 = channelService.createChannel("IntelliJ", user2);
        System.out.println("============================\n");


        //메세지 객체들 생성
        System.out.println("메세지 객체들 생성\n");
        Message message_U1_1 = messageService.createMessage(user1, channel1, "Java is GOOD~");
        Message message_U1_2 = messageService.createMessage(user1, channel2, "Spring is GOOD~");
        try {
            Message message_U2_1 = messageService.createMessage(user2, channel1, "I think so Too");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            Message message_U3_1 = messageService.createMessage(user3, channel2, "I don't Think so");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            Message message_U4_1 = messageService.createMessage(user4, channel3, "IntelliJ is GOOD~");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("============================\n");


        //유저를 채널에 등록 / 유저가 채널에 가입
        System.out.println("유저를 채널에 등록\n");
        channelService.addUserToChannel(channel1, user2);
        channelService.addUserToChannel(channel2, user2);
        channelService.addUserToChannel(channel2, user3);
        channelService.addUserToChannel(channel3, user4);
        userService.joinChannel(user3, channel3);
        userService.joinChannel(user4, channel1);
        userService.joinChannel(user5, channel1);
        userService.joinChannel(user5, channel2);
        userService.joinChannel(user5, channel3);
        System.out.println("============================\n");


        //메세지 객체 재생성
        System.out.println("아까 예외처리로 생성 못했던 메세지 객체 재생성\n");
        Message message_U2_1 = messageService.createMessage(user2, channel1, "I think so Too");
        Message message_U3_1 = messageService.createMessage(user3, channel2, "I don't Think so");
        Message message_U4_1 = messageService.createMessage(user4, channel3, "IntelliJ is GOOD~");
        Message message_U5_1 = messageService.createMessage(user5, channel3, "Yeah~ Maybe.");
        Message message_U3_2 = messageService.createMessage(user3, channel3, "I Think IntelliJ is God.");
        Message message_U4_2 = messageService.createMessage(user4, channel3, "That's What Im talking about!");
        System.out.println("============================\n");


        // 유저 정보 출력 테스트
        System.out.println("유저 정보 출력 테스트\n");
        // 단건
        System.out.println("단건: \n");
        userService.printUserInfo(user2);
        // 다건
        System.out.println("다건: \n");
        userService.printAllUsersInfo();
        System.out.println("============================\n");


        // 채널 정보 출력 테스트
        System.out.println("채널 정보 출력 테스트\n");
        // 단건
        System.out.println("단건: \n");
        channelService.printChannelInfo(channel2);
        // 다건
        System.out.println("다건: \n");
        channelService.printAllChannelsInfo();
        System.out.println("============================\n");


        //메세지 정보 출력 테스트
        System.out.println("메세지 정보 출력 테스트\n");
        // 단건
        System.out.println("단건: \n");
        messageService.printMessage(message_U2_1);
        // 다건
        System.out.println("다건: \n");
        messageService.printAllMessages();
        System.out.println("============================\n");


        //특정 채널에 존재하는 메세지 읽기(출력) 테스트
        System.out.println("특정 채널에 존재하는 메세지 읽기(출력) 테스트\n");

        channelService.printMessages(channel1);
        System.out.println("============================\n");


        //특정 채널에 가입한 유저 정보 읽기(출력) 테스트
        System.out.println("특정 채널에 가입한 유저 정보 읽기(출력) 테스트\n");
        channelService.printUsersInfo(channel1);
        System.out.println("============================\n");


        // 유저별 가입한 채널 읽기(출력) 테스트
        System.out.println("유저별 가입한 채널 읽기(출력) 테스트\n");
        userService.printMyChannelsInfo(user1);
        System.out.println("============================\n");


        //유저별 작성한 메세지 읽기(출력) 테스트
        System.out.println("유저별 작성한 메세지 읽기(출력) 테스트\n");
        userService.printMessages(user1);
        System.out.println("============================\n");


        //이름 변경 테스트
        System.out.println("이름 변경 테스트\n");
        userService.changeName(user1, "김정현");
        try {
            userService.changeName(user2, "");
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        userService.changeName(user2, "박민재");
        userService.changeName(user3, "유하정");
        userService.changeName(user4, "이해빈");
        userService.changeName(user5, "장현서");
        System.out.println("============================\n");


        //이메일 변경 테스트
        System.out.println("이메일 변경 테스트\n");
        userService.changeEmail(user1, "김김김@gmail.com");
        userService.changeEmail(user2, "박박박@gmail.com");
        userService.changeEmail(user3, "유유유@gmail.com");
        userService.changeEmail(user4, "이이이@gmail.com");
        userService.changeEmail(user5, "장장장@gmail.com");
        System.out.println("============================\n");


        //수정된 정보 확인용 출력
        System.out.println("수정된 정보 확인용 출력\n");
        userService.printAllUsersInfo();
        System.out.println("============================\n");


        // 채널 이름 수정 테스트
        System.out.println("채널 이름 수정 테스트\n");
        try {
            channelService.editChannelName(channel1, user2, "자바");
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        channelService.editChannelName(channel1, user1, "자바");
        channelService.editChannelName(channel2, user1, "스프링");
        try {
            channelService.editChannelName(channel3, user1, "인텔리제이");
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        try {
            channelService.editChannelName(channel3, user2, " ");
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        channelService.editChannelName(channel3, user2, "인텔리제이");
        System.out.println("============================\n");


        // 채널 호스트 변경 테스트
        System.out.println("채널 호스트 변경 테스트\n");
        try {
            channelService.changeChannelHost(channel1, user3);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        channelService.changeChannelHost(channel1, user2);
        channelService.changeChannelHost(channel3, user4);
        System.out.println("============================\n");


        //수정된 정보 확인용 출력
        System.out.println("수정된 정보 확인용 출력\n");
        channelService.printAllChannelsInfo();
        System.out.println("============================\n");


        // 메세지 수정 테스트
        System.out.println("메세지 수정 테스트\n");
        messageService.editMessage(message_U1_1, user1, "자바는 최고야!");
        try {
            messageService.editMessage(message_U2_1, user3, "저도 그렇게 생각해요!");
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        messageService.editMessage(message_U2_1, user2, "저도 그렇게 생각해요!");
        messageService.editMessage(message_U1_2, user1, "스프링은 최고야!");
        messageService.editMessage(message_U3_1, user3, "아닌 것 같은데..");
        System.out.println("============================\n");


        //수정된 정보 확인용 출력
        System.out.println("수정된 정보 확인용 출력\n");
        messageService.printAllMessages();
        System.out.println("============================\n");


        // 채널 탈퇴 테스트
        System.out.println("채널 탈퇴 테스트\n");
        //임시 유저 생성 및 특정 채널에 가입 후 해당 채널 인원들 정보 출력
        System.out.println("임시 유저 생성 및 특정 채널에 가입 후 해당 채널 인원들 정보 출력\n");
        userTemp = userService.createUser("TempUser", "temp@gmail.com");
        userService.joinChannel(userTemp, channel1);
        channelService.printUsersInfo(channel1);
        //임시 유저가 해당 채널 탈퇴 및 해당 채널 인원 출력
        System.out.println("임시 유저가 해당 채널 탈퇴 및 해당 채널 인원 출력\n");
        userService.leaveChannel(userTemp, channel1);
        channelService.printUsersInfo(channel1);
        System.out.println("테스트 마친 임시 유저 삭제\n");
        userTemp = userService.deleteUser(userTemp);
        System.out.println("============================\n");


        // 유저 삭제 테스트
        System.out.println("유저 삭제 테스트\n");
        //임시 유저 생성 및 특정 채널에 해당 유저 참가 후 해당 채널 유저 정보 출력
        System.out.println("임시 유저 생성 및 특정 채널에 해당 유저 참가 후 해당 채널 유저 정보 출력\n");
        userTemp = userService.createUser("TempUser", "temp@gmail.com");
        channelService.addUserToChannel(channel1, userTemp);
        channelService.printUsersInfo(channel1);
        //임시 유저 삭제 및 채널 인원 출력
        System.out.println("임시 유저 삭제 및 채널 인원 출력\n");
        userTemp = userService.deleteUser(userTemp);
        channelService.printUsersInfo(channel1);
        System.out.println("============================\n");


        // 채널 삭제 테스트
        System.out.println("채널 삭제 테스트\n");
        //임시 채널 생성 및 특정 유저가 해당 채널에 가입 후, 해당 유저가 가입한 채널들 출력
        System.out.println("임시 채널 생성 및 특정 유저가 해당 채널에 가입 후, 해당 유저가 가입한 채널들 출력\n");
        channelTemp = channelService.createChannel("TempChannel", user1);
        userService.printMyChannelsInfo(user1);
        //채널 삭제 후 해당 유저가 가입한 채널들 출력
        System.out.println("채널 삭제 후 해당 유저가 가입한 채널들 출력\n");
        channelTemp = channelService.deleteChannel(channelTemp, user1);
        userService.printMyChannelsInfo(user1);
        System.out.println("============================\n");


        // 메세지 삭제 테스트
        System.out.println("메세지 삭제 테스트\n");
        //임시 메세지 생성 후 해당 채널의 메세지들 출력
        System.out.println("임시 메세지 생성 후 해당 채널의 메세지들 출력\n");
        messageTemp = messageService.createMessage(user1, channel1, "아~~~~잠온다~~~~");
        channelService.printMessages(channel1);
        //임시 메세지 삭제 후 해당 채널의 메세지들 출력
        System.out.println("임시 메세지 삭제 후 해당 채널의 메세지들 출력\n");
        messageService.deleteMessage(messageTemp, user1);
        channelService.printMessages(channel1);
        System.out.println("============================\n");



//        System.out.println("========= File IO 테스트 ==========\n================================");
//
//        //FileService 객체들 생성
//        FileUserService userService = new FileUserService();
//        FileChannelService channelService =  new FileChannelService();
//        FileMessageService messageService = new FileMessageService();
//
//
//        //임시 객체들 생성
//        User userTemp;
//        Channel channelTemp;
//        Message messageTemp;
//
//
//        //유저 객체들 생성
//        System.out.println("유저 객체들 생성\n");
//        User user1 = userService.createUser("KimJH", "KJH@gmail.com");
//        User user2 = userService.createUser("ParkMJ", "PMJ@gmail.com");
//        User user3 = userService.createUser("YuuHJ", "YHJ@gmail.com");
//        User user4 = userService.createUser("LeeHB", "LHB@gmail.com");
//        User user5 = userService.createUser("JangHS", "JHS@gmail.com");
//        System.out.println("============================\n");
//
//
//        //채널 객체들 생성
//        System.out.println("채널 객체들 생성\n");
//        Channel channel1 = channelService.createChannel("Java", user1);
//        Channel channel2 = channelService.createChannel("Spring", user1);
//        Channel channel3 = channelService.createChannel("IntelliJ", user2);
//        System.out.println("============================\n");
//
//
//        //메세지 객체들 생성
//        System.out.println("메세지 객체들 생성\n");
//        Message message_U1_1 = messageService.createMessage(user1, channel1, "Java is GOOD~");
//        Message message_U1_2 = messageService.createMessage(user1, channel2, "Spring is GOOD~");
//        try {
//            Message message_U2_1 = messageService.createMessage(user2, channel1, "I think so Too");
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
//        try {
//            Message message_U3_1 = messageService.createMessage(user3, channel2, "I don't Think so");
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
//        try {
//            Message message_U4_1 = messageService.createMessage(user4, channel3, "IntelliJ is GOOD~");
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
//        System.out.println("============================\n");
//
//
//        //유저를 채널에 등록 / 유저가 채널에 가입
//        System.out.println("유저를 채널에 등록\n");
//        channelService.addUserToChannel(channel1, user2);
//        channelService.addUserToChannel(channel2, user2);
//        channelService.addUserToChannel(channel2, user3);
//        channelService.addUserToChannel(channel3, user4);
//        userService.joinChannel(user3, channel3);
//        userService.joinChannel(user4, channel1);
//        userService.joinChannel(user5, channel1);
//        userService.joinChannel(user5, channel2);
//        userService.joinChannel(user5, channel3);
//        System.out.println("============================\n");
//
//
//        //메세지 객체 재생성
//        System.out.println("아까 예외처리로 생성 못했던 메세지 객체 재생성\n");
//        Message message_U2_1 = messageService.createMessage(user2, channel1, "I think so Too");
//        Message message_U3_1 = messageService.createMessage(user3, channel2, "I don't Think so");
//        Message message_U4_1 = messageService.createMessage(user4, channel3, "IntelliJ is GOOD~");
//        Message message_U5_1 = messageService.createMessage(user5, channel3, "Yeah~ Maybe.");
//        Message message_U3_2 = messageService.createMessage(user3, channel3, "I Think IntelliJ is God.");
//        Message message_U4_2 = messageService.createMessage(user4, channel3, "That's What Im talking about!");
//        System.out.println("============================\n");
//
//
//        // 유저 정보 출력 테스트
//        System.out.println("유저 정보 출력 테스트\n");
//        // 단건
//        System.out.println("단건: \n");
//        userService.printUserInfo(user2);
//        // 다건
//        System.out.println("다건: \n");
//        userService.printAllUsersInfo();
//        System.out.println("============================\n");
//
//
//        // 채널 정보 출력 테스트
//        System.out.println("채널 정보 출력 테스트\n");
//        // 단건
//        System.out.println("단건: \n");
//        channelService.printChannelInfo(channel2);
//        // 다건
//        System.out.println("다건: \n");
//        channelService.printAllChannelsInfo();
//        System.out.println("============================\n");
//
//
//        //메세지 정보 출력 테스트
//        System.out.println("메세지 정보 출력 테스트\n");
//        // 단건
//        System.out.println("단건: \n");
//        messageService.printMessage(message_U2_1);
//        // 다건
//        System.out.println("다건: \n");
//        messageService.printAllMessages();
//        System.out.println("============================\n");
//
//
//        //특정 채널에 존재하는 메세지 읽기(출력) 테스트
//        System.out.println("특정 채널에 존재하는 메세지 읽기(출력) 테스트\n");
//
//        channelService.printMessages(channel1);
//        System.out.println("============================\n");
//
//
//        //특정 채널에 가입한 유저 정보 읽기(출력) 테스트
//        System.out.println("특정 채널에 가입한 유저 정보 읽기(출력) 테스트\n");
//        channelService.printUsersInfo(channel1);
//        System.out.println("============================\n");
//
//
//        // 유저별 가입한 채널 읽기(출력) 테스트
//        System.out.println("유저별 가입한 채널 읽기(출력) 테스트\n");
//        userService.printMyChannelsInfo(user1);
//        System.out.println("============================\n");
//
//
//        //유저별 작성한 메세지 읽기(출력) 테스트
//        System.out.println("유저별 작성한 메세지 읽기(출력) 테스트\n");
//        userService.printMessages(user1);
//        System.out.println("============================\n");
//
//
//        //이름 변경 테스트
//        System.out.println("이름 변경 테스트\n");
//        userService.changeName(user1, "김정현");
//        try {
//            userService.changeName(user2, "");
//        } catch (Exception e){
//            System.out.println(e.getMessage());
//        }
//        userService.changeName(user2, "박민재");
//        userService.changeName(user3, "유하정");
//        userService.changeName(user4, "이해빈");
//        userService.changeName(user5, "장현서");
//        System.out.println("============================\n");
//
//
//        //이메일 변경 테스트
//        System.out.println("이메일 변경 테스트\n");
//        userService.changeEmail(user1, "김김김@gmail.com");
//        userService.changeEmail(user2, "박박박@gmail.com");
//        userService.changeEmail(user3, "유유유@gmail.com");
//        userService.changeEmail(user4, "이이이@gmail.com");
//        userService.changeEmail(user5, "장장장@gmail.com");
//        System.out.println("============================\n");
//
//
//        //수정된 정보 확인용 출력
//        System.out.println("수정된 정보 확인용 출력\n");
//        userService.printAllUsersInfo();
//        System.out.println("============================\n");
//
//
//        // 채널 이름 수정 테스트
//        System.out.println("채널 이름 수정 테스트\n");
//        try {
//            channelService.editChannelName(channel1, user2, "자바");
//        } catch (Exception e){
//            System.out.println(e.getMessage());
//        }
//        channelService.editChannelName(channel1, user1, "자바");
//        channelService.editChannelName(channel2, user1, "스프링");
//        try {
//            channelService.editChannelName(channel3, user1, "인텔리제이");
//        } catch (Exception e){
//            System.out.println(e.getMessage());
//        }
//        try {
//            channelService.editChannelName(channel3, user2, " ");
//        } catch (Exception e){
//            System.out.println(e.getMessage());
//        }
//        channelService.editChannelName(channel3, user2, "인텔리제이");
//        System.out.println("============================\n");
//
//
//        // 채널 호스트 변경 테스트
//        System.out.println("채널 호스트 변경 테스트\n");
//        try {
//            channelService.changeChannelHost(channel1, user3);
//        } catch (Exception e){
//            System.out.println(e.getMessage());
//        }
//        channelService.changeChannelHost(channel1, user2);
//        channelService.changeChannelHost(channel3, user4);
//        System.out.println("============================\n");
//
//
//        //수정된 정보 확인용 출력
//        System.out.println("수정된 정보 확인용 출력\n");
//        channelService.printAllChannelsInfo();
//        System.out.println("============================\n");
//
//
//        // 메세지 수정 테스트
//        System.out.println("메세지 수정 테스트\n");
//        messageService.editMessage(message_U1_1, user1, "자바는 최고야!");
//        try {
//            messageService.editMessage(message_U2_1, user3, "저도 그렇게 생각해요!");
//        } catch (Exception e){
//            System.out.println(e.getMessage());
//        }
//        messageService.editMessage(message_U2_1, user2, "저도 그렇게 생각해요!");
//        messageService.editMessage(message_U1_2, user1, "스프링은 최고야!");
//        messageService.editMessage(message_U3_1, user3, "아닌 것 같은데..");
//        System.out.println("============================\n");
//
//
//        //수정된 정보 확인용 출력
//        System.out.println("수정된 정보 확인용 출력\n");
//        messageService.printAllMessages();
//        System.out.println("============================\n");
//
//
//        // 채널 탈퇴 테스트
//        System.out.println("채널 탈퇴 테스트\n");
//        //임시 유저 생성 및 특정 채널에 가입 후 해당 채널 인원들 정보 출력
//        System.out.println("임시 유저 생성 및 특정 채널에 가입 후 해당 채널 인원들 정보 출력\n");
//        userTemp = userService.createUser("TempUser", "temp@gmail.com");
//        userService.joinChannel(userTemp, channel1);
//        channelService.printUsersInfo(channel1);
//        //임시 유저가 해당 채널 탈퇴 및 해당 채널 인원 출력
//        System.out.println("임시 유저가 해당 채널 탈퇴 및 해당 채널 인원 출력\n");
//        userService.leaveChannel(userTemp, channel1);
//        channelService.printUsersInfo(channel1);
//        System.out.println("테스트 마친 임시 유저 삭제\n");
//        userTemp = userService.deleteUser(userTemp);
//        System.out.println("============================\n");
//
//
//        // 유저 삭제 테스트
//        System.out.println("유저 삭제 테스트\n");
//        //임시 유저 생성 및 특정 채널에 해당 유저 참가 후 해당 채널 유저 정보 출력
//        System.out.println("임시 유저 생성 및 특정 채널에 해당 유저 참가 후 해당 채널 유저 정보 출력\n");
//        userTemp = userService.createUser("TempUser", "temp@gmail.com");
//        channelService.addUserToChannel(channel1, userTemp);
//        channelService.printUsersInfo(channel1);
//        //임시 유저 삭제 및 채널 인원 출력
//        System.out.println("임시 유저 삭제 및 채널 인원 출력\n");
//        userTemp = userService.deleteUser(userTemp);
//        channelService.printUsersInfo(channel1);
//        System.out.println("============================\n");
//
//
//        // 채널 삭제 테스트
//        System.out.println("채널 삭제 테스트\n");
//        //임시 채널 생성 및 특정 유저가 해당 채널에 가입 후, 해당 유저가 가입한 채널들 출력
//        System.out.println("임시 채널 생성 및 특정 유저가 해당 채널에 가입 후, 해당 유저가 가입한 채널들 출력\n");
//        channelTemp = channelService.createChannel("TempChannel", user1);
//        userService.printMyChannelsInfo(user1);
//        //채널 삭제 후 해당 유저가 가입한 채널들 출력
//        System.out.println("채널 삭제 후 해당 유저가 가입한 채널들 출력\n");
//        channelTemp = channelService.deleteChannel(channelTemp, user1);
//        userService.printMyChannelsInfo(user1);
//        System.out.println("============================\n");
//
//
//        // 메세지 삭제 테스트
//        System.out.println("메세지 삭제 테스트\n");
//        //임시 메세지 생성 후 해당 채널의 메세지들 출력
//        System.out.println("임시 메세지 생성 후 해당 채널의 메세지들 출력\n");
//        messageTemp = messageService.createMessage(user1, channel1, "아~~~~잠온다~~~~");
//        channelService.printMessages(channel1);
//        //임시 메세지 삭제 후 해당 채널의 메세지들 출력
//        System.out.println("임시 메세지 삭제 후 해당 채널의 메세지들 출력\n");
//        messageTemp = messageService.deleteMessage(messageTemp, user1);
//        channelService.printMessages(channel1);
//        System.out.println("============================\n");
//


    }
}
