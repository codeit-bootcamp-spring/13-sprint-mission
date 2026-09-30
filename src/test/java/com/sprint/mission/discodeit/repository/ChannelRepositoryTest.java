package com.sprint.mission.discodeit.repository;


import com.sprint.mission.discodeit.config.QueryDslTestConfig;
import com.sprint.mission.discodeit.dto.projection.ChannelProjection;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
//@TestPropertySource(properties = {
//        "spring.jpa.properties.hibernate.generate_statistics=true"
//})
@Import({
        QueryDslTestConfig.class
})
@ActiveProfiles("test")
@Slf4j
public class ChannelRepositoryTest {
    @Autowired
    ChannelRepository channelRepository;
    @Autowired
    TestEntityManager em;

    @MockitoBean
    BinaryContentStorage binaryContentStorage;

    private final List<User> users = new ArrayList<>();
    private final List<Channel> channels = new ArrayList<>();

    private void channel(boolean publicChecker, User user, Integer index){
        Channel newChannel;
        if (!publicChecker) {
            newChannel = new Channel(index.toString() ,null,ChannelType.PRIVATE);
            ReadStatus newReadStatus = new ReadStatus(user, newChannel, Instant.now());

            em.persist(newChannel);
            em.persist(newReadStatus);

        } else {
            newChannel = new Channel(index.toString(), null, ChannelType.PUBLIC);
            em.persist(newChannel);
        }

        channels.add(newChannel);
    }

    private User user(String name, String email){
        User user = new User(name,email,"password",null,null);
        users.add(user);
        em.persist(user);

        return user;
    }


    @BeforeEach
    void setTestEnv(){
        User ksk = user("김숙희","ksk@email.com");
        User ehe = user("이하이","ehe@email.com");

        channel(true,ksk,1);
        channel(false,ksk,2);
        channel(true,ehe,3);
        channel(false,ehe,4);

        em.flush();
        em.clear();
    }


    // todo - config 에 clock 을 설정해서 test 용 entity 매니저 시간 조정.
    // todo - lastMessageAt 검정.
    @Test
    @DisplayName("getChannelsFromUserId 매서드 테스트 - userId 가 조회할 수 있는 모든 채널 조회")
    void testQueryableChannelByUSer(){
        // 김숙희 유저 기준.
        User target = users.get(0);

        // 정답은 channel 1,2,3
        List<UUID> currectChannelIdList = Stream.of(
                channels.get(0),
                channels.get(1),
                channels.get(2)
        ).map(Channel::getId).toList();

        List<ChannelProjection> result = channelRepository.getChannelsFromUserId(target.getId())
                .values().stream().toList();


        // 유저가 조회 가능한 채널 -> ksk 의 생성채널 + public = 3개.
        assertThat(result).hasSize(3);
        // 3개의 채널은 1,2,3 번 채널임.
        for (ChannelProjection projection : result){
            assertThat(currectChannelIdList.contains(projection.id()))
                    .isTrue();
        }
    }

}
