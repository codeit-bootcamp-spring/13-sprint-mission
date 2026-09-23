package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.config.JpaAuditingTestConfig;
import com.sprint.mission.discodeit.config.P6SpySqlFormatter;
import com.sprint.mission.discodeit.config.QuerydslTestConfig;
import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import com.sprint.mission.discodeit.dto.command.user.UserRoleUpdateCommand;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Import(value = {QuerydslTestConfig.class, JpaAuditingTestConfig.class, P6SpySqlFormatter.class})
@DisplayName("UserRepository 슬라이스 테스트")
@Slf4j
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    BinaryContentRepository binaryContentRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("사용자명 존재 여부 조회 성공 - 존재하는 사용자명이면 true 반환")
    void existsByUsername_returnsTrue_whenUsernameExists() {
        // given
        // @DataJpaTest는 Repository와 JPA 관련 컴포넌트만 로드하는 슬라이스 테스트다.
        // 따라서 UserRepository는 실제 Spring Data JPA Repository로 동작하고,
        // 아래 saveAndFlush(...)는 테스트 DB에 실제 insert SQL을 실행한다.
        UserCreateCommand command = userCreateCommand();

        // existsByUsername(...)가 영속성 컨텍스트의 객체가 아니라 DB에 저장된 row를 기준으로 동작하는지 보려면
        // save(...)만 두는 것보다 flush까지 명시하는 편이 테스트 의도가 분명하다.
        User savedUser = saveUser(command);

        // 조회 조건으로 사용할 username은 저장한 command에서 꺼내 사용한다.
        // 문자열을 다시 직접 쓰면 저장 데이터와 조회 조건이 우연히 달라져도 알아차리기 어렵다.
        String username = command.username();

        // when
        // 실제 Repository derived query 메서드를 호출한다.
        boolean exists = userRepository.existsByUsername(username);

        // then
        // 저장된 사용자 id가 생성됐고, 같은 username으로 존재 여부를 조회하면 true가 반환되어야 한다.
        assertThat(savedUser.getId()).isNotNull();
        assertThat(exists).isTrue();


    }


    @Test
    @DisplayName("사용자명 존재 여부 조회 성공 - 존재하지 않는 사용자명이면 false 반환")
    void existsByUsername_returnsFalse_whenUsernameDoesNotExist() {
        // given
        // 단순히 빈 테이블에서 false를 확인하면 "데이터가 없어서 false"인지만 검증된다.
        // 그래서 다른 username을 가진 사용자를 하나 저장해 두고,
        // 조회 조건과 일치하는 username만 없다는 상황을 만든다.
        UserCreateCommand command = userCreateCommand();
        saveUser(command);

        String username = "missingUsername";

        // when
        // 저장된 사용자와 다른 username으로 존재 여부를 조회한다.
        boolean exists = userRepository.existsByUsername(username);

        // then
        // users 테이블에 row가 있더라도 username 조건과 일치하지 않으면 false가 반환되어야 한다.
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("이메일 존재 여부 조회 성공 - 존재하는 이메일이면 true 반환")
    void existsByEmail_returnsTrue_whenEmailExists() {
        // given
        // 이 테스트는 Spring Data JPA가 메서드 이름으로 생성한 existsByEmail(...) 쿼리가
        // users 테이블의 email 컬럼을 기준으로 존재 여부를 올바르게 판단하는지 검증한다.
        // Repository 테스트이므로 UserRepository와 테스트 DB를 실제로 사용하고, User 엔티티도 실제 객체로 만든다.
        UserCreateCommand command = userCreateCommand();

        // saveAndFlush(...)로 insert SQL을 즉시 DB에 반영한다.
        // save(...)만 호출하면 영속성 컨텍스트에만 머무는 상태처럼 보일 수 있으므로,
        // Repository의 실제 DB 조회 결과를 확인한다는 의도를 flush로 명확히 한다.
        User savedUser = saveUser(command);

        // when
        // 저장한 사용자와 동일한 email을 조회 조건으로 사용한다.
        // 테스트 데이터와 조회 조건을 같은 command에서 꺼내면 문자열 오타로 인한 테스트 오류를 줄일 수 있다.
        String email = command.email();
        boolean exists = userRepository.existsByEmail(email);

        // then
        // 사용자 저장이 정상적으로 완료되어 id가 생성됐고,
        // 같은 email을 가진 row가 있으므로 existsByEmail(...)은 true를 반환해야 한다.
        assertThat(savedUser.getId()).isNotNull();
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이메일 존재 여부 조회 성공 - 존재하지 않는 이메일이면 false 반환")
    void existsByEmail_returnsFalse_whenEmailDoesNotExist() {
        // given
        // 빈 테이블에서 false가 나오는지만 보면 email 조건이 실제로 적용됐는지 확인하기 어렵다.
        // 그래서 다른 email을 가진 사용자를 먼저 저장해 두고,
        // 조회하려는 email만 존재하지 않는 상황을 만든다.
        UserCreateCommand command = userCreateCommand();
        User savedUser = saveUser(command);

        // 저장된 email과 명확히 다른 값을 조회 조건으로 사용한다.
        // 랜덤 prefix를 붙이는 방식보다 고정된 값이 실패 원인을 재현하고 읽기 쉽다.
        String missingEmail = "missingEmail@gmail.com";

        // when
        // users 테이블에는 row가 있지만, 이 email과 일치하는 row는 없다.
        boolean exists = userRepository.existsByEmail(missingEmail);

        // then
        // 저장 자체가 정상적으로 이루어진 상태에서,
        // email 조건과 일치하는 데이터가 없으면 existsByEmail(...)은 false를 반환해야 한다.
        assertThat(savedUser.getId()).isNotNull();
        assertThat(missingEmail).isNotEqualTo(command.email());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("사용자 저장 성공 - 회원가입 사용자는 USER 역할을 가진다")
    void save_persistsUserRole_whenUserIsCreated() {
        User savedUser = saveUser(userCreateCommand());

        em.clear();

        User foundUser = userRepository.findById(savedUser.getId())
                .orElseThrow(AssertionError::new);
        assertThat(foundUser.getRole()).isEqualTo(Role.USER);
        assertThat(userRepository.existsByRole(Role.USER)).isTrue();
    }

    @Test
    @DisplayName("역할 존재 여부 조회 성공 - 일치하는 역할이 없으면 false를 반환한다")
    void existsByRole_returnsFalse_whenRoleDoesNotExist() {
        User user = new User(userCreateCommand(), null);
        user.updateRole(new UserRoleUpdateCommand(Role.ADMIN));
        userRepository.saveAndFlush(user);

        em.clear();

        assertThat(userRepository.existsByRole(Role.ADMIN)).isTrue();
        assertThat(userRepository.existsByRole(Role.CHANNEL_MANAGER)).isFalse();
    }


    @Test
    @DisplayName("사용자 목록 조회 성공 - 프로필을 함께 조회")
    void findAll_fetchesProfile_whenUsersExist() {
        // given
        // 따라서 이 테스트는 단순히 User 목록이 반환되는지만 보는 것이 아니라,
        UserCreateCommand command = userCreateCommand();

        // profile을 null로 두면 "프로필을 함께 조회한다"는 요구사항을 검증할 수 없다.
        // 그래서 실제 BinaryContent를 먼저 저장하고 User의 profile 연관관계에 연결한다.
        UserFixture fixture = saveUserWithProfile(command);

        UUID savedProfileId = fixture.profile().getId();
        UUID savedUserId = fixture.user().getId();

        // 영속성 컨텍스트를 비워야 findAll()이 1차 캐시에 남아 있는 엔티티를 그대로 반환하지 않는다.
        // 이 clear() 덕분에 아래 조회는 실제 DB에서 다시 읽어 오는 흐름이 된다.
        em.clear();

        // when
        // 실제 UserRepository.findAll()을 호출한다.
        List<User> users = userRepository.findAll();

        // then
        // 테스트 데이터로 저장한 사용자가 정확히 1명 조회되어야 한다.
        assertThat(users).hasSize(1);

        User foundUser = users.get(0);
        PersistenceUnitUtil persistenceUnitUtil = getPersistenceUnitUtil();

        // 연관 객체의 getter를 먼저 호출하면 지연 로딩이 발생해서
        // EntityGraph로 함께 조회됐는지, getter 접근 때문에 뒤늦게 조회됐는지 구분하기 어렵다.
        // 그래서 연관 객체에 접근하기 전에 JPA 표준 PersistenceUnitUtil.isLoaded(...)로 로딩 여부를 먼저 확인한다.
        assertThat(persistenceUnitUtil.isLoaded(foundUser, "profile")).isTrue();

        // em.clear() 이후 조회된 foundUser는 DB에서 다시 조회된 엔티티다.
        // 엔티티 equals/hashCode가 id 기반으로 정의되어 있더라도,
        // Repository 테스트에서는 어떤 row와 필드가 조회됐는지 드러나도록 id와 주요 필드를 직접 검증한다.
        assertThat(savedUserId).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(savedUserId);
        assertThat(foundUser.getUsername()).isEqualTo(command.username());
        assertThat(foundUser.getEmail()).isEqualTo(command.email());

        // 이 검증은 단순 null 여부보다 명확하게 "저장한 연관 데이터가 함께 조회됐다"는 사실을 보장한다.

        assertThat(savedProfileId).isNotNull();
        assertThat(foundUser.getProfile()).isNotNull();
        assertThat(foundUser.getProfile().getId()).isEqualTo(savedProfileId);
    }

    @Test
    @DisplayName("사용자 단건 조회 성공 - 프로필을 함께 조회")
    void findById_fetchesProfile_whenUserExists() {
        // given
        // 따라서 이 테스트는 단순히 id로 User 단건을 찾는지만 보는 것이 아니라,
        UserCreateCommand command = userCreateCommand();

        // profile을 null로 두면 "프로필을 함께 조회한다"는 요구사항을 검증할 수 없다.
        // 그래서 실제 BinaryContent를 먼저 저장하고 User의 profile 연관관계에 연결한다.
        UserFixture fixture = saveUserWithProfile(command);

        UUID savedProfileId = fixture.profile().getId();
        UUID savedUserId = fixture.user().getId();

        // 영속성 컨텍스트를 비워야 findById(...)가 1차 캐시에 남아 있는 savedUser를 그대로 반환하지 않는다.
        // 이 clear() 덕분에 아래 조회는 실제 DB에서 다시 읽어 오는 흐름이 된다.
        em.clear();

        // when
        // 저장된 사용자의 id로 실제 Repository 단건 조회 메서드를 호출한다.
        User foundUser = userRepository.findById(savedUserId).orElseThrow(AssertionError::new);

        // then
        // 연관 객체의 getter를 먼저 호출하면 지연 로딩이 발생해서
        // EntityGraph로 함께 조회됐는지, getter 접근 때문에 뒤늦게 조회됐는지 구분하기 어렵다.
        // 그래서 연관 객체에 접근하기 전에 JPA 표준 PersistenceUnitUtil.isLoaded(...)로 로딩 여부를 먼저 확인한다.
        PersistenceUnitUtil persistenceUnitUtil = getPersistenceUnitUtil();
        assertThat(persistenceUnitUtil.isLoaded(foundUser, "profile")).isTrue();

        // em.clear() 이후 조회된 foundUser는 DB에서 다시 조회된 엔티티다.
        // 엔티티 equals/hashCode가 id 기반으로 정의되어 있더라도,
        // Repository 테스트에서는 어떤 row와 필드가 조회됐는지 드러나도록 id와 주요 필드를 직접 검증한다.
        assertThat(savedUserId).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(savedUserId);
        assertThat(foundUser.getUsername()).isEqualTo(command.username());
        assertThat(foundUser.getEmail()).isEqualTo(command.email());


        // profile도 마찬가지로 실제 저장한 BinaryContent가 함께 조회됐는지 확인한다.
        assertThat(savedProfileId).isNotNull();
        assertThat(foundUser.getProfile()).isNotNull();
        assertThat(foundUser.getProfile().getId()).isEqualTo(savedProfileId);
    }

    @Test
    @DisplayName("사용자 단건 조회 성공 - 존재하지 않는 사용자이면 Optional.empty 반환")
    void findById_returnsEmpty_whenUserDoesNotExist() {
        // given
        // findById(...)가 "존재하지 않는 id"에 대해 Optional.empty를 반환하는지 검증한다.
        // 테스트 DB가 완전히 비어 있어서 우연히 empty가 되는 상황과 구분하기 위해
        // 실제 User row를 하나 저장한 뒤, 그 id와 다른 UUID를 조회 대상으로 사용한다.
        User savedUser = saveUser(userCreateCommand());
        UUID savedUserId = savedUser.getId();
        UUID missingUserId = UUID.randomUUID();

        assertThat(savedUserId).isNotNull();
        assertThat(missingUserId).isNotEqualTo(savedUserId);

        // 저장 직후의 User가 1차 캐시에 남아 있어도 missingUserId 조회 결과에는 직접 영향이 없지만,
        // repository 조회 테스트에서는 실제 DB 조회 경로를 명확히 하기 위해 영속성 컨텍스트를 비운다.
        em.clear();

        // when
        // DB에 존재하지 않는 사용자 id로 단건 조회한다.
        Optional<User> foundUser = userRepository.findById(missingUserId);

        // then
        // users 테이블에 해당 id의 row가 없으므로 Optional.empty가 반환되어야 한다.
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("사용자명 조회 성공 - 프로필을 함께 조회")
    void findByUsername_fetchesProfile_whenUsernameMatches() {
        // given
        // UserRepository.findByUsername(...)에는
        // 따라서 이 테스트는 사용자명이 일치하는 User를 찾는지뿐 아니라,
        UserCreateCommand command = userCreateCommand();

        // profile을 null로 두면 "프로필을 함께 조회한다"는 요구사항을 검증할 수 없다.
        // 그래서 실제 BinaryContent를 먼저 저장하고 User의 profile 연관관계에 연결한다.
        UserFixture fixture = saveUserWithProfile(command);

        UUID savedUserId = fixture.user().getId();
        UUID savedProfileId = fixture.profile().getId();

        assertThat(savedUserId).isNotNull();
        assertThat(savedProfileId).isNotNull();

        // 영속성 컨텍스트를 비워야 findByUsername(...)가
        // 1차 캐시에 남아 있는 savedUser를 그대로 반환하지 않는다.
        // 이 clear() 덕분에 아래 조회는 실제 DB에서 다시 읽어 오는 흐름이 된다.
        em.clear();

        // when
        // 저장한 사용자와 같은 username으로 실제 Repository 조회 메서드를 호출한다.
        User foundUser = userRepository.findByUsername(command.username())
                .orElseThrow(AssertionError::new);

        // then
        // 연관 객체의 getter를 먼저 호출하면 지연 로딩이 발생해서
        // EntityGraph로 함께 조회됐는지, getter 접근 때문에 뒤늦게 조회됐는지 구분하기 어렵다.
        // 그래서 연관 객체에 접근하기 전에 JPA 표준 PersistenceUnitUtil.isLoaded(...)로 로딩 여부를 먼저 확인한다.
        PersistenceUnitUtil persistenceUnitUtil = getPersistenceUnitUtil();
        assertThat(persistenceUnitUtil.isLoaded(foundUser, "profile")).isTrue();

        // 조회 조건으로 사용한 username과 일치하는 사용자가 조회됐는지 확인한다.
        // id뿐 아니라 주요 필드도 함께 확인하면 잘못된 row가 반환되는 문제를 더 명확히 잡을 수 있다.
        assertThat(foundUser.getId()).isEqualTo(savedUserId);
        assertThat(foundUser.getUsername()).isEqualTo(command.username());
        assertThat(foundUser.getPassword()).isEqualTo(command.password());
        assertThat(foundUser.getEmail()).isEqualTo(command.email());

        assertThat(foundUser.getProfile()).isNotNull();
        assertThat(foundUser.getProfile().getId()).isEqualTo(savedProfileId);
    }

    @Test
    @DisplayName("사용자명 조회 성공 - 사용자명이 일치하지 않으면 Optional.empty 반환")
    void findByUsername_returnsEmpty_whenUsernameDoesNotMatch() {
        // given
        // findByUsername(...)은 username이 일치할 때만 User를 반환해야 한다.
        // 이 테스트는 users 테이블에 실제 사용자가 존재하더라도
        // 다른 username으로 조회하면 Optional.empty가 반환되는지 검증한다.
        UserCreateCommand command = userCreateCommand();

        // 따라서 이 테스트에서는 연관관계 로딩이 아니라 username 조건 적용이 관심사이므로
        User savedUser = saveUser(command);
        UUID savedUserId = savedUser.getId();

        assertThat(savedUserId).isNotNull();

        // 1차 캐시에 저장 직후의 User가 남아 있으면 조회 결과가 DB 상태와 분리되어 보일 수 있다.
        // 영속성 컨텍스트를 비워 실제 DB 조회 기준으로 검증한다.
        em.clear();

        // 저장된 사용자와 명확히 다른 값을 조회 조건으로 사용한다.
        // 고정 prefix를 붙여 어떤 조건이 불일치하는지 테스트 코드에서 바로 드러나게 한다.
        String wrongUsername = "wrong-" + command.username();
        assertThat(wrongUsername).isNotEqualTo(command.username());

        // when
        Optional<User> userWithWrongUsername = userRepository.findByUsername(wrongUsername);

        // then
        // username이 일치하지 않으면 Optional.empty가 반환되어야 한다.
        assertThat(userWithWrongUsername).isEmpty();
    }

    private UserCreateCommand userCreateCommand() {
        return new UserCreateCommand(
                "testUsername",
                "testPassword",
                "testEmail@gmail.com"
        );
    }

    private User saveUser(UserCreateCommand command) {
        return userRepository.saveAndFlush(new User(command, null));
    }

    private BinaryContent saveProfile() {
        return binaryContentRepository.saveAndFlush(new BinaryContent("profile.png", "image/png", 1_024L));
    }

    private UserFixture saveUserWithProfile(UserCreateCommand command) {
        BinaryContent savedProfile = saveProfile();
        User savedUser = userRepository.saveAndFlush(new User(command, savedProfile));

        return new UserFixture(savedUser, savedProfile);
    }

    private PersistenceUnitUtil getPersistenceUnitUtil() {
        return em.getEntityManagerFactory().getPersistenceUnitUtil();
    }

    private record UserFixture(User user, BinaryContent profile) {
    }
}
