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

import java.util.List;

@SpringBootApplication
public class DiscodeitApplication {

	static User setupUser(UserService userService) {
		try {
			User user = userService.create("woody", "woody@codeit.com", "woody1234", null);
			return user;
		} catch (IllegalArgumentException e) {
			System.out.println("이미 존재하는 유저: " + e.getMessage());
			return null;
		}
	}

	static Channel setupChannel(ChannelService channelService) {
		Channel channel = channelService.createPublic("공지", "공지 채널입니다.");
		return channel;
	}

	static void messageCreateTest(MessageService messageService, Channel channel, User author) {
		Message message = messageService.create("안녕하세요.", channel.getId(), author.getId(), List.of());
		System.out.println("메시지 생성: " + message.getId());
	}

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

		UserService userService = context.getBean(UserService.class);
		ChannelService channelService = context.getBean(ChannelService.class);
		MessageService messageService = context.getBean(MessageService.class);

		User user = setupUser(userService);
		if (user != null) {
			Channel channel = setupChannel(channelService);
			messageCreateTest(messageService, channel, user);
		}
	}
}