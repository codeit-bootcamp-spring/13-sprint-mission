package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.naming.Context;
import java.util.Collection;
import java.util.UUID;

@SpringBootApplication
public class DiscodeitApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

		UserService userService = context.getBean(UserService.class);
		ChannelService channelService = context.getBean(ChannelService.class);
		MessageService messageService = context.getBean(MessageService.class);

		// 유저 생성
		User user1 = userService.create("KJH", "KJH@kjh.com");
		User user2 = userService.create("UHJ", "UHJ@kjh.com");
		User user3 = userService.create("PMJ", "PMJ@kjh.com");
		User user4 = userService.create("JHS", "JHS@kjh.com");
		User user5 = userService.create("LHB", "LHB@kjh.com");


		System.out.println("\n============== 유저 생성 확인 ==================");
		System.out.println("All of members: " + userService.findAll().size());

		System.out.println("\n============== 유저 조회 (단건)) ==================");
		User found = userService.findById(user1.getId());
		if (found != null) {
			System.out.println(found);
		}

		System.out.println("\n============== 유저 조회 (다건)) ==================");
		Collection<User> allUsers = userService.findAll();
		for (User u : allUsers) {
			System.out.println(u);
		}

		System.out.println("\n============== 유저 수정 ==================");
		userService.update(user1.getId(), "LKM", "LKM@kjh.com");
		User updatedUser = userService.findById(user1.getId());
		System.out.println(updatedUser);


		System.out.println("\n============== 유저 삭제 및 조회==================");
		userService.delete(user1.getId());
		User deletedUser = userService.findById(user1.getId());
		if (deletedUser == null) {
			System.out.println("삭제 검증 조회 : 유저 없음. ");
		}else {
			System.out.println("삭제 실패 : "+ deletedUser);
		}
		System.out.println("All of members: " + userService.findAll().size());

		//채널
		System.out.println("\n============== 채널 생성 ==================");
		Channel channel1 = channelService.create(
				"Codeit" ,
				"Sprint Boot Camp",
				user5,
				ChannelType.TEXT);
		Channel channel2 = channelService.create(
				"Codeit2" ,
				"Sprint Boot Camp",
				user4,
				ChannelType.TEXT);

		System.out.println("All of channels: " + channelService.findAll().size());
		System.out.println("Channels: " + channelService.findById(channel1.getId()));


		System.out.println("\n============== 채널 조회 (단건)) ==================");
		Channel found1 = channelService.findById(channel1.getId());
		if (found1 != null) {
			System.out.println(found1);
		}

		System.out.println("\n============== 채널 조회 (다건)) ==================");
		Collection <Channel> allChannels = channelService.findAll();
		for (Channel channel : allChannels) {
			System.out.println(channel);
		}

		System.out.println("\n============== 채널 수정 ==================");
		channelService.update(channel1.getId(), "Laptop", "MacbookPro M1");
		Channel updatedchannel = channelService.findById(channel1.getId());
		System.out.println(updatedchannel);


		System.out.println("\n============== 채널 삭제 및 조회 ==================");
		channelService.delete(channel1.getId());
		Channel deletedchannel = channelService.findById(channel1.getId());
		if (deletedchannel == null) {
			System.out.println("채널 삭제 조회 : 완료. ");
		}else {
			System.out.println("채널 삭제 실패 :" +deletedchannel);
		}
		System.out.println("All of channels: " + channelService.findAll().size());

		//메세지
		System.out.println("\n============== 메세지 생성 ==================");
		Message message1 = messageService.create("How to feels today?" , channel2.getId(), user2.getId());
		Message message2 = messageService.create("I'm bad, b/c I was car lol" , channel2.getId(), user3.getId());
		Message message3 = messageService.create("So nice, toady.", channel2.getId() , user4.getId());
		Message message4 = messageService.create("It's good. HRU?" , channel2.getId(), user5.getId());
		Message message5 = messageService.create("User2, that's to bad." , channel2.getId(), user2.getId());


		System.out.println("All of Messages: " + messageService.findAll());

		System.out.println("\n============== 메세지 조회 (단건)) ==================");
		Message messageFound = messageService.findById(message1.getId());
		System.out.println(messageFound);

		System.out.println("\n============== 메세지 조회 (다건)) ==================");
		Collection <Message> allMessages = messageService.findAll();
		for (Message messages : allMessages) {
			System.out.println(messages);
		}

		System.out.println("\n============== 메세지 삭제 및 조회 ==================");
		messageService.delete(message2.getId());
		Message deletedmessage = messageService.findById(message2.getId());
		if (deletedmessage != null) {
			System.out.println("찾은 메세지 : " + deletedmessage);
		} else {
			System.out.println("더 이상 메세지를 찾을 수 없습니다.");
		};

		System.out.println("All of messages: " + messageService.findAll().size());

		System.out.println("\n============== 가짜 유저 검증 테스트 ==============");

		try {
			messageService.create("치트키 씁니다", channel2.getId(), UUID.randomUUID());

			System.out.println("검증 실패: 존재하지 않는 유저인데 메시지가 저장되었습니다.");
		} catch (IllegalArgumentException e) {
			System.out.println("검증 성공! 에러 메시지: " + e.getMessage());
		}

	}

}
