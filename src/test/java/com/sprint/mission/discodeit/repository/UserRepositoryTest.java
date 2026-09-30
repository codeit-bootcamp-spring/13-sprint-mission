package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.config.QueryDslTestConfig;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
//@TestPropertySource(properties = {
//        "spring.jpa.properties.hibernate.generate_statistics=true"
//})
@Import({
        QueryDslTestConfig.class
})
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @MockitoBean
    BinaryContentStorage binaryContentStorage;

    @Autowired
    TestEntityManager em;

    @BeforeEach
    void setup(){
        em.persist(new User("김숙희","ksk@email.com","password",null,null));
        em.persist(new User("이하이","ehe@email.com","password",null,null));
        em.persist(new User("박수","prk@email.com","password",null,null));

        em.flush();
        em.clear();
    }


    @Test
    @DisplayName("test user find by email")
    void testFindByEmail(){
        List<User> res = userRepository.findByEmail("ksk@email.com");

        assertThat(res).hasSize(1);
        assertThat(res.get(0)).extracting(User::getUsername).isEqualTo("김숙희");
    }

    @Test
    @DisplayName("test user find with not contained email")
    void testNotFindByEmail(){
        List<User> res = userRepository.findByEmail("ksp@email.com");

        assertThat(res).hasSize(0);
    }

    @Test
    @DisplayName("test user find by Username")
    void testFindByUsername(){
        List<User> res = userRepository.findByUsername("김숙희");

        assertThat(res).hasSize(1);
        assertThat(res.get(0)).extracting(User::getEmail).isEqualTo("ksk@email.com");
    }

}
