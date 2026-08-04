package com.sprint.mission.discodeit.repository;


import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
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
