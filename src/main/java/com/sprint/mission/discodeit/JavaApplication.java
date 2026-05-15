package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

import java.util.UUID;

public class JavaApplication {

    public static void main(String[] args) {

        JCFUserService jcfUserService = new JCFUserService();
        JCFChannelService jcfChannelService = new JCFChannelService();
        JCFMessageService jcfMessageService = new JCFMessageService();

        User user1 = new User(
                UUID.randomUUID(),
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                "KIM",
                "kim@test.com",
                "1234",
                UserStatus.ONLINE
        );

        User user2 = new User(
                UUID.randomUUID(),
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                "KIM",
                "kim@test.com",
                "1234",
                UserStatus.ONLINE
        );




    }
}
