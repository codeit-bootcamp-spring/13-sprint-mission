package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFBinaryContentRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFReadStatusRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFUserStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;

import static com.sprint.mission.discodeit.DiscodeitApplication.*;

public class JavaApplication {


    public static void main(String[] args) {
        // 레포지토리
        UserRepository userRepository = new FileUserRepository();
        ChannelRepository channelRepository = new FileChannelRepository();
        MessageRepository messageRepository = new FileMessageRepository();
        BinaryContentRepository binaryContentRepository = new JCFBinaryContentRepository();
        ReadStatusRepository readStatusRepository = new JCFReadStatusRepository();
        UserStatusRepository userStatusRepository = new JCFUserStatusRepository();

        // 서비스
        UserService userService = new BasicUserService(
                userRepository,
                binaryContentRepository,
                userStatusRepository
        );

        ChannelService channelService = new BasicChannelService(
                channelRepository,
                readStatusRepository,
                messageRepository,
                userRepository,
                binaryContentRepository
        );

        MessageService messageService = new BasicMessageService(
                messageRepository,
                channelRepository,
                userRepository,
                binaryContentRepository
        );
        // 셋업
        UserResponse user = setupUser(userService);
        ChannelResponse channel = setupChannel(channelService);

        // 테스트
        messageCreateTest(messageService, channel, user);
    }
}
