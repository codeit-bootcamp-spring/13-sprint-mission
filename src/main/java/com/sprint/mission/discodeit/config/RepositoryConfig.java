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
        if ("file".equalsIgnoreCase(properties.getType())) {
            return new FileUserRepository(properties.getFileDirectory());
        }
        return new JCFUserRepository();
    }

    @Bean
    public UserStatusRepository userStatusRepository() {
        if ("file".equalsIgnoreCase(properties.getType())) {
            return new FileUserStatusRepository(properties.getFileDirectory());
        }
        return new JCFUserStatusRepository();
    }

    @Bean
    public ReadStatusRepository readStatusRepository() {
        if ("file".equalsIgnoreCase(properties.getType())) {
            return new FileReadStatusRepository(properties.getFileDirectory());
        }
        return new JCFReadStatusRepository();
    }

    @Bean
    public MessageRepository messageRepository() {
        if ("file".equalsIgnoreCase(properties.getType())) {
            return new FileMessageRepository(properties.getFileDirectory());
        }
        return new JCFMessageRepository();
    }

    @Bean
    public ChannelRepository channelRepository() {
        if ("file".equalsIgnoreCase(properties.getType())) {
            return new FileChannelRepository(properties.getFileDirectory());
        }
        return new JCFChannelRepository();
    }

    @Bean
    public BinaryContentRepository binaryContentRepository() {
        if ("file".equalsIgnoreCase(properties.getType())) {
            return new FileBinaryContentRepository(properties.getFileDirectory());
        }
        return new JCFBinaryContentRepository();
    }

}
