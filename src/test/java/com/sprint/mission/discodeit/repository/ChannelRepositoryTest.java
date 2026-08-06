package com.sprint.mission.discodeit.repository;


import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
@ActiveProfiles("test")
@Slf4j
public class ChannelRepositoryTest {
    @Autowired
    ChannelRepository channelRepository;
    @Autowired
    TestEntityManager em;
    @Autowired
    EntityManagerFactory emf;

    private void setup(){
        Channel privateChannel = new Channel("","", ChannelType.PRIVATE);
        Channel publicChannel = new Channel("testChannel","publicChannel", ChannelType.PUBLIC);

        em.persist(privateChannel);
        em.persist(publicChannel);

        em.flush();
        em.clear();
    }

    private void channelSetup(ChannelType type, User user){
        // set up channel - readstatus (+ userid)
        String name = "";
        String description = "";
        if (type == ChannelType.PUBLIC) {
            name = UUID.randomUUID().toString();
            description = "public";
        }

        Channel channel = new Channel(name,description,type);
        ReadStatus readStatus = new ReadStatus(user,channel, Instant.now());

        em.persist(channel);
        em.persist(readStatus);

        em.flush();
        em.clear();
    }

    private User setUser(String name, String email){
        User user = new User(name,email,"password",null,null);
        em.persist(user);
        em.flush();
        em.clear();
        return user;
    };

    @Test
    @DisplayName("Visible channel query test - query all visible channel by user id")
    void testVisibleQuery() {
        // given
        User user1 = setUser("김숙희","ksk@email.com");
        User user2 = setUser("박규","qk@email.com");

        channelSetup(ChannelType.PUBLIC,user1);
        channelSetup(ChannelType.PUBLIC,user2);
        channelSetup(ChannelType.PRIVATE,user1);
        channelSetup(ChannelType.PRIVATE,user2);


        // when
        List<Channel> visible = channelRepository.findVisibleChannelByUserId(user1.getId());
        // then
        assertThat(visible.size()).isEqualTo(3);
        for (Channel channel : visible) {
            log.debug("visible channel - id: {}, name: {}, type: {}", channel.getId(),channel.getName(),channel.getType());
        }
    }


    @Test
    @DisplayName("find channel by type ")
    void testFindChannelByType() {
        // given
        setup();
        // when
        // then
        List<Channel> res = channelRepository.findByTypeIs(ChannelType.PRIVATE);

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getType()).isEqualTo(ChannelType.PRIVATE);
    }

}
