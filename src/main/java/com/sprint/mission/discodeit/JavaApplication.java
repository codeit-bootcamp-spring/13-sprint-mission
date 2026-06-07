package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFUserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import com.sprint.mission.discodeit.service.file.FileChannelService;
import com.sprint.mission.discodeit.service.file.FileMessageService;
import com.sprint.mission.discodeit.service.file.FileUserService;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

import java.util.List;
import java.util.UUID;

import static com.sprint.mission.discodeit.entity.Channel.ChannelType.PRIVATE;
import static com.sprint.mission.discodeit.entity.Channel.ChannelType.PUBLIC;

public class JavaApplication {

    public static void main(String[] args) {

        System.out.println("============================= JCFService 테스트 ============================");

        UserRepository jcfUserRepository = new JCFUserRepository();
        UserService jcfUserService = new BasicUserService(jcfUserRepository);

        // 생성
        User user = jcfUserService.create("혜령", "gpfud09@gmail.com", "1111@@");

        // 단일 조회
        System.out.println(jcfUserService.find(user.getId()));

        // 전체 조회
        System.out.println(jcfUserService.findAll());

        // 수정 후 조회
        jcfUserService.update(user.getId(), "조혜령", "new@gmail.com", "2222@@"
        );
        System.out.println(jcfUserService.find(user.getId()));

        // 삭제
        jcfUserService.delete(user.getId());

        // 삭제 후 전체 조회
        System.out.println(jcfUserService.findAll());

        System.out.println("============================ FileService 테스트 ============================");

        UserRepository fileUserRepository = new FileUserRepository();
        UserService fileUserService = new BasicUserService(fileUserRepository);

        // 생성
        User user2 = fileUserService.create("영경", "young@gmail.com", "3333@@");

        // 단일 조회
        System.out.println(fileUserService.find(user2.getId()));

        // 전체 조회
        System.out.println(fileUserService.findAll());

        // 수정 후 조회
        fileUserService.update(
                user2.getId(),
                "김영경",
                "updated@gmail.com",
                "4444@@"
        );
        System.out.println(fileUserService.find(user2.getId()));

        // 삭제
        fileUserService.delete(user2.getId());

        // 삭제 후 전체 조회
        System.out.println(fileUserService.findAll());
    }

}
