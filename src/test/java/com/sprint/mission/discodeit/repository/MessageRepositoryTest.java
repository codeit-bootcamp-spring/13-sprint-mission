package com.sprint.mission.discodeit.repository;


import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DataJpaTest(showSql = false)
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
@ActiveProfiles("test")
@Slf4j
public class MessageRepositoryTest {

    @Autowired
    ChannelRepository channelRepository;
    @Autowired
    TestEntityManager em;
    @Autowired
    EntityManagerFactory emf;
    @Autowired
    private MessageRepository messageRepository;

    private List<UUID> setup() {
        Channel channel = getTestChannel("2026-08-02T09:00:00Z","1",ChannelType.PUBLIC);
        List<UUID> res = new ArrayList<>();
        res.add(channel.getId());

        User user = getTestUser();

        Message message = new Message("message1",channel,user,null);
        Message message2 = new Message("message2",channel,user,null);
        Message message3 = new Message("message3",channel,user,null);
        Message message4 = new Message("message4",channel,user,null);

        // set create time on ASC of msg
        ReflectionTestUtils.setField(message4,"createdAt",Instant.parse("2026-08-02T09:00:00Z"));
        ReflectionTestUtils.setField(message3,"createdAt",Instant.parse("2026-08-01T09:00:00Z"));
        ReflectionTestUtils.setField(message2,"createdAt",Instant.parse("2026-07-02T09:00:00Z"));
        ReflectionTestUtils.setField(message,"createdAt",Instant.parse("2025-08-02T09:00:00Z"));

        em.persist(message);
        em.persist(message2);
        em.persist(message3);
        em.persist(message4);
        log.info("test message entity set");
        em.flush();
        em.clear();
        return res;
    }

    /* 채널 id 를 통해 메세지를 조회해야 함으로
     * mock 객체가 아닌 실제 객체에 id 를 주입해서 사용한다.
     *
     * createdAt 을 기준으로 정렬함으로 생성 시간도 조정한다.
     */
    private Channel getTestChannel(String ctime, String name, ChannelType type) {
        Channel channel = new Channel(name,"description",type);
        ReflectionTestUtils.setField(channel,"createdAt",Instant.parse(ctime));

        em.persist(channel);
        return channel;
    }

    private User getTestUser(){
        User user = new User("김숙희","ksk@email.com","password",null,null);
        ReflectionTestUtils.setField(user,"createdAt",Instant.parse("2026-08-02T09:00:00Z"));

        em.persist(user);
        return user;
    }

    @Test
    @DisplayName("test find by channel id")
    void testFindByChannelId() {
        // given
        List<UUID> ids = setup();
        log.info("id {}",ids.get(0));
        // when
        // then

        // will return 4 message with orderd by ctime -> 4,3,2,1
        List<Message> messages = messageRepository.findByChannelId(ids.get(0));

        assertThat(messages).hasSize(4);
        assertThat(messages.get(0)).extracting(Message::getContent).isEqualTo("message4");
    }

    @Test
    @DisplayName("test find by channel id orderd")
    void testFindByChannelIdWithCtime() {
        // given
        List<UUID> ids = setup();
        log.info("id {}",ids.get(0));
        Pageable page = PageRequest.of(0, 2);
        // when
        // then
        Slice<Message> messages = messageRepository.findByChannelIdOrderByCreatedAtDesc(ids.get(0),page);

        // test page size
        assertThat(messages.getContent()).hasSize(2);
        // test if msg order by DSC on ctime, message4 is first.
        assertThat(messages.getContent().get(0)).extracting(Message::getContent).isEqualTo("message4");
    }

    @Test
    @DisplayName("test find by channel id with pagenation")
    void testFindByChannelForMessageDto() {
        // query message with pageable and entity graph.
        // given
        List<UUID> ids = setup();
        log.info("id {}",ids.get(0));
        Pageable page = PageRequest.of(0, 2);
        // when
        // then
        Slice<Message> messages = messageRepository.findByChannelIdForMessageDto(ids.get(0),page);

        // test page size = 2
        assertThat(messages.getContent()).hasSize(2);
        // test no order by DSC on ctime, message1 is first.
        assertThat(messages.getContent().get(0)).extracting(Message::getContent).isEqualTo("message1");
    }

    @Test
    @DisplayName("test find by channel id with cursor")
    void testFindByChannelWithCursor() {
        // query message with pageable and entity graph.
        // given
        List<UUID> ids = setup();
        log.info("id {}",ids.get(0));
        Pageable page = PageRequest.of(0, 4);

        // make cursor for midline -> message1 will return
        Instant cursor = Instant.parse("2026-06-10T09:00:00Z");
        // when
        // then
        Slice<Message> messages = messageRepository.findByChannelWithCursor(ids.get(0),page,cursor);

        // test page size = 1
        assertThat(messages.getContent()).hasSize(1);
        // test no order by DSC on ctime, message1 is first.s
        assertThat(messages.getContent().get(0)).extracting(Message::getContent).isEqualTo("message1");
    }

    @Test
    @DisplayName("test find by channel id is fail")
    void testFindFail() {
        // given
        Pageable page = PageRequest.of(0, 2);
        // when
        // then
        // if the worng channel id is whrown it will return empty list
        assertThat(messageRepository.findByChannelIdOrderByCreatedAtDesc(UUID.randomUUID(),page))
                .isEmpty();

    }

}
