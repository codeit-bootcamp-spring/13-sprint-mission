package com.sprint.mission.discodeit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class DiscodeitApplication {
	public static void main(String[] args) {
		SpringApplication.run(DiscodeitApplication.class, args);
	}
}

//	//user 생성
//	static List<UserResponse> setupUser (UserService userService) {
//		UserResponse user1 = userService.createUser(
//				new UserCreateRequest("박경석", "aaa@gmail.com", "1234"), null
//		);
//		UserResponse user2 = userService.createUser(
//				new UserCreateRequest("손흥민", "son@gmail.com","777"), null
//		);
//		UserResponse user3 = userService.createUser(
//				new UserCreateRequest("이강인", "lee@gmail.com","1111"), null
//		);
//		return List.of(user1, user2, user3);
//	}
//
//	//비공개 채널 생성
//	static ChannelResponse setupPrivateChannel(ChannelService channelService, UserResponse user1, UserResponse user2) {
//		List<UUID> participantIds = new ArrayList<>();
//		participantIds.add(user1.id());
//		participantIds.add(user2.id());
//
//		return channelService.createPrivateChannel(
//				new PrivateChannelRequest(participantIds)
//		);
//
//	}
//
//	//공개 채널 생성
//	static ChannelResponse setupPublicChannel(ChannelService channelService){
//		return channelService.createPublicChannel(
//				new PublicChannelRequest("공지채널", "공지를 위한 채널 입니다."));
//	}
//
//	//메시지 생성
//	static MessageResponse setupMessage(MessageService messageService, ChannelResponse channel, UserResponse user) {
//		return messageService.create(
//				new MessageCreateRequest(channel.id(),user.id(),"반가워요", null));
//	}

//	public static void main(String[] args) {
//		ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

//		UserService userService = context.getBean(UserService.class);
//		ChannelService channelService = context.getBean(ChannelService.class);
//		MessageService messageService = context.getBean(MessageService.class);
//
//		List<UserResponse> users = setupUser(userService);
//		UserResponse user1 = users.get(0);
//		UserResponse user2 = users.get(1);
//		UserResponse user3 = users.get(2);
//
//		setupPrivateChannel(channelService, user1, user2);
//		ChannelResponse channel = setupPublicChannel(channelService);
//		MessageResponse message = setupMessage(messageService, channel, user1);
//
//		//단건 조회
//		userService.findByUserId(user1.id());
//		channelService.findByChannelId(channel.id());
//
//		//전체 조회
//		userService.findAllUser();
//		channelService.findAllByUserId(user3.id());
//		messageService.findAllByChannelId(channel.id());
//
//		//수정
//		userService.updateUser(user1.id(),
//				new UserUpdateRequest(
//						"박경석1", null, null), null);
//
//		channelService.updateChannel(channel.id(),new ChannelUpdateRequest(
//				"소개 채널", "공지채널에서 소개 채널로 변경 했습니다~"));
//
//
//		messageService.updateMessage(message.id(), new MessageUpdateRequest("메롱이다!"));
//
//
//		// 삭제
//		userService.deleteUser(user3.id());
//		messageService.delete(message.id());
//		channelService.deleteChannel(channel.id());
//
//	}
//}
