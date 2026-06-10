package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.PublicChannelRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DiscodeitApplication {

	//user, channel, content 생성 테스트
	static UserResponse setupUser(UserService userService) {
		return userService.createUser(
				new UserCreateRequest("박경석", "aaa@gmail.com", "1234"), null
		);
	}
	static ChannelResponse setupChannel(ChannelService channelService) {
		return channelService.createPublicChannel(
				new PublicChannelRequest("소개채널", "본인을 소개 해주세요!")
		);

	}
	static void messageCreateTest(MessageService messageService, ChannelResponse channel, UserResponse user) {
		Message message = messageService.create(
				new MessageCreateRequest(channel.id(), user.id(), "안녕하세요",null)
		);
		System.out.println(message);
	}

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

		UserService userService = context.getBean(UserService.class);
		ChannelService channelService = context.getBean(ChannelService.class);
		MessageService messageService = context.getBean(MessageService.class);

		UserResponse user = setupUser(userService);
		ChannelResponse channel = setupChannel(channelService);
		messageCreateTest(messageService, channel, user);
	}
}
