package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    TestEntityManager em;
    @Autowired
    EntityManagerFactory emf;

    private void setup(){
        em.persist(new User("김숙희","ksk@email.com","password",setTestBinaryContent(),null));

        em.flush();
        em.clear();
    }

    private BinaryContent setTestBinaryContent(){
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test".getBytes());
        byte[] bytes = null;
        try { bytes = file.getBytes();} catch (IOException e) { e.printStackTrace(); }
        BinaryContent bc = new BinaryContent(file.getOriginalFilename(), file.getContentType(), file.getSize(), bytes);
        em.persist(bc);
        return bc;
    }


    @Test
    @DisplayName("test user find by email")
    void testFindByEmail(){
        setup();
        List<User> res = userRepository.findByEmail("ksk@email.com");

        assertThat(res).hasSize(1);
        assertThat(res.get(0)).extracting(User::getUsername).isEqualTo("김숙희");
    }

    @Test
    @DisplayName("test user find with not contained email")
    void testNotFindByEmail(){
        setup();
        List<User> res = userRepository.findByEmail("ksp@email.com");

        assertThat(res).hasSize(0);
    }

    @Test
    @DisplayName("test user find by Username")
    void testFindByUsername(){
        setup();
        List<User> res = userRepository.findByUsername("김숙희");

        assertThat(res).hasSize(1);
        assertThat(res.get(0)).extracting(User::getEmail).isEqualTo("ksk@email.com");
    }

    @Test
    @DisplayName("Profile find test")
    void testProfileFind() {
        // given
        setup();
        List<User> res = userRepository.findAllWithProfile();
        // when
        // then
        assertThat(res).hasSize(1);
        // check the original file name is same with mock entity.
        assertThat(res.get(0))
                .extracting(User::getProfile)
                .extracting(BinaryContent::getFileName)
                .isEqualTo("test.txt");
    }





}
