package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.config.QueryDslTestConfig;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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

}
