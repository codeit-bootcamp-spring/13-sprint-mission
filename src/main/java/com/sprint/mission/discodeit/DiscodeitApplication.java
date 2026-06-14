package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Collection;
import java.util.UUID;

@Slf4j
@SpringBootApplication
public class DiscodeitApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

		UserService userService = context.getBean(UserService.class);
		ChannelService channelService = context.getBean(ChannelService.class);
		MessageService messageService = context.getBean(MessageService.class);

		// 유저 생성
		UserResponse user1 = userService.create(new UserCreateRequest("KJH", "KJH@kjh.com", "1234", null, null, null));
		UserResponse user2 = userService.create(new UserCreateRequest("UHJ", "UHJ@kjh.com", "1234", null, null, null));
		UserResponse user3 = userService.create(new UserCreateRequest("PMJ", "PMJ@kjh.com", "1234", null, null, null));
		UserResponse user4 = userService.create(new UserCreateRequest("JHS", "JHS@kjh.com", "1234", null, null, null));
		UserResponse user5 = userService.create(new UserCreateRequest("LHB", "LHB@kjh.com", "1234", null, null, null));


		log.info("============== 유저 생성 확인 ==================");
		log.info("All of members: {}", userService.findAll().size());

		log.info("============== 유저 조회 (단건) ==================");
		UserResponse found = userService.findById(user1.id());
		if (found != null) {
			log.info("found user: {}", found);
		}

		log.info("============== 유저 조회 (다건) ==================");
		Collection<UserResponse> allUsers = userService.findAll();
		for (UserResponse user : allUsers) {
			log.info("user: {}", user);
		}

		log.info("============== 유저 수정 ==================");
		userService.update(new UserUpdateRequest(
				user1.id(),
				"LKM",
				"LKM@kjh.com",
				"5678",
				null,
				null,
				null
		));

		UserResponse updatedUser = userService.findById(user1.id());
		log.info("updated user: {}", updatedUser);

		log.info("============== 유저 삭제 및 조회 ==================");
		userService.delete(user1.id());

		UserResponse deletedUser = userService.findById(user1.id());
		if (deletedUser == null) {
			log.info("삭제 검증 조회 : 유저 없음.");
		} else {
			log.info("삭제 실패 : {}", deletedUser);
		}

		log.info("All of members: {}", userService.findAll().size());

		//채널
		// 채널
		log.info("============== 채널 생성 ==================");

		ChannelResponse channel1 = channelService.createPublic(
				new PublicChannelCreateRequest(
						"Codeit",
						"Sprint Boot Camp",
						user5.id()
				)
		);

		ChannelResponse channel2 = channelService.createPublic(
				new PublicChannelCreateRequest(
						"Codeit2",
						"Sprint Boot Camp",
						user4.id()
				)
		);

		log.info("All of channels: {}", channelService.findAllByUserId(user5.id()).size());
		log.info("channel: {}", channelService.findById(channel1.id()));

		log.info("============== 채널 조회 (단건) ==================");
		ChannelResponse foundChannel = channelService.findById(channel1.id());
		if (foundChannel != null) {
			log.info("found channel: {}", foundChannel);
		}

		log.info("============== 채널 조회 (다건) ==================");
		Collection<ChannelResponse> allChannels = channelService.findAllByUserId(user5.id());
		for (ChannelResponse channel : allChannels) {
			log.info("channel: {}", channel);
		}

		log.info("============== 채널 수정 ==================");
		channelService.update(new ChannelUpdateRequest(
				channel1.id(),
				"Laptop",
				"MacbookPro M1"
		));

		ChannelResponse updatedChannel = channelService.findById(channel1.id());
		log.info("updated channel: {}", updatedChannel);

		log.info("============== 채널 삭제 및 조회 ==================");
		channelService.delete(channel1.id());

		ChannelResponse deletedChannel = channelService.findById(channel1.id());
		if (deletedChannel == null) {
			log.info("채널 삭제 조회 : 완료.");
		} else {
			log.info("채널 삭제 실패 : {}", deletedChannel);
		}

		log.info("All of channels: {}", channelService.findAllByUserId(user5.id()).size());

		// 메세지
		log.info("============== 메세지 생성 ==================");

		MessageResponse message1 = messageService.create(
				new MessageCreateRequest(
						"How to feels today?",
						channel2.id(),
						user2.id(),
						null
				)
		);

		MessageResponse message2 = messageService.create(
				new MessageCreateRequest(
						"I'm bad, b/c I was car lol",
						channel2.id(),
						user3.id(),
						null
				)
		);

		MessageResponse message3 = messageService.create(
				new MessageCreateRequest(
						"So nice, today.",
						channel2.id(),
						user4.id(),
						null
				)
		);

		MessageResponse message4 = messageService.create(
				new MessageCreateRequest(
						"It's good. HRU?",
						channel2.id(),
						user5.id(),
						null
				)
		);

		MessageResponse message5 = messageService.create(
				new MessageCreateRequest(
						"User2, that's too bad.",
						channel2.id(),
						user2.id(),
						null
				)
		);

		log.info("created messages: {}", messageService.findAllByChannelId(channel2.id()));

		log.info("============== 메세지 조회 (단건) ==================");
		MessageResponse messageFound = messageService.findById(message1.id());
		log.info("found message: {}", messageFound);

		log.info("============== 메세지 조회 (다건) ==================");
		Collection<MessageResponse> allMessages = messageService.findAllByChannelId(channel2.id());
		for (MessageResponse message : allMessages) {
			log.info("message: {}", message);
		}

		log.info("============== 메세지 삭제 및 조회 ==================");
		messageService.delete(message2.id());

		MessageResponse deletedMessage = messageService.findById(message2.id());
		if (deletedMessage != null) {
			log.info("찾은 메세지 : {}", deletedMessage);
		} else {
			log.info("더 이상 메세지를 찾을 수 없습니다.");
		}

		log.info("All of messages: {}", messageService.findAllByChannelId(channel2.id()).size());

		log.info("============== 가짜 유저 검증 테스트 ==============");

		try {
			messageService.create(
					new MessageCreateRequest(
							"치트키 씁니다",
							channel2.id(),
							UUID.randomUUID(),
							null
					)
			);

			log.info("검증 실패: 존재하지 않는 유저인데 메시지가 저장되었습니다.");
		} catch (IllegalArgumentException e) {
			log.info("검증 성공! 에러 메시지: {}", e.getMessage());
		}
	}
}
