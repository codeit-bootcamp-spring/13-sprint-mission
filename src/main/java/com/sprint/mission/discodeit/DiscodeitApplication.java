package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class DiscodeitApplication {

	public static void main(String[] args) {

		ApplicationContext context =
				SpringApplication.run(DiscodeitApplication.class, args);

		UserService userService =
				context.getBean(UserService.class);

		ChannelService channelService =
				context.getBean(ChannelService.class);

		MessageService messageService =
				context.getBean(MessageService.class);

		User user = setupUser(userService);
		Channel channel = setupChannel(channelService);

		messageCreateTest(
				messageService,
				channel,
				user
		);

		updateTest(
				userService,
				channelService,
				messageService,
				user,
				channel
		);

		deleteTest(
				userService,
				channelService,
				messageService,
				user,
				channel
		);
	}

	private static User setupUser(UserService userService) {

		User user =
				new User("kim", "kim@test.com");

		return userService.create(user);
	}

	private static Channel setupChannel(ChannelService channelService) {

		Channel channel =
				new Channel(
						"general",
						"general chat"
				);

		return channelService.create(channel);
	}

	private static void messageCreateTest(
			MessageService messageService,
			Channel channel,
			User user
	) {

		Message message =
				new Message(
						"hello message",
						user.getId(),
						channel.getId()
				);

		messageService.create(message);

		System.out.println("===== CREATE TEST =====");
		System.out.println(user);
		System.out.println(channel);
		System.out.println(message);
	}

	private static void updateTest(
			UserService userService,
			ChannelService channelService,
			MessageService messageService,
			User user,
			Channel channel
	) {

		Message message =
				messageService.findAll().get(0);

		userService.update(
				user.getId(),
				"kim-updated",
				"updated@test.com"
		);

		channelService.update(
				channel.getId(),
				"random",
				"random channel"
		);

		messageService.update(
				message.getId(),
				"updated message"
		);

		System.out.println("===== UPDATE TEST =====");
		System.out.println(userService.find(user.getId()));
		System.out.println(channelService.find(channel.getId()));
		System.out.println(messageService.find(message.getId()));
	}

	private static void deleteTest(
			UserService userService,
			ChannelService channelService,
			MessageService messageService,
			User user,
			Channel channel
	) {

		Message message =
				messageService.findAll().get(0);

		userService.delete(user.getId());
		channelService.delete(channel.getId());
		messageService.delete(message.getId());

		System.out.println("===== DELETE TEST =====");
		System.out.println(userService.findAll());
		System.out.println(channelService.findAll());
		System.out.println(messageService.findAll());
	}
}