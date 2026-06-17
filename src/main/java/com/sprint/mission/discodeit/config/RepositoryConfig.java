package com.sprint.mission.discodeit.config;


import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.repository.file.*;
import com.sprint.mission.discodeit.repository.jcf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RepositoryProperties.class)
@RequiredArgsConstructor
public class RepositoryConfig {

    private final RepositoryProperties properties;

    @Bean
    public UserRepository userRepository() {
        if (RepositoryType.FILE == (properties.getType())) {
            return new FileUserRepository(properties.getFILE_DIRECTORY());
        }
        return new JCFUserRepository();
    }

    @Bean
    public UserStatusRepository userStatusRepository() {
        if (RepositoryType.FILE == (properties.getType())) {
            return new FileUserStatusRepository(properties.getFILE_DIRECTORY());
        }
        return new JCFUserStatusRepository();
    }

    @Bean
    public ReadStatusRepository readStatusRepository() {
        if (RepositoryType.FILE == (properties.getType())) {
            return new FileReadStatusRepository(properties.getFILE_DIRECTORY());
        }
        return new JCFReadStatusRepository();
    }

    @Bean
    public MessageRepository messageRepository() {
        if (RepositoryType.FILE == (properties.getType())) {
            return new FileMessageRepository(properties.getFILE_DIRECTORY());
        }
        return new JCFMessageRepository();
    }

    @Bean
    public ChannelRepository channelRepository() {
        if (RepositoryType.FILE == (properties.getType())) {
            return new FileChannelRepository(properties.getFILE_DIRECTORY());
        }
        return new JCFChannelRepository();
    }

    @Bean
    public BinaryContentRepository binaryContentRepository() {
        if (RepositoryType.FILE == (properties.getType())) {
            return new FileBinaryContentRepository(properties.getFILE_DIRECTORY());
        }
        return new JCFBinaryContentRepository();
    }

}
