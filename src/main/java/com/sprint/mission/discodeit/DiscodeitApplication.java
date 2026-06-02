package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFUserRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DiscodeitApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscodeitApplication.class, args);

		System.out.println("============================= JCFService 테스트 ============================");

		UserRepository jcfUserRepository = new JCFUserRepository();
		UserService jcfUserService = new BasicUserService(jcfUserRepository);

		User user1 = new User(
				"혜령",
				"gpfud09@gmail.com",
				"1111@@"
		);

		// 생성
		jcfUserService.createUser(user1);

		// 단일 조회
		System.out.println(jcfUserService.findUser(user1.getId()));

		// 전체 조회
		System.out.println(jcfUserService.findAllUsers());

		// 수정 후 조회
		jcfUserService.updateUser(
				user1.getId(),
				"조혜령",
				"new@gmail.com",
				"2222@@"
		);
		System.out.println(jcfUserService.findUser(user1.getId()));

		// 삭제
		jcfUserService.deleteUser(user1.getId());

		// 삭제 후 전체 조회
		System.out.println(jcfUserService.findAllUsers());

		System.out.println("============================ FileService 테스트 ============================");

		UserRepository fileUserRepository = new FileUserRepository();
		UserService fileUserService = new BasicUserService(fileUserRepository);

		User user2 = new User(
				"영경",
				"young@gmail.com",
				"3333@@"
		);

		// 생성
		fileUserService.createUser(user2);

		// 단일 조회
		System.out.println(fileUserService.findUser(user2.getId()));

		// 전체 조회
		System.out.println(fileUserService.findAllUsers());

		// 수정 후 조회
		fileUserService.updateUser(
				user2.getId(),
				"김영경",
				"updated@gmail.com",
				"4444@@"
		);
		System.out.println(fileUserService.findUser(user2.getId()));

		// 삭제
		fileUserService.deleteUser(user2.getId());

		// 삭제 후 전체 조회
		System.out.println(fileUserService.findAllUsers());
	}

}
