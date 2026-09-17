package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.config.JpaAuditingTestConfig;
import com.sprint.mission.discodeit.config.P6SpySqlFormatter;
import com.sprint.mission.discodeit.config.QuerydslTestConfig;
import com.sprint.mission.discodeit.dto.command.channel.ChannelCreatePublicCommand;
import com.sprint.mission.discodeit.dto.command.readStatus.ReadStatusCreateCommand;
import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import com.sprint.mission.discodeit.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest(showSql = false)
@Import(value = {QuerydslTestConfig.class, JpaAuditingTestConfig.class, P6SpySqlFormatter.class})
// burkInsert(...) native query는 PostgreSQL의 gen_random_uuid()를 사용한다.
// @DataJpaTest는 embedded H2로 Repository SQL을 검증하므로,
// 테스트 DB에서 같은 함수 이름을 호출할 수 있도록 H2 alias를 등록한다.
// 운영 쿼리를 H2 전용 random_uuid()로 바꾸지 않고도 native insert SQL의 동작을 검증하기 위한 테스트 전용 호환 설정이다.
@Sql(statements = "CREATE ALIAS IF NOT EXISTS gen_random_uuid FOR \"java.util.UUID.randomUUID\"")
@DisplayName("ReadStatusRepository 슬라이스 테스트")
class ReadStatusRepositoryTest {

    @Autowired
    ReadStatusRepository readStatusRepository;

    @Autowired
    UserRepository userRepository;


    @Autowired
    ChannelRepository channelRepository;

    @Autowired
    BinaryContentRepository binaryContentRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("사용자별 읽음 상태 목록 조회 성공 - User와 Channel을 함께 조회")
    void findByUserId_fetchesUserAndChannel_whenUserHasReadStatuses() {
        // given
        // 이 테스트의 대상은 ReadStatusRepository.findByUserId(...) 쿼리다.
        // H2 테스트 DB에 저장한 뒤 Repository가 실제 SQL로 어떤 row와 연관 객체를 조회하는지 검증한다.
        UserCreateCommand userCreateCommand = userCreateCommand();
        ChannelCreatePublicCommand channelCreatePublicCommand = channelCreatePublicCommand();
        Instant readAt = Instant.now();

        // ReadStatus.user와 ReadStatus.channel은 nullable = false인 LAZY 연관관계다.
        // 따라서 읽음 상태만 단독으로 만들지 않고 먼저 실제 User와 Channel을 저장한다.
        User savedUser = saveUser(userCreateCommand);
        Channel savedChannel = saveChannel(channelCreatePublicCommand);


        ReadStatus savedReadStatus = saveReadStatus(savedChannel, savedUser, readAt);

        UUID savedUserId = savedUser.getId();
        UUID savedChannelId = savedChannel.getId();
        UUID savedReadStatusId = savedReadStatus.getId();

        assertThat(savedUserId).isNotNull();
        assertThat(savedChannelId).isNotNull();
        assertThat(savedReadStatusId).isNotNull();

        // 저장 직후의 엔티티들은 영속성 컨텍스트 1차 캐시에 남아 있다.
        // em.clear()를 하지 않으면 아래 조회 결과가 DB에서 다시 읽힌 객체인지,
        // 저장할 때 이미 관리 중이던 객체인지 구분하기 어렵다.
        // EntityGraph와 LAZY 로딩 검증을 하려면 반드시 1차 캐시를 비운 뒤 조회해야 한다.
        em.clear();

        // when
        // 사용자 id로 ReadStatus 목록을 조회한다.
        // 현재 Repository 메서드는 @Query로 rs.user.id 조건을 명시하고,
        List<ReadStatus> readStatuses = readStatusRepository.findByUserId(savedUserId);

        // then
        // 먼저 조회 결과 개수와 식별자를 확인해, 조회 조건에 맞는 ReadStatus row만 반환됐는지 검증한다.
        assertThat(readStatuses).hasSize(1);
        ReadStatus foundReadStatus = readStatuses.get(0);
        assertThat(foundReadStatus.getId()).isEqualTo(savedReadStatusId);

        // @EntityGraph 검증은 getUser(), getChannel(), getUserId(), getChannelId() 같은 getter 접근 전에 해야 한다.
        // getter를 먼저 호출하면 그 시점에 LAZY 연관관계가 초기화될 수 있어서,
        // EntityGraph 때문에 미리 로딩된 것인지 구분할 수 없어진다.
        PersistenceUnitUtil persistenceUnitUtil = getPersistenceUnitUtil();
        assertThat(persistenceUnitUtil.isLoaded(foundReadStatus, "user")).isTrue();
        assertThat(persistenceUnitUtil.isLoaded(foundReadStatus, "channel")).isTrue();

        User foundUser = foundReadStatus.getUser();
        Channel foundChannel = foundReadStatus.getChannel();

        // 실제 SQL 개수를 세는 테스트는 아니지만, getter 접근 전에 이미 로딩되어 있음을 확인하므로

        // ReadStatus 자체 필드와 외래키 accessor가 저장한 row를 기준으로 일치하는지 확인한다.
        assertThat(foundReadStatus.getUserId()).isEqualTo(savedUserId);
        assertThat(foundReadStatus.getChannelId()).isEqualTo(savedChannelId);

        // H2의 timestamp(6) 정밀도 때문에 Instant의 나노초 값은 저장/조회 과정에서 미세하게 달라질 수 있다.
        // 그래서 정확한 나노초 동일성 대신 DB 정밀도 수준의 오차만 허용한다.
        assertThat(Duration.between(readAt, foundReadStatus.getLastReadAt()).abs())
                .isLessThanOrEqualTo(Duration.ofNanos(1_000));

        // given에서 저장한 실제 row와 연결되어 있는지 주요 필드와 id로 검증한다.
        assertThat(foundUser.getId()).isEqualTo(savedUserId);
        assertThat(foundUser.getUsername()).isEqualTo(userCreateCommand.username());
        assertThat(foundUser.getEmail()).isEqualTo(userCreateCommand.email());

        assertThat(foundChannel.getId()).isEqualTo(savedChannelId);
        assertThat(foundChannel.getName()).isEqualTo(channelCreatePublicCommand.channelName());
        assertThat(foundChannel.getDescription()).isEqualTo(channelCreatePublicCommand.channelDescription());
        assertThat(foundChannel.getType()).isEqualTo(ChannelType.PUBLIC);
    }

    @Test
    @DisplayName("사용자별 읽음 상태 목록 조회 성공 - 읽음 상태가 없으면 빈 목록 반환")
    void findByUserId_returnsEmptyList_whenUserHasNoReadStatuses() {
        // given
        // 이 테스트의 대상은 ReadStatusRepository.findByUserId(...)가
        // 전달받은 userId에 연결된 ReadStatus만 반환하는지 검증하는 것이다.
        // 단순히 빈 DB에서 조회하면 "테이블이 비어서 빈 목록"인지,
        // "userId 조건에 맞는 row가 없어서 빈 목록"인지 구분할 수 없다.
        // 그래서 조회 대상 사용자는 ReadStatus가 없게 두고,
        // 다른 사용자의 ReadStatus row는 실제로 저장해 read_statuses 테이블이 비어 있지 않은 상황을 만든다.
        UserCreateCommand userCreateCommand = userCreateCommand();
        UserCreateCommand otherUserCreateCommand = userCreateCommand(
                "otherUser",
                "otherPassword",
                "other@gmail.com"
        );
        ChannelCreatePublicCommand channelCreatePublicCommand = channelCreatePublicCommand();
        Instant otherUserReadAt = Instant.now();

        // 조회 대상 사용자다.
        User savedUserWithoutReadStatus = saveUser(userCreateCommand);

        // 대조군 사용자다.
        // 같은 채널에 대해 이 사용자에게만 ReadStatus를 만들어 두면,
        // Repository가 userId 조건을 무시하거나 잘못 조인하는 경우 테스트가 실패한다.
        User savedOtherUser = saveUser(otherUserCreateCommand);
        Channel savedChannel = saveChannel(channelCreatePublicCommand);
        ReadStatus savedOtherReadStatus = saveReadStatus(savedChannel, savedOtherUser, otherUserReadAt);

        UUID savedUserId = savedUserWithoutReadStatus.getId();
        UUID savedOtherUserId = savedOtherUser.getId();
        UUID savedChannelId = savedChannel.getId();
        UUID savedOtherReadStatusId = savedOtherReadStatus.getId();

        // given 절의 저장이 정상적으로 끝났는지 먼저 확인한다.
        // 특히 savedOtherReadStatusId가 null이 아니어야 read_statuses 테이블에 대조군 row가 있는 상태에서
        // 빈 목록을 검증한다는 테스트 의도가 성립한다.
        assertThat(savedUserId).isNotNull();
        assertThat(savedOtherUserId).isNotNull();
        assertThat(savedChannelId).isNotNull();
        assertThat(savedOtherReadStatusId).isNotNull();
        assertThat(savedOtherUserId).isNotEqualTo(savedUserId);
        assertThat(savedOtherReadStatus.getUserId()).isEqualTo(savedOtherUserId);
        assertThat(savedOtherReadStatus.getChannelId()).isEqualTo(savedChannelId);

        // 저장 직후의 엔티티가 1차 캐시에 남아 있으면 실제 SELECT 결과를 검증한다는 의미가 약해진다.
        // 영속성 컨텍스트를 비운 뒤 조회해, 아래 when 절이 DB에 저장된 row를 기준으로 동작하게 한다.
        em.clear();

        // when
        // ReadStatus가 없는 조회 대상 사용자 id로 목록을 조회한다.
        // read_statuses 테이블에는 다른 사용자의 row가 있지만,
        // 이 userId와 연결된 row는 없으므로 빈 목록이 반환되어야 한다.
        List<ReadStatus> readStatuses = readStatusRepository.findByUserId(savedUserId);

        // then
        // 결과가 비어 있어야 한다.
        // 이 검증은 "ReadStatus가 전혀 없는 경우"가 아니라
        // "다른 사용자의 ReadStatus는 있어도 조회 대상 사용자의 ReadStatus는 없는 경우"를 확인한다.
        assertThat(readStatuses).isEmpty();
    }

    @Test
    @DisplayName("채널별 읽음 상태 삭제 성공 - 채널에 연결된 읽음 상태 삭제")
    void deleteByChannel_Id_deletesReadStatuses_whenChannelHasReadStatuses() {
        // given
        // 이 테스트의 대상은 ReadStatusRepository.deleteByChannel_Id(...)다.
        // 단순히 ReadStatus row 하나를 저장한 뒤 삭제하면 "delete가 실행됐다"는 정도만 확인할 수 있다.
        // 그래서 삭제 대상 채널에는 ReadStatus를 2개 저장하고,
        // 삭제 대상이 아닌 다른 채널에는 ReadStatus를 1개 저장해 둔다.
        // 이렇게 하면 삭제 조건(channel_id)이 정확히 적용되어 대상 채널의 row만 삭제되는지 함께 검증할 수 있다.
        UserCreateCommand userCreateCommand = userCreateCommand();
        UserCreateCommand otherUserCreateCommand = userCreateCommand(
                "otherUser",
                "otherPassword",
                "other@gmail.com"
        );
        ChannelCreatePublicCommand targetChannelCreateCommand = channelCreatePublicCommand();
        ChannelCreatePublicCommand otherChannelCreateCommand = channelCreatePublicCommand(
                "otherChannel",
                "otherChannelDescription",
                ChannelType.PUBLIC
        );
        Instant firstReadAt = Instant.now();
        Instant secondReadAt = firstReadAt.plusSeconds(1);
        Instant remainingReadAt = firstReadAt.plusSeconds(2);

        // 삭제 대상 채널에 서로 다른 두 사용자의 ReadStatus를 연결한다.
        // 실제 서비스에서는 채널 참여자별 읽음 상태가 여러 개 존재할 수 있으므로,
        // 한 건만 삭제되는 우연한 통과를 피하기 위해 복수 row를 준비한다.
        User savedUser = saveUser(userCreateCommand);
        User savedOtherUser = saveUser(otherUserCreateCommand);


        Channel savedTargetChannel = saveChannel(targetChannelCreateCommand);
        Channel savedOtherChannel = saveChannel(otherChannelCreateCommand);

        ReadStatus savedTargetReadStatus = saveReadStatus(savedTargetChannel, savedUser, firstReadAt);
        ReadStatus savedOtherTargetReadStatus = saveReadStatus(savedTargetChannel, savedOtherUser, secondReadAt);
        ReadStatus savedRemainingReadStatus = saveReadStatus(savedOtherChannel, savedUser, remainingReadAt);

        UUID savedUserId = savedUser.getId();
        UUID savedOtherUserId = savedOtherUser.getId();
        UUID savedTargetChannelId = savedTargetChannel.getId();
        UUID savedOtherChannelId = savedOtherChannel.getId();
        UUID savedTargetReadStatusId = savedTargetReadStatus.getId();
        UUID savedOtherTargetReadStatusId = savedOtherTargetReadStatus.getId();
        UUID savedRemainingReadStatusId = savedRemainingReadStatus.getId();

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // 여기서 id가 null이면 이후 삭제 검증이 Repository 동작 실패인지, given 구성 실패인지 구분하기 어렵다.
        assertThat(savedUserId).isNotNull();
        assertThat(savedOtherUserId).isNotNull();
        assertThat(savedTargetChannelId).isNotNull();
        assertThat(savedOtherChannelId).isNotNull();
        assertThat(savedTargetReadStatusId).isNotNull();
        assertThat(savedOtherTargetReadStatusId).isNotNull();
        assertThat(savedRemainingReadStatusId).isNotNull();
        assertThat(savedOtherUserId).isNotEqualTo(savedUserId);
        assertThat(savedOtherChannelId).isNotEqualTo(savedTargetChannelId);

        // 저장 직후 영속성 컨텍스트를 비워, 아래 사전 조회가 1차 캐시가 아니라 DB의 실제 row를 읽도록 한다.
        // deleteByChannel_Id(...) 검증 전에 대상/비대상 데이터가 모두 존재하는지 확인해 두면
        // 삭제 후 빈 목록이 "원래 없어서 빈 목록"인 경우와 구분할 수 있다.
        em.clear();
        List<ReadStatus> targetChannelReadStatuses = readStatusRepository.findByChannelId(savedTargetChannelId);
        List<ReadStatus> otherChannelReadStatuses = readStatusRepository.findByChannelId(savedOtherChannelId);

        // 삭제 대상 채널에는 두 사용자의 ReadStatus가 모두 있어야 한다.
        // 순서는 Repository 계약이 아니므로 id, userId, channelId 조합을 순서와 무관하게 검증한다.
        assertThat(targetChannelReadStatuses)
                .hasSize(2)
                .extracting(ReadStatus::getId, ReadStatus::getUserId, ReadStatus::getChannelId)
                .containsExactlyInAnyOrder(
                        tuple(savedTargetReadStatusId, savedUserId, savedTargetChannelId),
                        tuple(savedOtherTargetReadStatusId, savedOtherUserId, savedTargetChannelId)
                );

        // 비대상 채널에도 ReadStatus가 실제로 존재해야 한다.
        // 이 row가 삭제 후에도 남아 있어야 deleteByChannel_Id(...)가 channel_id 조건만 삭제했다는 점을 확인할 수 있다.
        assertThat(otherChannelReadStatuses)
                .hasSize(1)
                .singleElement()
                .extracting(ReadStatus::getId, ReadStatus::getUserId, ReadStatus::getChannelId)
                .containsExactly(savedRemainingReadStatusId, savedUserId, savedOtherChannelId);

        // H2의 timestamp(6) 정밀도 때문에 Instant의 나노초 값은 저장/조회 과정에서 미세하게 달라질 수 있다.
        // 삭제 테스트의 핵심은 아니지만, 사전 조회한 row가 given에서 만든 row인지 명확히 하기 위해 함께 확인한다.
        ReadStatus firstTargetReadStatus = targetChannelReadStatuses.stream()
                .filter(readStatus -> readStatus.getId().equals(savedTargetReadStatusId))
                .findFirst()
                .orElseThrow(AssertionError::new);
        assertThat(Duration.between(firstReadAt, firstTargetReadStatus.getLastReadAt()).abs())
                .isLessThanOrEqualTo(Duration.ofNanos(1_000));

        // when
        // 삭제 대상 채널 id로 ReadStatus를 삭제한다.
        // deleteByChannel_Id(...)는 Spring Data JPA derived delete 메서드다.
        // 따라서 @Modifying bulk delete처럼 아래 SQL 한 번으로 바로 지우는 방식이 아니다.
        //
        //     delete from read_statuses where channel_id = ?
        //
        // 실제 흐름은 다음에 가깝다.
        // 1. channel_id 조건에 해당하는 ReadStatus 엔티티를 먼저 조회한다.
        // 2. 조회된 각 ReadStatus 엔티티를 삭제 대상으로 영속성 컨텍스트에 등록한다.
        // 3. flush 시점에 엔티티 단위 DELETE SQL이 만들어진다.
        //
        // 이 방식은 SQL 템플릿이 row마다 아래처럼 보일 수 있다.
        //
        //     delete from read_statuses where id = ?
        //
        // SQL 로그에 같은 DELETE 템플릿이 2번 보인다고 해서 반드시 DB로 2번 따로 전송됐다는 뜻은 아니다.
        // hibernate.jdbc.batch_size 설정이 적용되면 Hibernate가 같은 DELETE 템플릿을 JDBC batch에 모았다가
        // PreparedStatement.executeBatch()로 실행할 수 있다.
        em.clear();
        readStatusRepository.deleteByChannel_Id(savedTargetChannelId);

        // deleteByChannel_Id(...) 호출만으로는 삭제 SQL이 즉시 DB에 반영됐다고 단정하지 않는다.
        // JPA는 변경 내용을 flush 시점에 DB로 내보내므로, 여기서 flush를 호출해 DELETE 실행 시점을 명확히 한다.
        //
        // JDBC batch 적용 여부는 테스트 결과 assertion만으로 검증하지 않는다.
        // 이 테스트의 책임은 "대상 채널의 ReadStatus만 삭제되고, 다른 채널의 ReadStatus는 남는다"는 Repository 동작 검증이다.
        // batch 적용은 Hibernate/JDBC 실행 방식에 대한 진단 정보이므로 로그로 확인한다.
        //
        // org.hibernate.orm.jdbc.batch: trace 설정에서 아래 로그가 나오면,
        // 같은 DELETE 템플릿 2건이 batch에 쌓인 뒤 executeBatch()로 실행됐다고 볼 수 있다.
        //
        //     Adding to JDBC batch (1) - `com.sprint.mission.discodeit.entity.ReadStatus#DELETE`
        //     Adding to JDBC batch (2) - `com.sprint.mission.discodeit.entity.ReadStatus#DELETE`
        //     Executing JDBC batch (2 / 50) - `com.sprint.mission.discodeit.entity.ReadStatus#DELETE`
        //
        // 특히 "Executing JDBC batch (2 / 50)"가 실제 batch 실행 여부를 판단하는 핵심 문구다.
        // 앞의 숫자 2는 이번 flush에서 batch로 실행된 DELETE 작업 수이고,
        // 뒤의 50은 hibernate.jdbc.batch_size 설정값이다.
        //
        // flush 이후 clear를 호출해 삭제된 엔티티나 남은 엔티티가 1차 캐시에 의해 검증 결과에 영향을 주지 않게 한다.
        em.flush();
        em.clear();

        // then
        // 대상 채널에 연결되어 있던 ReadStatus는 모두 삭제되어야 한다.
        List<ReadStatus> statusesForTargetChannel = readStatusRepository.findByChannelId(savedTargetChannelId);
        assertThat(statusesForTargetChannel).isEmpty();

        // 반면 다른 채널의 ReadStatus는 삭제 조건에 해당하지 않으므로 그대로 남아 있어야 한다.
        // 이 검증이 없으면 deleteByChannel_Id(...)가 실수로 더 넓은 범위의 row를 삭제해도 놓칠 수 있다.
        List<ReadStatus> statusesForOtherChannel = readStatusRepository.findByChannelId(savedOtherChannelId);
        assertThat(statusesForOtherChannel)
                .hasSize(1)
                .singleElement()
                .extracting(ReadStatus::getId, ReadStatus::getUserId, ReadStatus::getChannelId)
                .containsExactly(savedRemainingReadStatusId, savedUserId, savedOtherChannelId);

        // 전체 ReadStatus 개수도 보조적으로 확인한다.
        // given에서 3개를 만들고 대상 채널의 2개만 삭제했으므로, 최종적으로 1개만 남아야 한다.
        assertThat(readStatusRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("채널과 사용자 기준 읽음 상태 존재 여부 조회 성공 - 읽음 상태가 있으면 true 반환")
    void existsByChannel_IdAndUser_Id_returnsTrue_whenReadStatusExists() {
        // given
        // 이 테스트의 대상은 ReadStatusRepository.existsByChannel_IdAndUser_Id(...)다.
        // 이 메서드는 channel_id와 user_id가 같은 ReadStatus row 안에서 동시에 일치하는지 확인해야 한다.
        // 따라서 단순히 ReadStatus를 1건만 저장하면 "둘 중 하나의 조건만 맞아도 true가 되는지"까지는 구분하기 어렵다.
        // 아래 fixture는 조회 대상 조합과 대조군 조합을 함께 만들어,
        // Repository가 channelId와 userId를 AND 조건으로 정확히 적용하는지 드러내기 위한 구성이다.
        UserCreateCommand userCreateCommand = userCreateCommand();
        UserCreateCommand otherUserCreateCommand = userCreateCommand(
                "otherUser",
                "otherPassword",
                "other@gmail.com"
        );
        ChannelCreatePublicCommand targetChannelCreateCommand = channelCreatePublicCommand();
        ChannelCreatePublicCommand otherChannelCreateCommand = channelCreatePublicCommand(
                "otherChannel",
                "otherChannelDescription",
                ChannelType.PUBLIC
        );
        Instant targetReadAt = Instant.now();
        Instant otherUserReadAt = targetReadAt.plusSeconds(1);
        Instant otherChannelReadAt = targetReadAt.plusSeconds(2);

        // 조회 대상 사용자와 대조군 사용자를 각각 저장한다.
        User savedUser = saveUser(userCreateCommand);
        User savedOtherUser = saveUser(otherUserCreateCommand);

        // 조회 대상 채널과 대조군 채널을 각각 저장한다.
        // 같은 사용자라도 다른 채널의 ReadStatus는 조회 대상 조합이 아니어야 하고,
        // 같은 채널이라도 다른 사용자의 ReadStatus 역시 조회 대상 조합이 아니어야 한다.
        Channel savedTargetChannel = saveChannel(targetChannelCreateCommand);
        Channel savedOtherChannel = saveChannel(otherChannelCreateCommand);

        // 이 row가 existsByChannel_IdAndUser_Id(savedTargetChannelId, savedUserId)를 true로 만드는 실제 대상 row다.
        ReadStatus savedTargetReadStatus = saveReadStatus(savedTargetChannel, savedUser, targetReadAt);

        // 대조군 1: 채널은 같지만 사용자가 다르다.
        // Repository가 userId 조건을 무시하면 이 row 때문에 잘못된 true가 나올 수 있다.
        ReadStatus savedOtherUserReadStatus = saveReadStatus(savedTargetChannel, savedOtherUser, otherUserReadAt);

        // 대조군 2: 사용자는 같지만 채널이 다르다.
        // Repository가 channelId 조건을 무시하면 이 row 때문에 잘못된 true가 나올 수 있다.
        ReadStatus savedOtherChannelReadStatus = saveReadStatus(savedOtherChannel, savedUser, otherChannelReadAt);

        UUID savedUserId = savedUser.getId();
        UUID savedOtherUserId = savedOtherUser.getId();
        UUID savedTargetChannelId = savedTargetChannel.getId();
        UUID savedOtherChannelId = savedOtherChannel.getId();
        UUID savedTargetReadStatusId = savedTargetReadStatus.getId();
        UUID savedOtherUserReadStatusId = savedOtherUserReadStatus.getId();
        UUID savedOtherChannelReadStatusId = savedOtherChannelReadStatus.getId();

        // given 절에서 만든 row들이 의도대로 저장됐는지 확인한다.
        // id null 여부와 서로 다른 id 여부를 먼저 검증하면,
        // 이후 exists 결과가 Repository 문제인지 fixture 구성 문제인지 구분하기 쉬워진다.
        assertThat(savedUserId).isNotNull();
        assertThat(savedOtherUserId).isNotNull();
        assertThat(savedTargetChannelId).isNotNull();
        assertThat(savedOtherChannelId).isNotNull();
        assertThat(savedTargetReadStatusId).isNotNull();
        assertThat(savedOtherUserReadStatusId).isNotNull();
        assertThat(savedOtherChannelReadStatusId).isNotNull();
        assertThat(savedOtherUserId).isNotEqualTo(savedUserId);
        assertThat(savedOtherChannelId).isNotEqualTo(savedTargetChannelId);

        // 조회 대상 row는 targetChannel + savedUser 조합이어야 한다.
        assertThat(savedTargetReadStatus.getChannelId()).isEqualTo(savedTargetChannelId);
        assertThat(savedTargetReadStatus.getUserId()).isEqualTo(savedUserId);

        // 대조군 row들은 둘 중 하나의 조건만 일치한다.
        // 이 검증은 existsByChannel_IdAndUser_Id(...)가 두 조건을 모두 만족하는 row를 찾아야 한다는 점을 분명히 한다.
        assertThat(savedOtherUserReadStatus.getChannelId()).isEqualTo(savedTargetChannelId);
        assertThat(savedOtherUserReadStatus.getUserId()).isEqualTo(savedOtherUserId);
        assertThat(savedOtherChannelReadStatus.getChannelId()).isEqualTo(savedOtherChannelId);
        assertThat(savedOtherChannelReadStatus.getUserId()).isEqualTo(savedUserId);

        // 영속성 컨텍스트를 비워, 아래 exists 조회가 저장 직후 관리 객체가 아니라 DB row 기준으로 수행되게 한다.
        em.clear();

        // when
        // channelId와 userId가 모두 일치하는 실제 ReadStatus row가 있는 조합으로 존재 여부를 조회한다.
        boolean exists = readStatusRepository.existsByChannel_IdAndUser_Id(savedTargetChannelId, savedUserId);

        // then
        // targetChannel + savedUser 조합의 ReadStatus가 실제로 존재하므로 true를 반환해야 한다.
        // 같은 채널의 다른 사용자 row, 같은 사용자의 다른 채널 row가 함께 있어도
        // 이 조회는 정확히 두 조건을 모두 만족하는 row 존재 여부를 판단해야 한다.
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("채널과 사용자 기준 읽음 상태 존재 여부 조회 성공 - 읽음 상태가 없으면 false 반환")
    void existsByChannel_IdAndUser_Id_returnsFalse_whenReadStatusDoesNotExist() {
        // given
        // 이 테스트의 대상은 ReadStatusRepository.existsByChannel_IdAndUser_Id(...)의 false 케이스다.
        // 빈 DB에서 조회하면 false가 나오기는 하지만, 그 경우에는 channelId 조건과 userId 조건이
        // 실제로 AND 조건으로 적용되는지 확인하기 어렵다.
        //
        // 그래서 아래 fixture는 다음 상황을 만든다.
        // 1. 조회 대상 channelId는 실제로 존재한다.
        // 2. 조회 대상 userId도 실제로 존재한다.
        // 3. read_statuses 테이블에도 row가 존재한다.
        // 4. 하지만 조회 대상 channelId + 조회 대상 userId가 같은 row에서 만나는 ReadStatus는 없다.
        //
        // 즉, 단순히 데이터가 없어서 false가 아니라
        // "둘 중 하나의 조건만 맞는 row는 있어도 두 조건을 동시에 만족하는 row는 없어서 false"임을 검증한다.
        UserCreateCommand userCreateCommand = userCreateCommand();
        UserCreateCommand otherUserCreateCommand = userCreateCommand(
                "otherUser",
                "otherPassword",
                "other@gmail.com"
        );
        ChannelCreatePublicCommand targetChannelCreateCommand = channelCreatePublicCommand();
        ChannelCreatePublicCommand otherChannelCreateCommand = channelCreatePublicCommand(
                "otherChannel",
                "otherChannelDescription",
                ChannelType.PUBLIC
        );
        Instant targetReadAt = Instant.now();
        Instant otherChannelReadAt = targetReadAt.plusSeconds(1);

        // savedUser는 targetChannel에 대한 ReadStatus를 갖는 사용자다.
        // savedOtherUser는 이번 exists 조회에서 사용할 사용자지만,
        // targetChannel에 대한 ReadStatus는 일부러 만들지 않는다.
        User savedUser = saveUser(userCreateCommand);
        User savedOtherUser = saveUser(otherUserCreateCommand);

        // targetChannel은 조회 대상 채널이고, otherChannel은 대조군 채널이다.
        // savedOtherUser에게는 otherChannel의 ReadStatus만 부여해
        // "사용자는 존재하지만 targetChannel에는 참여하지 않은 상태"를 만든다.
        Channel savedTargetChannel = saveChannel(targetChannelCreateCommand);
        Channel savedOtherChannel = saveChannel(otherChannelCreateCommand);

        // 대조군 1: targetChannel에는 ReadStatus가 있지만 사용자가 savedUser다.
        // Repository가 userId 조건을 무시하면 이 row 때문에 잘못 true가 나올 수 있다.
        ReadStatus savedTargetReadStatus = saveReadStatus(savedTargetChannel, savedUser, targetReadAt);

        // 대조군 2: savedOtherUser에게도 ReadStatus가 있지만 채널이 otherChannel이다.
        // Repository가 channelId 조건을 무시하면 이 row 때문에 잘못 true가 나올 수 있다.
        ReadStatus savedOtherUserReadStatus = saveReadStatus(savedOtherChannel, savedOtherUser, otherChannelReadAt);

        UUID savedUserId = savedUser.getId();
        UUID savedOtherUserId = savedOtherUser.getId();
        UUID savedTargetChannelId = savedTargetChannel.getId();
        UUID savedOtherChannelId = savedOtherChannel.getId();
        UUID savedTargetReadStatusId = savedTargetReadStatus.getId();
        UUID savedOtherUserReadStatusId = savedOtherUserReadStatus.getId();

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // 조회에 사용할 savedTargetChannelId와 savedOtherUserId가 모두 실제 DB에 존재해야
        // false 결과가 "존재하지 않는 id라서 false"가 아니라 "조합이 없어서 false"라는 의미를 가진다.
        assertThat(savedUserId).isNotNull();
        assertThat(savedOtherUserId).isNotNull();
        assertThat(savedTargetChannelId).isNotNull();
        assertThat(savedOtherChannelId).isNotNull();
        assertThat(savedTargetReadStatusId).isNotNull();
        assertThat(savedOtherUserReadStatusId).isNotNull();
        assertThat(savedOtherUserId).isNotEqualTo(savedUserId);
        assertThat(savedOtherChannelId).isNotEqualTo(savedTargetChannelId);

        // targetChannel에는 ReadStatus가 있지만 savedUser와 연결되어 있다.
        // 즉, 조회 대상 사용자(savedOtherUser)와는 연결되어 있지 않다.
        assertThat(savedTargetReadStatus.getChannelId()).isEqualTo(savedTargetChannelId);
        assertThat(savedTargetReadStatus.getUserId()).isEqualTo(savedUserId);

        // savedOtherUser도 ReadStatus를 갖고 있지만 otherChannel과 연결되어 있다.
        // 즉, 조회 대상 채널(targetChannel)과는 연결되어 있지 않다.
        assertThat(savedOtherUserReadStatus.getChannelId()).isEqualTo(savedOtherChannelId);
        assertThat(savedOtherUserReadStatus.getUserId()).isEqualTo(savedOtherUserId);

        // 영속성 컨텍스트를 비워, 아래 exists 조회가 저장 직후 관리 객체가 아니라 DB row 기준으로 수행되게 한다.
        em.clear();

        // when
        // targetChannel은 존재하고 savedOtherUser도 존재하지만,
        // targetChannel + savedOtherUser 조합의 ReadStatus는 저장하지 않았다.
        boolean exists = readStatusRepository.existsByChannel_IdAndUser_Id(savedTargetChannelId, savedOtherUserId);

        // then
        // channelId와 userId가 각각 다른 row에는 존재하더라도,
        // 같은 ReadStatus row에서 두 조건을 동시에 만족하지 않으면 false를 반환해야 한다.
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("읽음 상태 벌크 생성 성공 - 존재하는 사용자 ID 목록으로 ReadStatus 생성")
    void burkInsert_insertsReadStatusesForUsers_whenUserIdsExist() {
        // given
        // 이 테스트의 대상은 ReadStatusRepository.burkInsert(...) native query다.
        // 기존처럼 ReadStatus를 직접 new 해서 save(...)하면 Repository의 벌크 insert SQL을 검증하지 못한다.
        // 따라서 users와 channel만 실제 DB에 저장한 뒤,
        // readStatusRepository.burkInsert(...)를 직접 호출해 read_statuses row가 생성되는지 확인한다.
        UserCreateCommand userCreateCommand = userCreateCommand();
        UserCreateCommand otherUserCreateCommand = userCreateCommand(
                "otherUser",
                "otherPassword",
                "other@gmail.com"
        );
        UserCreateCommand excludedUserCreateCommand = userCreateCommand(
                "excludedUser",
                "excludedPassword",
                "excluded@gmail.com"
        );
        ChannelCreatePublicCommand targetChannelCreateCommand = channelCreatePublicCommand();
        ChannelCreatePublicCommand otherChannelCreateCommand = channelCreatePublicCommand(
                "otherChannel",
                "otherChannelDescription",
                ChannelType.PUBLIC
        );
        Instant readAt = Instant.now();

        // bulk insert 쿼리는 users 테이블에서 userIds에 해당하는 사용자만 선택한다.
        // 그래서 삽입 대상 사용자 2명과, 요청 목록에 넣지 않을 제외 대상 사용자 1명을 함께 저장한다.
        User savedUser = saveUser(userCreateCommand);
        User savedOtherUser = saveUser(otherUserCreateCommand);
        User savedExcludedUser = saveUser(excludedUserCreateCommand);

        // 실제 사용자 생성 흐름에서는 User와 함께 존재하는 데이터이므로 실제 row로 구성한다.

        // targetChannel은 bulk insert 대상 채널이고, otherChannel은 대조군 채널이다.
        // burkInsert(...)에 targetChannelId만 전달했으므로 otherChannel에는 ReadStatus가 생성되면 안 된다.
        Channel savedTargetChannel = saveChannel(targetChannelCreateCommand);
        Channel savedOtherChannel = saveChannel(otherChannelCreateCommand);

        UUID savedUserId = savedUser.getId();
        UUID savedOtherUserId = savedOtherUser.getId();
        UUID savedExcludedUserId = savedExcludedUser.getId();
        UUID savedTargetChannelId = savedTargetChannel.getId();
        UUID savedOtherChannelId = savedOtherChannel.getId();
        List<UUID> userIdsToInsert = List.of(savedUserId, savedOtherUserId);

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // 사용자와 채널 id가 null이면 native query의 where u.id in (:userIds)와 :channelId 조건을 검증할 수 없다.
        assertThat(savedUserId).isNotNull();
        assertThat(savedOtherUserId).isNotNull();
        assertThat(savedExcludedUserId).isNotNull();
        assertThat(savedTargetChannelId).isNotNull();
        assertThat(savedOtherChannelId).isNotNull();
        assertThat(savedOtherUserId).isNotEqualTo(savedUserId);
        assertThat(savedExcludedUserId).isNotIn(userIdsToInsert);
        assertThat(savedOtherChannelId).isNotEqualTo(savedTargetChannelId);

        // bulk insert 실행 전에는 targetChannel과 otherChannel 모두 ReadStatus가 없어야 한다.
        // 이 사전 검증이 있어야 이후 조회 결과 2건이 burkInsert(...)가 새로 만든 row라는 점이 분명해진다.
        assertThat(readStatusRepository.findByChannelId(savedTargetChannelId)).isEmpty();
        assertThat(readStatusRepository.findByChannelId(savedOtherChannelId)).isEmpty();

        // when
        // targetChannel에 대해 userIdsToInsert에 포함된 기존 사용자 2명의 ReadStatus를 native query로 생성한다.
        // burkInsert(...)는 insert into ... select ... from users where u.id in (:userIds) 형태이므로,
        // DB에 실제로 존재하는 사용자 id만 ReadStatus 생성 대상이 된다.
        int insertedRowsCount = readStatusRepository.burkInsert(savedTargetChannelId, userIdsToInsert, readAt);

        // @Modifying(flushAutomatically = true)로 insert SQL은 flush되지만,
        // native DML 이후 영속성 컨텍스트에 남은 상태가 조회 검증에 영향을 주지 않도록 비운다.
        em.clear();

        // then
        // userIdsToInsert에는 기존 사용자 id 2개가 들어 있으므로 insert 결과 row 수도 2여야 한다.
        assertThat(insertedRowsCount).isEqualTo(2);

        // targetChannel에는 두 사용자에 대한 ReadStatus가 생성되어야 한다.
        List<ReadStatus> readStatuses = readStatusRepository.findByChannelId(savedTargetChannelId);
        assertThat(readStatuses)
                .hasSize(2)
                .extracting(ReadStatus::getChannelId, ReadStatus::getUserId)
                .containsExactlyInAnyOrder(
                        tuple(savedTargetChannelId, savedUserId),
                        tuple(savedTargetChannelId, savedOtherUserId)
                );

        // native query에서 id는 gen_random_uuid()로 생성된다.
        // 따라서 JPA가 new ReadStatus(...)로 만든 객체는 없지만, DB row에는 id가 반드시 있어야 한다.
        assertThat(readStatuses)
                .extracting(ReadStatus::getId)
                .doesNotContainNull();

        // last_read_at은 burkInsert(...)에 전달한 readAt 값으로 저장되어야 한다.
        // H2의 timestamp(6) 정밀도 때문에 Instant의 나노초 값은 미세하게 달라질 수 있어 오차를 허용한다.
        assertThat(readStatuses)
                .allSatisfy(readStatus ->
                        assertThat(Duration.between(readAt, readStatus.getLastReadAt()).abs())
                                .isLessThanOrEqualTo(Duration.ofNanos(1_000))
                );

        // userIdsToInsert에 포함하지 않은 사용자는 DB에 존재하더라도 ReadStatus가 생성되면 안 된다.
        assertThat(readStatusRepository.findByUserId(savedExcludedUserId)).isEmpty();

        // bulk insert 대상이 아닌 채널에도 ReadStatus가 생성되면 안 된다.
        assertThat(readStatusRepository.findByChannelId(savedOtherChannelId)).isEmpty();

        // 최종적으로 생성된 ReadStatus는 targetChannel의 2건뿐이어야 한다.
        assertThat(readStatusRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("읽음 상태 벌크 생성 성공 - 존재하지 않는 사용자 ID는 생성 대상에서 제외")
    void burkInsert_insertsOnlyExistingUsers_whenSomeUserIdsDoNotExist() {
        // given
        // 이 테스트의 대상은 ReadStatusRepository.burkInsert(...) native query의 필터링 조건이다.
        // burkInsert(...)는 전달받은 userIds를 그대로 insert 값으로 쓰는 방식이 아니라,
        // users 테이블에서 "실제로 존재하는 사용자"를 select 한 결과만 read_statuses에 insert한다.
        // 따라서 요청 목록 안에 DB에 없는 UUID가 섞여 있어도,
        // 존재하는 사용자에 대해서만 ReadStatus가 생성되어야 한다.
        UserCreateCommand userCreateCommand = userCreateCommand();
        UserCreateCommand otherUserCreateCommand = userCreateCommand(
                "otherUser",
                "otherPassword",
                "other@gmail.com"
        );
        UserCreateCommand excludedUserCreateCommand = userCreateCommand(
                "excludedUser",
                "excludedPassword",
                "excluded@gmail.com"
        );
        ChannelCreatePublicCommand targetChannelCreateCommand = channelCreatePublicCommand();
        ChannelCreatePublicCommand otherChannelCreateCommand = channelCreatePublicCommand(
                "otherChannel",
                "otherChannelDescription",
                ChannelType.PUBLIC
        );
        Instant readAt = Instant.now();

        // 삽입 대상이 될 실제 사용자 2명과,
        // DB에는 존재하지만 요청 목록에는 넣지 않을 제외 대상 사용자 1명을 저장한다.
        // savedExcludedUser는 "DB에 존재한다는 이유만으로 insert 되면 안 된다"는 대조군이다.
        User savedUser = saveUser(userCreateCommand);
        User savedOtherUser = saveUser(otherUserCreateCommand);
        User savedExcludedUser = saveUser(excludedUserCreateCommand);

        // 다만 실제 사용자 데이터 구조를 유지하고,
        // 이후 findByUserId(...), findByChannelId(...)의 EntityGraph 조회가 깨지지 않도록 함께 구성한다.

        // targetChannel은 bulk insert 대상 채널이고, otherChannel은 비대상 채널이다.
        // userId 필터링뿐 아니라 channelId 조건도 의도대로 적용되는지 함께 확인한다.
        Channel savedTargetChannel = saveChannel(targetChannelCreateCommand);
        Channel savedOtherChannel = saveChannel(otherChannelCreateCommand);

        UUID savedUserId = savedUser.getId();
        UUID savedOtherUserId = savedOtherUser.getId();
        UUID savedExcludedUserId = savedExcludedUser.getId();
        UUID savedTargetChannelId = savedTargetChannel.getId();
        UUID savedOtherChannelId = savedOtherChannel.getId();

        // DB에 저장하지 않은 사용자 id다.
        // 이 id를 요청 목록에는 넣어두되 users 테이블에는 row가 없으므로,
        // insert into ... select ... from users where u.id in (:userIds)의 select 결과에서 제외되어야 한다.
        UUID notSavedUserId = UUID.randomUUID();
        List<UUID> userIdsToInsert = List.of(savedUserId, savedOtherUserId, notSavedUserId);

        // 테스트 fixture가 의도대로 구성됐는지 확인한다.
        // 여기서 id가 null이거나 대조군이 요청 목록에 잘못 포함되면,
        // 실패 원인이 burkInsert(...)의 필터링인지 given 구성 오류인지 구분하기 어렵다.
        assertThat(savedUserId).isNotNull();
        assertThat(savedOtherUserId).isNotNull();
        assertThat(savedExcludedUserId).isNotNull();
        assertThat(savedTargetChannelId).isNotNull();
        assertThat(savedOtherChannelId).isNotNull();
        assertThat(savedOtherUserId).isNotEqualTo(savedUserId);
        assertThat(notSavedUserId).isNotIn(savedUserId, savedOtherUserId, savedExcludedUserId);
        assertThat(userIdsToInsert)
                .contains(savedUserId, savedOtherUserId, notSavedUserId)
                .doesNotContain(savedExcludedUserId);
        assertThat(savedOtherChannelId).isNotEqualTo(savedTargetChannelId);

        // bulk insert 실행 전에는 어느 채널에도 ReadStatus가 없어야 한다.
        // 이 사전 검증이 있어야 아래에서 조회되는 2건이 테스트 준비 과정에서 생긴 row가 아니라
        // burkInsert(...)가 새로 생성한 row라는 점을 확인할 수 있다.
        assertThat(readStatusRepository.findByChannelId(savedTargetChannelId)).isEmpty();
        assertThat(readStatusRepository.findByChannelId(savedOtherChannelId)).isEmpty();

        // when
        // 요청 목록에는 savedUserId, savedOtherUserId, notSavedUserId 세 개가 들어 있다.
        // 하지만 notSavedUserId는 users 테이블에 존재하지 않으므로 실제 insert 대상은 앞의 두 사용자뿐이어야 한다.
        int insertedRowsCount = readStatusRepository.burkInsert(savedTargetChannelId, userIdsToInsert, readAt);

        // native DML 이후에는 영속성 컨텍스트를 비워, 아래 검증이 DB에 실제 반영된 row를 기준으로 수행되게 한다.
        em.clear();

        // then
        // 요청한 userId는 3개지만, 그중 실제 users row가 있는 id는 2개뿐이다.
        // 반환 row 수가 2라는 점은 존재하지 않는 사용자 id가 insert 대상에서 제외됐다는 직접적인 근거다.
        assertThat(insertedRowsCount)
                .isNotEqualTo(userIdsToInsert.size())
                .isEqualTo(2);

        // targetChannel에는 존재하는 두 사용자에 대한 ReadStatus만 생성되어야 한다.
        // notSavedUserId는 요청 목록에 있었더라도 users 테이블에 없으므로 결과에 포함되면 안 된다.
        List<ReadStatus> readStatuses = readStatusRepository.findByChannelId(savedTargetChannelId);
        assertThat(readStatuses)
                .hasSize(2)
                .extracting(ReadStatus::getChannelId, ReadStatus::getUserId)
                .containsExactlyInAnyOrder(
                        tuple(savedTargetChannelId, savedUserId),
                        tuple(savedTargetChannelId, savedOtherUserId)
                )
                .doesNotContain(tuple(savedTargetChannelId, notSavedUserId));

        // native query에서 ReadStatus id는 gen_random_uuid()로 생성된다.
        // JPA의 save(...)를 통하지 않아도 DB row의 식별자는 반드시 채워져 있어야 한다.
        assertThat(readStatuses)
                .extracting(ReadStatus::getId)
                .doesNotContainNull();

        // last_read_at은 burkInsert(...)에 전달한 readAt 값으로 저장되어야 한다.
        // H2 timestamp 정밀도 차이로 Instant 나노초가 미세하게 달라질 수 있어 작은 오차를 허용한다.
        assertThat(readStatuses)
                .allSatisfy(readStatus ->
                        assertThat(Duration.between(readAt, readStatus.getLastReadAt()).abs())
                                .isLessThanOrEqualTo(Duration.ofNanos(1_000))
                );

        // DB에 없는 사용자 id로는 ReadStatus가 생성되지 않아야 한다.
        assertThat(readStatusRepository.findByUserId(notSavedUserId)).isEmpty();

        // DB에는 존재하지만 요청 목록에 포함하지 않은 사용자도 ReadStatus가 생성되면 안 된다.
        assertThat(readStatusRepository.findByUserId(savedExcludedUserId)).isEmpty();

        // bulk insert 대상이 아닌 채널에는 ReadStatus가 생성되면 안 된다.
        assertThat(readStatusRepository.findByChannelId(savedOtherChannelId)).isEmpty();

        // 최종적으로 생성된 ReadStatus는 targetChannel에 연결된 기존 사용자 2건뿐이어야 한다.
        assertThat(readStatusRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("채널별 읽음 상태 목록 조회 성공 - User, 프로필, Channel을 함께 조회")
    void findByChannelId_fetchesUserProfileAndChannel_whenChannelHasReadStatuses() {
        // given
        // 이 테스트의 대상은 ReadStatusRepository.findByChannelId(...) 쿼리다.
        // findByChannelId(...)는 특정 channelId에 연결된 ReadStatus 목록을 조회하면서
        // 조회 결과에서 연관 객체가 초기화되어 있는지 PersistenceUnitUtil로 확인한다.
        UserCreateCommand userCreateCommand = userCreateCommand();
        UserCreateCommand otherUserCreateCommand = userCreateCommand(
                "otherUser",
                "otherPassword",
                "other@gmail.com"
        );
        UserCreateCommand thirdUserCreateCommand = userCreateCommand(
                "thirdUser",
                "thirdPassword",
                "third@gmail.com"
        );
        ChannelCreatePublicCommand targetChannelCreateCommand = channelCreatePublicCommand();
        ChannelCreatePublicCommand otherChannelCreateCommand = channelCreatePublicCommand(
                "otherChannel",
                "otherChannelDescription",
                ChannelType.PUBLIC
        );
        Instant readAt = Instant.now();
        Instant otherChannelReadAt = readAt.plusSeconds(1);

        // 대상 채널에 연결할 사용자 3명을 저장한다.
        // 단건만 저장하면 목록 조회, EntityGraph, channelId 필터링이 우연히 통과할 수 있어 복수 row로 구성한다.
        // 각 사용자에게 서로 다른 BinaryContent 프로필을 연결해야 조회 결과에서 profile fetch join도 확인할 수 있다.
        User savedUser = saveUserWithProfile(userCreateCommand, "originFile1");
        User savedOtherUser = saveUserWithProfile(otherUserCreateCommand, "originFile2");
        User savedThirdUser = saveUserWithProfile(thirdUserCreateCommand, "originFile3");


        // targetChannel은 조회 대상 채널이고, otherChannel은 channelId 필터링을 검증하기 위한 대조군이다.
        // 두 채널 모두 ReadStatus를 갖게 만들어야 findByChannelId(...)가 대상 채널 row만 반환하는지 확인할 수 있다.
        Channel savedTargetChannel = saveChannel(targetChannelCreateCommand);
        Channel savedOtherChannel = saveChannel(otherChannelCreateCommand);

        UUID savedUserId = savedUser.getId();
        UUID savedOtherUserId = savedOtherUser.getId();
        UUID savedThirdUserId = savedThirdUser.getId();
        UUID savedTargetChannelId = savedTargetChannel.getId();
        UUID savedOtherChannelId = savedOtherChannel.getId();
        UUID savedUserBinaryContentId = savedUser.getProfileId();
        UUID savedOtherUserBinaryContentId = savedOtherUser.getProfileId();
        UUID savedThirdUserBinaryContentId = savedThirdUser.getProfileId();

        List<UUID> userIdsToInsert = List.of(savedUserId, savedOtherUserId, savedThirdUserId);

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // id가 null이면 이후 조회 실패가 Repository 쿼리 문제인지 given 구성 문제인지 구분하기 어렵다.
        assertThat(savedUserId).isNotNull();
        assertThat(savedOtherUserId).isNotNull();
        assertThat(savedThirdUserId).isNotNull();
        assertThat(savedTargetChannelId).isNotNull();
        assertThat(savedOtherChannelId).isNotNull();
        assertThat(savedUserBinaryContentId).isNotNull();
        assertThat(savedOtherUserBinaryContentId).isNotNull();
        assertThat(savedThirdUserBinaryContentId).isNotNull();
        assertThat(savedOtherUserId).isNotEqualTo(savedUserId);
        assertThat(savedThirdUserId).isNotIn(savedUserId, savedOtherUserId);
        assertThat(userIdsToInsert).contains(savedUserId, savedOtherUserId, savedThirdUserId);
        assertThat(savedOtherChannelId).isNotEqualTo(savedTargetChannelId);

        // native bulk insert로 두 채널에 같은 사용자 3명의 ReadStatus를 생성한다.
        // targetChannel 3건, otherChannel 3건을 모두 저장해 둔 뒤 targetChannel만 조회하면,
        // findByChannelId(...)의 channel_id 조건이 정확히 적용되는지 확인할 수 있다.
        int targetInsertedRowsCount = readStatusRepository.burkInsert(savedTargetChannelId, userIdsToInsert, readAt);
        int otherInsertedRowsCount = readStatusRepository.burkInsert(savedOtherChannelId, userIdsToInsert, otherChannelReadAt);

        assertThat(targetInsertedRowsCount).isEqualTo(3);
        assertThat(otherInsertedRowsCount).isEqualTo(3);

        // 영속성 컨텍스트를 비워, 아래 조회가 1차 캐시가 아니라 DB에서 다시 읽은 결과를 기준으로 검증되게 한다.
        em.clear();

        // when
        // 조회 대상 채널 id로 ReadStatus 목록을 조회한다.
        List<ReadStatus> readStatuses = readStatusRepository.findByChannelId(savedTargetChannelId);

        PersistenceUnitUtil persistenceUnitUtil = getPersistenceUnitUtil();

        // then
        // findByChannelId(...)의 EntityGraph가 유지되면 ReadStatus.user, ReadStatus.channel뿐 아니라
        // 중첩 경로 문자열("user.profile")을 ReadStatus에 직접 검사하지 않고,
        readStatuses.forEach(readStatus -> {
            assertThat(persistenceUnitUtil.isLoaded(readStatus, "user")).isTrue();
            assertThat(persistenceUnitUtil.isLoaded(readStatus, "channel")).isTrue();

            User loadedUser = readStatus.getUser();
            assertThat(persistenceUnitUtil.isLoaded(loadedUser, "profile")).isTrue();
            });

        // targetChannel에는 사용자 3명의 ReadStatus가 모두 조회되어야 한다.
        // 순서는 Repository 계약이 아니므로 channelId, userId, profileId 조합을 순서와 무관하게 검증한다.
        assertThat(readStatuses)
                .hasSize(3)
                .extracting(
                        ReadStatus::getChannelId,
                        ReadStatus::getUserId,
                        readStatus -> readStatus.getUser().getProfileId()
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                savedTargetChannelId,
                                savedUserId,
                                savedUserBinaryContentId
                        ),
                        tuple(
                                savedTargetChannelId,
                                savedOtherUserId,
                                savedOtherUserBinaryContentId
                        ),
                        tuple(
                                savedTargetChannelId,
                                savedThirdUserId,
                                savedThirdUserBinaryContentId
                        )
                );

        // findByChannelId(...)는 targetChannel만 조회해야 하므로 otherChannel의 ReadStatus는 결과에 섞이면 안 된다.
        assertThat(readStatuses)
                .extracting(ReadStatus::getChannelId)
                .containsOnly(savedTargetChannelId)
                .doesNotContain(savedOtherChannelId);

        // last_read_at은 targetChannel에 bulk insert할 때 전달한 readAt 값이어야 한다.
        // H2 timestamp 정밀도 차이로 Instant의 나노초 값이 미세하게 달라질 수 있어 작은 오차를 허용한다.
        assertThat(readStatuses)
                .allSatisfy(readStatus ->
                        assertThat(Duration.between(readAt, readStatus.getLastReadAt()).abs())
                                .isLessThanOrEqualTo(Duration.ofNanos(1_000))
                );

        // 전체 ReadStatus는 targetChannel 3건 + otherChannel 3건으로 총 6건이다.
        // 이 보조 검증은 대조군 데이터가 실제로 존재했는데도 findByChannelId(...) 결과에는 포함되지 않았다는 점을 뒷받침한다.
        assertThat(readStatusRepository.count()).isEqualTo(6);
    }

    @Test
    @DisplayName("채널별 읽음 상태 목록 조회 성공 - 읽음 상태가 없으면 빈 목록 반환")
    void findByChannelId_returnsEmptyList_whenChannelHasNoReadStatuses() {
        // given
        // 이 테스트의 대상은 ReadStatusRepository.findByChannelId(...)가
        // 조회 대상 channelId에 연결된 ReadStatus가 없을 때 빈 목록을 반환하는지 여부다.
        // 단순히 아무 ReadStatus도 저장하지 않으면 "테이블이 비어 있어서 빈 목록"인 경우와 구분하기 어렵다.
        // 그래서 다른 채널에는 ReadStatus를 실제로 저장해 두고,
        // 조회 대상 채널에는 ReadStatus를 만들지 않는 방식으로 channel_id 조건을 검증한다.
        UserCreateCommand userCreateCommand = userCreateCommand();
        UserCreateCommand otherUserCreateCommand = userCreateCommand(
                "otherUser",
                "otherPassword",
                "other@gmail.com"
        );
        ChannelCreatePublicCommand emptyChannelCreateCommand = channelCreatePublicCommand();
        ChannelCreatePublicCommand channelWithReadStatusesCreateCommand = channelCreatePublicCommand(
                "channelWithReadStatuses",
                "channelWithReadStatusesDescription",
                ChannelType.PUBLIC
        );
        Instant readAt = Instant.now();

        // 대조군 채널에 ReadStatus를 만들 사용자 2명을 저장한다.
        // 조회 대상 채널에는 이 사용자들의 ReadStatus를 생성하지 않으므로,
        // 사용자가 존재하더라도 해당 channelId에 연결된 ReadStatus가 없으면 결과는 빈 목록이어야 한다.
        User savedUser = saveUser(userCreateCommand);
        User savedOtherUser = saveUser(otherUserCreateCommand);

        // 다만 실제 사용자 생성 흐름과 유사한 fixture를 유지하고,
        // 다른 채널의 ReadStatus 조회가 EntityGraph 때문에 깨지지 않도록 실제 row로 구성한다.

        // emptyChannel은 조회 대상이지만 ReadStatus가 없는 채널이다.
        // channelWithReadStatuses는 대조군으로, 같은 테스트 DB 안에 ReadStatus가 실제로 존재함을 보여준다.
        Channel savedEmptyChannel = saveChannel(emptyChannelCreateCommand);
        Channel savedChannelWithReadStatuses = saveChannel(channelWithReadStatusesCreateCommand);

        UUID savedUserId = savedUser.getId();
        UUID savedOtherUserId = savedOtherUser.getId();
        UUID savedEmptyChannelId = savedEmptyChannel.getId();
        UUID savedChannelWithReadStatusesId = savedChannelWithReadStatuses.getId();

        List<UUID> userIdsToInsert = List.of(savedUserId, savedOtherUserId);

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // 여기서 id가 null이거나 두 채널 id가 같으면, 빈 결과가 Repository 조건 때문인지 given 오류 때문인지 구분하기 어렵다.
        assertThat(savedUserId).isNotNull();
        assertThat(savedOtherUserId).isNotNull();
        assertThat(savedEmptyChannelId).isNotNull();
        assertThat(savedChannelWithReadStatusesId).isNotNull();
        assertThat(savedOtherUserId).isNotEqualTo(savedUserId);
        assertThat(userIdsToInsert).contains(savedUserId, savedOtherUserId);
        assertThat(savedChannelWithReadStatusesId).isNotEqualTo(savedEmptyChannelId);

        // 대조군 채널에만 ReadStatus를 생성한다.
        // 이때 burkInsert(...)는 insert into ... select ... 형태라 사용자 2명에 대해 row 2건을 만든다.
        int insertedRowsCount = readStatusRepository.burkInsert(savedChannelWithReadStatusesId, userIdsToInsert, readAt);

        assertThat(insertedRowsCount).isEqualTo(2);

        // 영속성 컨텍스트를 비워, 아래 조회가 저장 직후 관리 객체가 아니라 DB의 실제 row를 읽도록 한다.
        em.clear();

        // 대조군 채널에는 ReadStatus가 실제로 존재해야 한다.
        // 이 사전 검증이 있어야 조회 대상 채널의 빈 목록이 "전체 테이블이 비어서" 나온 결과가 아님을 확인할 수 있다.
        List<ReadStatus> readStatusesForChannelWithData = readStatusRepository.findByChannelId(savedChannelWithReadStatusesId);
        assertThat(readStatusesForChannelWithData)
                .hasSize(2)
                .extracting(ReadStatus::getChannelId, ReadStatus::getUserId)
                .containsExactlyInAnyOrder(
                        tuple(savedChannelWithReadStatusesId, savedUserId),
                        tuple(savedChannelWithReadStatusesId, savedOtherUserId)
                );

        // when
        // ReadStatus를 하나도 연결하지 않은 채널 id로 조회한다.
        List<ReadStatus> foundReadStatuses = readStatusRepository.findByChannelId(savedEmptyChannelId);

        // then
        // 같은 DB 안에 다른 채널의 ReadStatus가 존재하더라도,
        // 조회 대상 channelId에 해당하는 row가 없으면 빈 목록을 반환해야 한다.
        assertThat(foundReadStatuses).isEmpty();

        // 전체 ReadStatus 수는 대조군 채널에 만든 2건이어야 한다.
        // 조회 대상 빈 채널에 row가 실수로 생성되지 않았다는 보조 검증이다.
        assertThat(readStatusRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("여러 채널 읽음 상태 목록 조회 성공 - 채널 ID 목록에 포함된 읽음 상태 반환")
    void findByChannelIdIn_returnsReadStatusesMatchingChannelIds() {
        // given
        // 이 테스트의 대상은 ReadStatusRepository.findByChannelIdIn(...) 쿼리다.
        // channelIds 목록에 포함된 여러 채널의 ReadStatus만 반환하고,
        // 목록에 포함하지 않은 채널의 ReadStatus는 제외되어야 한다.
        // 단순히 조회 대상 채널의 row만 저장하면 IN 조건이 정확히 동작하는지 확인하기 어렵기 때문에,
        // 포함 채널 2개와 제외 채널 1개를 모두 같은 테스트 DB에 준비한다.
        UserCreateCommand userCreateCommand = userCreateCommand();
        UserCreateCommand otherUserCreateCommand = userCreateCommand(
                "otherUser",
                "otherPassword",
                "other@gmail.com"
        );
        ChannelCreatePublicCommand firstIncludedChannelCreateCommand = channelCreatePublicCommand();
        ChannelCreatePublicCommand secondIncludedChannelCreateCommand = channelCreatePublicCommand(
                "secondIncludedChannel",
                "secondIncludedChannelDescription",
                ChannelType.PUBLIC
        );
        ChannelCreatePublicCommand excludedChannelCreateCommand = channelCreatePublicCommand(
                "excludedChannel",
                "excludedChannelDescription",
                ChannelType.PUBLIC
        );
        Instant readAt = Instant.now();

        // 두 사용자를 저장한다.
        // 각 채널에 같은 사용자 2명의 ReadStatus를 생성하면,
        // 결과 검증에서 channelId와 userId 조합을 명확히 확인할 수 있다.
        User savedUser = saveUser(userCreateCommand);
        User savedOtherUser = saveUser(otherUserCreateCommand);


        // firstIncludedChannel, secondIncludedChannel은 조회 목록에 포함할 채널이다.
        // excludedChannel은 ReadStatus가 존재하지만 조회 목록에는 넣지 않을 대조군 채널이다.
        Channel savedFirstIncludedChannel = saveChannel(firstIncludedChannelCreateCommand);
        Channel savedSecondIncludedChannel = saveChannel(secondIncludedChannelCreateCommand);
        Channel savedExcludedChannel = saveChannel(excludedChannelCreateCommand);

        UUID savedUserId = savedUser.getId();
        UUID savedOtherUserId = savedOtherUser.getId();
        UUID savedFirstIncludedChannelId = savedFirstIncludedChannel.getId();
        UUID savedSecondIncludedChannelId = savedSecondIncludedChannel.getId();
        UUID savedExcludedChannelId = savedExcludedChannel.getId();

        List<UUID> userIdsToInsert = List.of(savedUserId, savedOtherUserId);
        List<UUID> channelIdsToFind = List.of(savedFirstIncludedChannelId, savedSecondIncludedChannelId);

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // 사용자와 채널 id가 null이거나 서로 구분되지 않으면 IN 조건 검증이 의미 없어질 수 있다.
        assertThat(savedUserId).isNotNull();
        assertThat(savedOtherUserId).isNotNull();
        assertThat(savedFirstIncludedChannelId).isNotNull();
        assertThat(savedSecondIncludedChannelId).isNotNull();
        assertThat(savedExcludedChannelId).isNotNull();
        assertThat(savedOtherUserId).isNotEqualTo(savedUserId);
        assertThat(userIdsToInsert).contains(savedUserId, savedOtherUserId);
        assertThat(savedSecondIncludedChannelId).isNotEqualTo(savedFirstIncludedChannelId);
        assertThat(savedExcludedChannelId).isNotIn(channelIdsToFind);

        // 포함 채널 2개와 제외 채널 1개에 각각 ReadStatus 2건씩 생성한다.
        // burkInsert(...)는 채널별로 insert into ... select ... SQL을 한 번 실행하고,
        // users 테이블에서 userIdsToInsert에 해당하는 사용자 2명을 선택해 row 2건을 만든다.
        int firstIncludedInsertedRowsCount = readStatusRepository.burkInsert(savedFirstIncludedChannelId, userIdsToInsert, readAt);
        int secondIncludedInsertedRowsCount = readStatusRepository.burkInsert(savedSecondIncludedChannelId, userIdsToInsert, readAt);
        int excludedInsertedRowsCount = readStatusRepository.burkInsert(savedExcludedChannelId, userIdsToInsert, readAt);

        assertThat(firstIncludedInsertedRowsCount).isEqualTo(2);
        assertThat(secondIncludedInsertedRowsCount).isEqualTo(2);
        assertThat(excludedInsertedRowsCount).isEqualTo(2);

        // 영속성 컨텍스트를 비워, 아래 조회가 1차 캐시가 아니라 DB의 실제 row 기준으로 수행되게 한다.
        em.clear();

        // 전체 ReadStatus는 3개 채널 x 2명으로 총 6건이어야 한다.
        // 이 사전 검증이 있어야 제외 채널 row가 실제로 존재하는 상황에서 IN 조건이 적용되는지 확인할 수 있다.
        assertThat(readStatusRepository.count()).isEqualTo(6);

        // when
        // 조회 목록에는 포함 채널 2개의 id만 전달하고, excludedChannelId는 전달하지 않는다.
        List<ReadStatus> fetchedReadStatuses = readStatusRepository.findByChannelIdIn(channelIdsToFind);

        // then
        // 조회 결과는 포함 채널 2개에 연결된 ReadStatus 4건이어야 한다.
        // 순서는 Repository 계약이 아니므로 channelId, userId 조합을 순서와 무관하게 검증한다.
        assertThat(fetchedReadStatuses)
                .hasSize(4)
                .extracting(ReadStatus::getChannelId, ReadStatus::getUserId)
                .containsExactlyInAnyOrder(
                        tuple(savedFirstIncludedChannelId, savedUserId),
                        tuple(savedFirstIncludedChannelId, savedOtherUserId),
                        tuple(savedSecondIncludedChannelId, savedUserId),
                        tuple(savedSecondIncludedChannelId, savedOtherUserId)
                );

        // excludedChannel에도 ReadStatus 2건이 존재하지만,
        // channelIdsToFind에 포함하지 않았으므로 조회 결과에는 섞이면 안 된다.
        assertThat(fetchedReadStatuses)
                .extracting(ReadStatus::getChannelId)
                .containsOnly(savedFirstIncludedChannelId, savedSecondIncludedChannelId)
                .doesNotContain(savedExcludedChannelId);

        // last_read_at은 bulk insert에 전달한 readAt 값이어야 한다.
        // H2 timestamp 정밀도 차이로 Instant의 나노초 값이 미세하게 달라질 수 있어 abs()로 작은 오차만 허용한다.
        assertThat(fetchedReadStatuses)
                .allSatisfy(readStatus ->
                        assertThat(Duration.between(readAt, readStatus.getLastReadAt()).abs())
                                .isLessThanOrEqualTo(Duration.ofNanos(1_000))
                );

        // 조회 결과는 4건이지만 DB 전체에는 제외 채널의 2건까지 남아 있어야 한다.
        // 이 보조 검증은 findByChannelIdIn(...)이 삭제나 상태 변경을 하지 않는 순수 조회임을 함께 확인한다.
        assertThat(readStatusRepository.count()).isEqualTo(6);

    }

    @Test
    @DisplayName("채널 기준 읽음 상태 존재 여부 조회 성공 - 채널에 읽음 상태가 있으면 true 반환")
    void existsByChannelId_returnsTrue_whenChannelHasReadStatuses() {
        // given
        // 이 테스트의 대상은 ReadStatusRepository.existsByChannel_Id(...) derived query다.
        // 특정 channelId에 연결된 ReadStatus가 하나라도 있으면 true를 반환해야 한다.
        // 단순히 채널 하나와 ReadStatus 하나만 저장하면 "채널이 존재해서 true"인지,
        // "ReadStatus가 존재해서 true"인지 테스트 의도가 흐려질 수 있다.
        // 그래서 ReadStatus가 있는 targetChannel과, 채널 row만 있고 ReadStatus는 없는 emptyChannel을 함께 준비한다.
        UserCreateCommand userCreateCommand = userCreateCommand();
        UserCreateCommand otherUserCreateCommand = userCreateCommand(
                "otherUser",
                "otherPassword",
                "other@gmail.com"
        );
        ChannelCreatePublicCommand targetChannelCreateCommand = channelCreatePublicCommand();
        ChannelCreatePublicCommand emptyChannelCreateCommand = channelCreatePublicCommand(
                "emptyChannel",
                "emptyChannelDescription",
                ChannelType.PUBLIC
        );
        Instant readAt = Instant.now();

        // targetChannel에 연결할 사용자 2명을 저장한다.
        // ReadStatus가 여러 건 있어도 existsByChannel_Id(...)의 결과는 true 하나로 수렴해야 한다.
        User savedUser = saveUser(userCreateCommand);
        User savedOtherUser = saveUser(otherUserCreateCommand);

        // 다만 실제 사용자 생성 흐름과 유사한 fixture를 유지하고,
        // ReadStatus 조회를 통한 사전 검증 시 EntityGraph가 깨지지 않도록 실제 row로 구성한다.

        // targetChannel에는 ReadStatus를 생성하고, emptyChannel에는 생성하지 않는다.
        // 이렇게 해야 existsByChannel_Id(...)가 Channel 존재 여부가 아니라 ReadStatus 존재 여부를 기준으로 판단하는지 확인할 수 있다.
        Channel savedTargetChannel = saveChannel(targetChannelCreateCommand);
        Channel savedEmptyChannel = saveChannel(emptyChannelCreateCommand);

        UUID savedUserId = savedUser.getId();
        UUID savedOtherUserId = savedOtherUser.getId();
        UUID savedTargetChannelId = savedTargetChannel.getId();
        UUID savedEmptyChannelId = savedEmptyChannel.getId();

        List<UUID> userIdsToInsert = List.of(savedUserId, savedOtherUserId);

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // id가 null이거나 두 채널이 구분되지 않으면 exists 결과의 원인을 명확히 판단하기 어렵다.
        assertThat(savedUserId).isNotNull();
        assertThat(savedOtherUserId).isNotNull();
        assertThat(savedTargetChannelId).isNotNull();
        assertThat(savedEmptyChannelId).isNotNull();
        assertThat(savedOtherUserId).isNotEqualTo(savedUserId);
        assertThat(userIdsToInsert).contains(savedUserId, savedOtherUserId);
        assertThat(savedEmptyChannelId).isNotEqualTo(savedTargetChannelId);

        // targetChannel에만 ReadStatus 2건을 생성한다.
        // burkInsert(...)는 insert into ... select ... 형태라 userIdsToInsert에 해당하는 기존 사용자 2명만 insert 대상이 된다.
        int insertedRowsCount = readStatusRepository.burkInsert(savedTargetChannelId, userIdsToInsert, readAt);

        assertThat(insertedRowsCount).isEqualTo(2);

        // 영속성 컨텍스트를 비워, 아래 사전 조회와 exists 조회가 DB에 실제 반영된 row를 기준으로 수행되게 한다.
        em.clear();

        // exists 조회 전에 targetChannel에는 ReadStatus가 실제로 존재하고,
        // emptyChannel에는 ReadStatus가 없다는 fixture 상태를 먼저 확인한다.
        // 이 검증이 있어야 true 결과가 given 구성 실패로 우연히 나온 것이 아님을 알 수 있다.
        assertThat(readStatusRepository.findByChannelId(savedTargetChannelId))
                .hasSize(2)
                .extracting(ReadStatus::getChannelId, ReadStatus::getUserId)
                .containsExactlyInAnyOrder(
                        tuple(savedTargetChannelId, savedUserId),
                        tuple(savedTargetChannelId, savedOtherUserId)
                );
        assertThat(readStatusRepository.findByChannelId(savedEmptyChannelId)).isEmpty();

        // when
        // ReadStatus가 연결된 targetChannel id로 존재 여부를 조회한다.
        boolean exists = readStatusRepository.existsByChannel_Id(savedTargetChannelId);

        // then
        // targetChannel에는 ReadStatus가 하나 이상 존재하므로 true를 반환해야 한다.
        assertThat(exists).isTrue();

        // 전체 ReadStatus 수는 targetChannel에 생성한 2건뿐이어야 한다.
        // emptyChannel에는 ReadStatus가 실수로 생성되지 않았다는 보조 검증이다.
        assertThat(readStatusRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("채널 기준 읽음 상태 존재 여부 조회 성공 - 채널에 읽음 상태가 없으면 false 반환")
    void existsByChannelId_returnsFalse_whenChannelHasNoReadStatuses() {
        // given
        // 이 테스트의 대상은 ReadStatusRepository.existsByChannel_Id(...) derived query다.
        // 특정 channelId에 연결된 ReadStatus가 없으면 false를 반환해야 한다.
        // 여기서 중요한 점은 "채널 자체가 존재하지 않아서 false"가 아니라,
        // "채널 row는 존재하지만 read_statuses row가 없어서 false"인 상황을 검증하는 것이다.
        // 그래서 targetChannel에는 ReadStatus를 만들고,
        // emptyChannel은 Channel row만 저장한 뒤 ReadStatus는 연결하지 않는다.
        UserCreateCommand userCreateCommand = userCreateCommand();
        UserCreateCommand otherUserCreateCommand = userCreateCommand(
                "otherUser",
                "otherPassword",
                "other@gmail.com"
        );
        ChannelCreatePublicCommand targetChannelCreateCommand = channelCreatePublicCommand();
        ChannelCreatePublicCommand emptyChannelCreateCommand = channelCreatePublicCommand(
                "emptyChannel",
                "emptyChannelDescription",
                ChannelType.PUBLIC
        );
        Instant readAt = Instant.now();

        // targetChannel에 ReadStatus를 만들 사용자 2명을 저장한다.
        // emptyChannel에도 같은 사용자들이 존재한다고 해서 ReadStatus가 자동으로 생기는 것은 아니므로,
        // existsByChannel_Id(...)는 users가 아니라 read_statuses의 channel_id 존재 여부를 기준으로 판단해야 한다.
        User savedUser = saveUser(userCreateCommand);
        User savedOtherUser = saveUser(otherUserCreateCommand);

        // 다만 실제 사용자 생성 흐름과 유사한 fixture를 유지하고,

        // targetChannel은 ReadStatus가 존재하는 대조군 채널이다.
        // emptyChannel은 조회 대상이지만 ReadStatus가 없는 채널이다.
        Channel savedTargetChannel = saveChannel(targetChannelCreateCommand);
        Channel savedEmptyChannel = saveChannel(emptyChannelCreateCommand);

        UUID savedUserId = savedUser.getId();
        UUID savedOtherUserId = savedOtherUser.getId();
        UUID savedTargetChannelId = savedTargetChannel.getId();
        UUID savedEmptyChannelId = savedEmptyChannel.getId();

        List<UUID> userIdsToInsert = List.of(savedUserId, savedOtherUserId);

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // id가 null이거나 두 채널이 같은 값이면 false 결과가 Repository 조건 때문인지 given 구성 오류인지 구분하기 어렵다.
        assertThat(savedUserId).isNotNull();
        assertThat(savedOtherUserId).isNotNull();
        assertThat(savedTargetChannelId).isNotNull();
        assertThat(savedEmptyChannelId).isNotNull();
        assertThat(savedOtherUserId).isNotEqualTo(savedUserId);
        assertThat(userIdsToInsert).contains(savedUserId, savedOtherUserId);
        assertThat(savedEmptyChannelId).isNotEqualTo(savedTargetChannelId);

        // targetChannel에만 ReadStatus 2건을 생성한다.
        // emptyChannel에는 ReadStatus를 만들지 않아야 existsByChannel_Id(emptyChannelId)가 false가 되는 조건을 만들 수 있다.
        int insertedRowsCount = readStatusRepository.burkInsert(savedTargetChannelId, userIdsToInsert, readAt);

        assertThat(insertedRowsCount).isEqualTo(2);

        // 영속성 컨텍스트를 비워, 아래 사전 조회와 exists 조회가 DB에 실제 반영된 row를 기준으로 수행되게 한다.
        em.clear();

        // exists 조회 전에 targetChannel에는 ReadStatus가 실제로 존재하고,
        // emptyChannel에는 ReadStatus가 없다는 fixture 상태를 먼저 확인한다.
        // 이 검증이 없으면 false 결과가 "원래 테스트 데이터가 하나도 없어서" 나온 것인지 구분하기 어렵다.
        assertThat(readStatusRepository.findByChannelId(savedTargetChannelId))
                .hasSize(2)
                .extracting(ReadStatus::getChannelId, ReadStatus::getUserId)
                .containsExactlyInAnyOrder(
                        tuple(savedTargetChannelId, savedUserId),
                        tuple(savedTargetChannelId, savedOtherUserId)
                );
        assertThat(readStatusRepository.findByChannelId(savedEmptyChannelId)).isEmpty();

        // when
        // Channel row는 존재하지만 ReadStatus가 하나도 연결되지 않은 emptyChannel id로 존재 여부를 조회한다.
        boolean exists = readStatusRepository.existsByChannel_Id(savedEmptyChannelId);

        // then
        // emptyChannel 자체는 DB에 존재하지만 read_statuses.channel_id와 매칭되는 row가 없으므로 false를 반환해야 한다.
        assertThat(exists).isFalse();

        // 전체 ReadStatus 수는 targetChannel에 만든 2건뿐이어야 한다.
        // existsByChannel_Id(...)가 조회 과정에서 데이터를 변경하지 않는다는 점도 보조적으로 확인한다.
        assertThat(readStatusRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("읽음 상태 단건 조회 성공 - User와 Channel을 함께 조회")
    void findById_fetchesUserAndChannel_whenReadStatusExists() {
        // given
        // 이 테스트의 대상은 ReadStatusRepository.findById(...) override 쿼리다.
        // Repository에는 findById(...)에 EntityGraph(user, channel)가 선언되어 있으므로,
        // 단건 조회 결과에서 ReadStatus.user와 ReadStatus.channel이 함께 로딩되어야 한다.
        // 또한 id 기반 조회가 정확히 동작하는지 확인하기 위해 조회 대상 ReadStatus와 대조군 ReadStatus를 함께 저장한다.
        UserCreateCommand userCreateCommand = userCreateCommand();
        UserCreateCommand otherUserCreateCommand = userCreateCommand(
                "otherUser",
                "otherPassword",
                "other@gmail.com"
        );
        ChannelCreatePublicCommand targetChannelCreateCommand = channelCreatePublicCommand();
        ChannelCreatePublicCommand otherChannelCreateCommand = channelCreatePublicCommand(
                "otherChannel",
                "otherChannelDescription",
                ChannelType.PUBLIC
        );
        Instant readAt = Instant.now();
        Instant otherReadAt = readAt.plusSeconds(1);

        // 조회 대상 ReadStatus와 대조군 ReadStatus에 사용할 사용자 2명을 저장한다.
        // findById(...)는 userId 조건으로 조회하지 않지만, 서로 다른 사용자와 채널을 준비하면
        // 반환된 row가 조회 대상 id에 해당하는 정확한 row인지 더 명확히 검증할 수 있다.
        User savedUser = saveUser(userCreateCommand);
        User savedOtherUser = saveUser(otherUserCreateCommand);

        // 다만 실제 사용자 생성 흐름과 유사한 fixture를 유지하기 위해 실제 row로 저장한다.

        Channel savedTargetChannel = saveChannel(targetChannelCreateCommand);
        Channel savedOtherChannel = saveChannel(otherChannelCreateCommand);

        // 조회 대상 ReadStatus는 targetChannel + savedUser 조합으로 저장한다.
        // ReadStatus 생성자에는 User 엔티티와 ReadStatusCreateCommand가 함께 들어가므로,
        // command의 userId도 실제 연결 사용자(savedUser)의 id와 맞춰 테스트 fixture가 헷갈리지 않게 한다.
        ReadStatus savedReadStatus = saveReadStatus(savedTargetChannel, savedUser, readAt);

        // 대조군 ReadStatus를 하나 더 저장한다.
        // 이 row가 있어야 findById(savedReadStatusId)가 단순히 첫 번째 row를 우연히 가져온 것이 아니라,
        // 전달한 id에 해당하는 row를 정확히 조회한다는 점을 함께 확인할 수 있다.
        ReadStatus savedOtherReadStatus = saveReadStatus(savedOtherChannel, savedOtherUser, otherReadAt);

        UUID savedUserId = savedUser.getId();
        UUID savedOtherUserId = savedOtherUser.getId();
        UUID savedTargetChannelId = savedTargetChannel.getId();
        UUID savedOtherChannelId = savedOtherChannel.getId();
        UUID savedReadStatusId = savedReadStatus.getId();
        UUID savedOtherReadStatusId = savedOtherReadStatus.getId();

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // id가 null이거나 사용자/채널/ReadStatus가 서로 구분되지 않으면,
        // findById(...) 결과가 정확한 row인지 판단하기 어렵다.
        assertThat(savedUserId).isNotNull();
        assertThat(savedOtherUserId).isNotNull();
        assertThat(savedTargetChannelId).isNotNull();
        assertThat(savedOtherChannelId).isNotNull();
        assertThat(savedReadStatusId).isNotNull();
        assertThat(savedOtherReadStatusId).isNotNull();
        assertThat(savedOtherUserId).isNotEqualTo(savedUserId);
        assertThat(savedOtherChannelId).isNotEqualTo(savedTargetChannelId);
        assertThat(savedOtherReadStatusId).isNotEqualTo(savedReadStatusId);

        // 저장 직후 엔티티의 외래키 기준 값도 확인한다.
        // 여기서 fixture가 어긋나면 이후 findById(...) 검증 실패 원인을 Repository 문제가 아니라 given 구성 오류로 봐야 한다.
        assertThat(savedReadStatus.getChannelId()).isEqualTo(savedTargetChannelId);
        assertThat(savedReadStatus.getUserId()).isEqualTo(savedUserId);
        assertThat(savedOtherReadStatus.getChannelId()).isEqualTo(savedOtherChannelId);
        assertThat(savedOtherReadStatus.getUserId()).isEqualTo(savedOtherUserId);

        // 영속성 컨텍스트를 비워, findById(...)가 1차 캐시에 남아 있는 savedReadStatus가 아니라
        // DB에서 다시 조회한 엔티티를 반환하도록 한다.
        em.clear();

        // when
        // 조회 대상 ReadStatus id로 단건 조회한다.
        ReadStatus readStatus = readStatusRepository.findById(savedReadStatusId).orElseThrow(AssertionError::new);
        PersistenceUnitUtil persistenceUnitUtil = getPersistenceUnitUtil();

        // then
        // findById(...)에 선언된 EntityGraph가 유지되면 user와 channel은 이미 로딩된 상태여야 한다.
        // 이 검증은 이후 getUserId(), getChannelId() 호출로 지연 로딩이 발생하기 전에 먼저 수행한다.
        assertThat(persistenceUnitUtil.isLoaded(readStatus, "user")).isTrue();
        assertThat(persistenceUnitUtil.isLoaded(readStatus, "channel")).isTrue();

        // 조회 결과는 savedReadStatusId에 해당하는 row여야 한다.
        // 대조군 row(savedOtherReadStatus)가 있어도 id, channelId, userId가 섞이면 안 된다.
        assertThat(readStatus.getId()).isEqualTo(savedReadStatusId);
        assertThat(readStatus.getChannelId()).isEqualTo(savedTargetChannelId);
        assertThat(readStatus.getUserId()).isEqualTo(savedUserId);
        assertThat(readStatus.getId()).isNotEqualTo(savedOtherReadStatusId);
        assertThat(readStatus.getChannelId()).isNotEqualTo(savedOtherChannelId);
        assertThat(readStatus.getUserId()).isNotEqualTo(savedOtherUserId);

        // last_read_at은 저장할 때 전달한 readAt 값이어야 한다.
        // H2 timestamp 정밀도 차이로 Instant의 나노초 값이 미세하게 달라질 수 있어 작은 오차를 허용한다.
        assertThat(Duration.between(readStatus.getLastReadAt(), readAt).abs())
                .isLessThanOrEqualTo(Duration.ofNanos(1_000));

        // 전체 ReadStatus 수는 조회 대상 1건 + 대조군 1건으로 총 2건이어야 한다.
        // findById(...)가 조회 과정에서 데이터를 변경하지 않는다는 점을 보조적으로 확인한다.
        assertThat(readStatusRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("읽음 상태 단건 조회 성공 - 존재하지 않으면 Optional.empty 반환")
    void findById_returnsEmpty_whenReadStatusDoesNotExist() {
        // given
        // 이 테스트의 대상은 ReadStatusRepository.findById(...)가
        // DB에 존재하지 않는 ReadStatus id를 조회했을 때 Optional.empty를 반환하는지 여부다.
        // 단순히 read_statuses 테이블을 비워 둔 상태에서 조회하면 "테이블이 비어서 empty"인 경우와 구분하기 어렵다.
        // 그래서 실제 ReadStatus row를 2건 저장해 둔 뒤,
        // 그 어떤 row의 id와도 일치하지 않는 UUID로 조회해 empty가 반환되는지 검증한다.
        UserCreateCommand userCreateCommand = userCreateCommand();
        UserCreateCommand otherUserCreateCommand = userCreateCommand(
                "otherUser",
                "otherPassword",
                "other@gmail.com"
        );
        ChannelCreatePublicCommand targetChannelCreateCommand = channelCreatePublicCommand();
        ChannelCreatePublicCommand otherChannelCreateCommand = channelCreatePublicCommand(
                "otherChannel",
                "otherChannelDescription",
                ChannelType.PUBLIC
        );
        Instant readAt = Instant.now();
        Instant otherReadAt = readAt.plusSeconds(1);

        // 서로 다른 ReadStatus row를 만들기 위해 사용자 2명을 저장한다.
        // 조회 대상이 아닌 기존 row가 여러 개 있어도, id가 일치하지 않으면 findById(...)는 empty를 반환해야 한다.
        User savedUser = saveUser(userCreateCommand);
        User savedOtherUser = saveUser(otherUserCreateCommand);

        // 다만 실제 사용자 생성 흐름과 유사한 fixture를 유지하기 위해 실제 row로 구성한다.

        // 서로 다른 채널 2개를 저장해, 기존 ReadStatus row들이 서로 다른 user/channel 조합을 갖도록 한다.
        // 이렇게 하면 notSavedReadStatusId가 기존 어떤 row와도 매칭되지 않는다는 점이 더 명확해진다.
        Channel savedTargetChannel = saveChannel(targetChannelCreateCommand);
        Channel savedOtherChannel = saveChannel(otherChannelCreateCommand);

        // DB에 실제로 존재하는 ReadStatus 2건을 저장한다.
        // 이 row들은 notSavedReadStatusId 조회 시 반환되면 안 되는 대조군이다.
        ReadStatus savedReadStatus = saveReadStatus(savedTargetChannel, savedUser, readAt);

        ReadStatus savedOtherReadStatus = saveReadStatus(savedOtherChannel, savedOtherUser, otherReadAt);

        UUID savedUserId = savedUser.getId();
        UUID savedOtherUserId = savedOtherUser.getId();
        UUID savedTargetChannelId = savedTargetChannel.getId();
        UUID savedOtherChannelId = savedOtherChannel.getId();
        UUID savedReadStatusId = savedReadStatus.getId();
        UUID savedOtherReadStatusId = savedOtherReadStatus.getId();

        // DB에 저장하지 않은 ReadStatus id다.
        // 이 id가 기존 ReadStatus id와 우연히 같지 않다는 점을 아래 fixture 검증에서 확인한다.
        UUID notSavedReadStatusId = UUID.randomUUID();

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // 기존 row의 id가 null이거나 notSavedReadStatusId와 겹치면,
        // Optional.empty 검증이 올바른 부재 조회 검증이 아니게 된다.
        assertThat(savedUserId).isNotNull();
        assertThat(savedOtherUserId).isNotNull();
        assertThat(savedTargetChannelId).isNotNull();
        assertThat(savedOtherChannelId).isNotNull();
        assertThat(savedReadStatusId).isNotNull();
        assertThat(savedOtherReadStatusId).isNotNull();
        assertThat(savedOtherUserId).isNotEqualTo(savedUserId);
        assertThat(savedOtherChannelId).isNotEqualTo(savedTargetChannelId);
        assertThat(savedOtherReadStatusId).isNotEqualTo(savedReadStatusId);
        assertThat(List.of(savedReadStatusId, savedOtherReadStatusId))
                .doesNotContain(notSavedReadStatusId);

        // 저장 직후 ReadStatus의 user/channel 연결도 확인한다.
        // 여기서 fixture가 어긋나면 findById(...) empty 검증 실패 원인을 Repository 문제가 아니라 given 구성 오류로 봐야 한다.
        assertThat(savedReadStatus.getChannelId()).isEqualTo(savedTargetChannelId);
        assertThat(savedReadStatus.getUserId()).isEqualTo(savedUserId);
        assertThat(savedOtherReadStatus.getChannelId()).isEqualTo(savedOtherChannelId);
        assertThat(savedOtherReadStatus.getUserId()).isEqualTo(savedOtherUserId);

        // 영속성 컨텍스트를 비워, 아래 findById(...)가 1차 캐시에 남은 엔티티가 아니라 DB 조회 기준으로 동작하게 한다.
        em.clear();

        // when
        // DB에 저장하지 않은 ReadStatus id로 단건 조회한다.
        Optional<ReadStatus> optionalReadStatus = readStatusRepository.findById(notSavedReadStatusId);

        // then
        // read_statuses 테이블에는 row가 2건 존재하지만,
        // 조회 id와 일치하는 row가 없으므로 Optional.empty를 반환해야 한다.
        assertThat(optionalReadStatus).isEmpty();

        // 전체 ReadStatus 수는 기존에 저장한 2건 그대로여야 한다.
        // findById(...)가 조회 과정에서 데이터를 변경하지 않는다는 점을 보조적으로 확인한다.
        assertThat(readStatusRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("사용자 기준 읽음 상태 존재 여부 조회 성공 - 사용자의 읽음 상태가 있으면 true 반환")
    void existsByUserId_returnsTrue_whenUserHasReadStatuses() {
        // given
        // 이 테스트의 대상은 ReadStatusRepository.existsByUser_Id(...) derived query다.
        // 특정 userId에 연결된 ReadStatus가 하나라도 있으면 true를 반환해야 한다.
        // 단순히 사용자 row만 저장하면 "사용자가 존재해서 true"인지,
        // "ReadStatus가 존재해서 true"인지 구분할 수 없다.
        // 그래서 savedUser에는 ReadStatus를 만들고, savedOtherUser에는 별도 ReadStatus를 만들어 대조군으로 둔다.
        UserCreateCommand userCreateCommand = userCreateCommand();
        UserCreateCommand otherUserCreateCommand = userCreateCommand(
                "otherUser",
                "otherPassword",
                "other@gmail.com"
        );
        ChannelCreatePublicCommand targetChannelCreateCommand = channelCreatePublicCommand();
        ChannelCreatePublicCommand otherChannelCreateCommand = channelCreatePublicCommand(
                "otherChannel",
                "otherChannelDescription",
                ChannelType.PUBLIC
        );
        Instant readAt = Instant.now();
        Instant otherReadAt = readAt.plusSeconds(1);

        // 조회 대상 사용자와 대조군 사용자를 저장한다.
        // existsByUser_Id(savedUserId)는 savedUser에 연결된 ReadStatus 존재 여부만 확인해야 한다.
        User savedUser = saveUser(userCreateCommand);
        User savedOtherUser = saveUser(otherUserCreateCommand);

        // 다만 실제 사용자 생성 흐름과 유사한 fixture를 유지하고,

        // 서로 다른 채널을 저장해 두 사용자 각각의 ReadStatus가 서로 다른 row임을 명확히 한다.
        Channel savedTargetChannel = saveChannel(targetChannelCreateCommand);
        Channel savedOtherChannel = saveChannel(otherChannelCreateCommand);

        // 조회 대상 사용자(savedUser)에 연결된 ReadStatus다.
        // existsByUser_Id(savedUserId)가 true를 반환해야 하는 직접 근거가 되는 row다.
        ReadStatus savedReadStatus = saveReadStatus(savedTargetChannel, savedUser, readAt);

        // 대조군 사용자(savedOtherUser)에도 ReadStatus를 저장한다.
        // 이 row가 있어야 existsByUser_Id(...)가 단순히 read_statuses 테이블에 row가 있으면 true를 반환하는 것이 아니라,
        // 전달한 userId와 매칭되는 row를 기준으로 판단하는지 함께 확인할 수 있다.
        ReadStatus savedOtherReadStatus = saveReadStatus(savedOtherChannel, savedOtherUser, otherReadAt);

        UUID savedUserId = savedUser.getId();
        UUID savedOtherUserId = savedOtherUser.getId();
        UUID savedTargetChannelId = savedTargetChannel.getId();
        UUID savedOtherChannelId = savedOtherChannel.getId();
        UUID savedReadStatusId = savedReadStatus.getId();
        UUID savedOtherReadStatusId = savedOtherReadStatus.getId();

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // id가 null이거나 대상/대조군 row가 서로 구분되지 않으면 exists 결과의 원인을 명확히 판단하기 어렵다.
        assertThat(savedUserId).isNotNull();
        assertThat(savedOtherUserId).isNotNull();
        assertThat(savedTargetChannelId).isNotNull();
        assertThat(savedOtherChannelId).isNotNull();
        assertThat(savedReadStatusId).isNotNull();
        assertThat(savedOtherReadStatusId).isNotNull();
        assertThat(savedOtherUserId).isNotEqualTo(savedUserId);
        assertThat(savedOtherChannelId).isNotEqualTo(savedTargetChannelId);
        assertThat(savedOtherReadStatusId).isNotEqualTo(savedReadStatusId);

        // 저장 직후 ReadStatus의 user/channel 연결을 확인한다.
        // 여기서 fixture가 어긋나면 existsByUser_Id(...) 검증 실패 원인을 Repository 문제가 아니라 given 구성 오류로 봐야 한다.
        assertThat(savedReadStatus.getChannelId()).isEqualTo(savedTargetChannelId);
        assertThat(savedReadStatus.getUserId()).isEqualTo(savedUserId);
        assertThat(savedOtherReadStatus.getChannelId()).isEqualTo(savedOtherChannelId);
        assertThat(savedOtherReadStatus.getUserId()).isEqualTo(savedOtherUserId);

        // 영속성 컨텍스트를 비워, 아래 사전 조회와 exists 조회가 DB에 실제 반영된 row를 기준으로 수행되게 한다.
        em.clear();

        // exists 조회 전에 savedUser에는 ReadStatus가 실제로 존재한다는 fixture 상태를 확인한다.
        // 이 검증이 있어야 true 결과가 "테이블에 다른 사용자의 row가 있어서" 우연히 나온 것이 아님을 알 수 있다.
        assertThat(readStatusRepository.findByUserId(savedUserId))
                .hasSize(1)
                .singleElement()
                .extracting(ReadStatus::getId, ReadStatus::getUserId, ReadStatus::getChannelId)
                .containsExactly(savedReadStatusId, savedUserId, savedTargetChannelId);

        // 대조군 사용자도 ReadStatus를 갖고 있지만, savedUserId 조회와는 별개의 row여야 한다.
        assertThat(readStatusRepository.findByUserId(savedOtherUserId))
                .hasSize(1)
                .singleElement()
                .extracting(ReadStatus::getId, ReadStatus::getUserId, ReadStatus::getChannelId)
                .containsExactly(savedOtherReadStatusId, savedOtherUserId, savedOtherChannelId);

        // when
        // ReadStatus가 연결된 savedUser id로 존재 여부를 조회한다.
        boolean exists = readStatusRepository.existsByUser_Id(savedUserId);

        // then
        // savedUser에는 ReadStatus가 하나 이상 존재하므로 true를 반환해야 한다.
        assertThat(exists).isTrue();

        // 전체 ReadStatus 수는 대상 사용자 1건 + 대조군 사용자 1건으로 총 2건이어야 한다.
        // existsByUser_Id(...)가 조회 과정에서 데이터를 변경하지 않는다는 점도 보조적으로 확인한다.
        assertThat(readStatusRepository.count()).isEqualTo(2);

    }

    @Test
    @DisplayName("사용자 기준 읽음 상태 존재 여부 조회 성공 - 사용자의 읽음 상태가 없으면 false 반환")
    void existsByUserId_returnsFalse_whenUserHasNoReadStatuses() {
        // given
        // 이 테스트의 대상은 ReadStatusRepository.existsByUser_Id(...) derived query다.
        // 특정 userId에 연결된 ReadStatus가 없으면 false를 반환해야 한다.
        // 여기서는 "사용자 자체가 없는 UUID"가 아니라,
        // "사용자 row는 존재하지만 read_statuses row가 없는 사용자"를 조회 대상으로 둔다.
        // 그래야 existsByUser_Id(...)가 User 존재 여부가 아니라 ReadStatus.user_id 존재 여부를 기준으로 판단하는지 확인할 수 있다.
        UserCreateCommand userWithReadStatusCreateCommand = userCreateCommand();
        UserCreateCommand otherUserCreateCommand = userCreateCommand(
                "otherUser",
                "otherPassword",
                "other@gmail.com"
        );
        UserCreateCommand userWithoutReadStatusCreateCommand = userCreateCommand(
                "userWithoutReadStatus",
                "userWithoutReadStatusPassword",
                "userWithoutReadStatus@gmail.com"
        );
        ChannelCreatePublicCommand targetChannelCreateCommand = channelCreatePublicCommand();
        ChannelCreatePublicCommand otherChannelCreateCommand = channelCreatePublicCommand(
                "otherChannel",
                "otherChannelDescription",
                ChannelType.PUBLIC
        );
        Instant readAt = Instant.now();
        Instant otherReadAt = readAt.plusSeconds(1);

        // ReadStatus를 가진 사용자 2명과, ReadStatus가 없는 조회 대상 사용자 1명을 저장한다.
        // 조회 대상 사용자도 users 테이블에는 실제로 존재해야 false 결과가 "사용자 부재"가 아니라
        // "읽음 상태 부재" 때문이라는 점을 검증할 수 있다.
        User savedUser = saveUser(userWithReadStatusCreateCommand);
        User savedOtherUser = saveUser(otherUserCreateCommand);
        User savedUserWithoutReadStatus = saveUser(userWithoutReadStatusCreateCommand);

        // 다만 실제 사용자 생성 흐름과 유사한 fixture를 유지하고,

        // ReadStatus를 저장할 서로 다른 채널 2개를 준비한다.
        // 조회 대상 사용자(savedUserWithoutReadStatus)에는 어떤 채널의 ReadStatus도 연결하지 않는다.
        Channel savedTargetChannel = saveChannel(targetChannelCreateCommand);
        Channel savedOtherChannel = saveChannel(otherChannelCreateCommand);

        // savedUser와 savedOtherUser에는 ReadStatus를 각각 1건씩 저장한다.
        // 이 row들은 DB에 ReadStatus가 실제로 존재하는 상황에서도,
        // 조회 대상 userId와 매칭되는 row가 없으면 false를 반환해야 한다는 대조군이다.
        ReadStatus savedReadStatus = saveReadStatus(savedTargetChannel, savedUser, readAt);

        ReadStatus savedOtherReadStatus = saveReadStatus(savedOtherChannel, savedOtherUser, otherReadAt);

        UUID savedUserId = savedUser.getId();
        UUID savedOtherUserId = savedOtherUser.getId();
        UUID savedUserWithoutReadStatusId = savedUserWithoutReadStatus.getId();
        UUID savedTargetChannelId = savedTargetChannel.getId();
        UUID savedOtherChannelId = savedOtherChannel.getId();
        UUID savedReadStatusId = savedReadStatus.getId();
        UUID savedOtherReadStatusId = savedOtherReadStatus.getId();

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // id가 null이거나 조회 대상 사용자가 기존 ReadStatus 사용자들과 구분되지 않으면,
        // false 결과가 Repository 조건 때문인지 given 구성 오류인지 구분하기 어렵다.
        assertThat(savedUserId).isNotNull();
        assertThat(savedOtherUserId).isNotNull();
        assertThat(savedUserWithoutReadStatusId).isNotNull();
        assertThat(savedTargetChannelId).isNotNull();
        assertThat(savedOtherChannelId).isNotNull();
        assertThat(savedReadStatusId).isNotNull();
        assertThat(savedOtherReadStatusId).isNotNull();
        assertThat(savedOtherUserId).isNotEqualTo(savedUserId);
        assertThat(savedOtherChannelId).isNotEqualTo(savedTargetChannelId);
        assertThat(savedOtherReadStatusId).isNotEqualTo(savedReadStatusId);
        assertThat(savedUserWithoutReadStatusId).isNotIn(savedUserId, savedOtherUserId);

        // 저장 직후 ReadStatus의 user/channel 연결을 확인한다.
        // 여기서 fixture가 어긋나면 existsByUser_Id(...) 검증 실패 원인을 Repository 문제가 아니라 given 구성 오류로 봐야 한다.
        assertThat(savedReadStatus.getChannelId()).isEqualTo(savedTargetChannelId);
        assertThat(savedReadStatus.getUserId()).isEqualTo(savedUserId);
        assertThat(savedOtherReadStatus.getChannelId()).isEqualTo(savedOtherChannelId);
        assertThat(savedOtherReadStatus.getUserId()).isEqualTo(savedOtherUserId);

        // 영속성 컨텍스트를 비워, 아래 사전 조회와 exists 조회가 DB에 실제 반영된 row를 기준으로 수행되게 한다.
        em.clear();

        // exists 조회 전에 기존 두 사용자에게는 ReadStatus가 실제로 존재한다는 점을 확인한다.
        // 이 검증이 있어야 false 결과가 "read_statuses 테이블이 비어서" 나온 것이 아님을 알 수 있다.
        assertThat(readStatusRepository.findByUserId(savedUserId))
                .hasSize(1)
                .singleElement()
                .extracting(ReadStatus::getId, ReadStatus::getUserId, ReadStatus::getChannelId)
                .containsExactly(savedReadStatusId, savedUserId, savedTargetChannelId);

        assertThat(readStatusRepository.findByUserId(savedOtherUserId))
                .hasSize(1)
                .singleElement()
                .extracting(ReadStatus::getId, ReadStatus::getUserId, ReadStatus::getChannelId)
                .containsExactly(savedOtherReadStatusId, savedOtherUserId, savedOtherChannelId);

        // 조회 대상 사용자는 users 테이블에 실제로 존재하지만 ReadStatus는 하나도 없어야 한다.
        // 이 사전 검증이 있어야 existsByUser_Id(...)의 false 결과가 "읽음 상태 부재" 때문이라는 점이 분명해진다.
        assertThat(readStatusRepository.findByUserId(savedUserWithoutReadStatusId)).isEmpty();

        // when
        // 사용자 row는 존재하지만 ReadStatus가 하나도 연결되지 않은 userId로 존재 여부를 조회한다.
        boolean exists = readStatusRepository.existsByUser_Id(savedUserWithoutReadStatusId);

        // then
        // savedUserWithoutReadStatus에는 read_statuses.user_id와 매칭되는 row가 없으므로 false를 반환해야 한다.
        assertThat(exists).isFalse();

        // 전체 ReadStatus 수는 기존 두 사용자에게 만든 2건뿐이어야 한다.
        // existsByUser_Id(...)가 조회 과정에서 데이터를 변경하지 않는다는 점도 보조적으로 확인한다.
        assertThat(readStatusRepository.count()).isEqualTo(2);

    }

    @Test
    @DisplayName("사용자별 읽음 상태 삭제 성공 - 사용자에 연결된 읽음 상태 삭제")
    void deleteByUserId_deletesOnlyUserReadStatuses_whenUserHasReadStatuses() {
        // given
        // 이 테스트의 대상은 ReadStatusRepository.deleteByUser_Id(...) derived delete query다.
        // 특정 userId에 연결된 ReadStatus를 삭제하되,
        // 다른 사용자의 ReadStatus는 같은 채널에 있더라도 삭제되면 안 된다.
        // 따라서 삭제 대상 사용자(savedUser)에게는 서로 다른 채널의 ReadStatus 2건을 만들고,
        // 대조군 사용자(savedOtherUser)에게는 targetChannel의 ReadStatus 1건을 만들어 둔다.
        // 이렇게 하면 deleteByUser_Id(...)가 user_id 조건으로만 삭제되는지 확인할 수 있다.
        UserCreateCommand userWithReadStatusCreateCommand = userCreateCommand();
        UserCreateCommand otherUserCreateCommand = userCreateCommand(
                "otherUser",
                "otherPassword",
                "other@gmail.com"
        );

        ChannelCreatePublicCommand targetChannelCreateCommand = channelCreatePublicCommand();
        ChannelCreatePublicCommand otherChannelCreateCommand = channelCreatePublicCommand(
                "otherChannel",
                "otherChannelDescription",
                ChannelType.PUBLIC
        );
        Instant readAt = Instant.now();
        Instant secondReadAt = readAt.plusSeconds(1);
        Instant remainingReadAt = readAt.plusSeconds(2);

        // 삭제 대상 사용자와 대조군 사용자를 저장한다.
        // deleteByUser_Id(savedUserId)는 savedUser의 ReadStatus만 삭제해야 한다.
        User savedUser = saveUser(userWithReadStatusCreateCommand);
        User savedOtherUser = saveUser(otherUserCreateCommand);

        // 다만 실제 사용자 생성 흐름과 유사한 fixture를 유지하기 위해 실제 row로 구성한다.

        // 삭제 대상 사용자의 ReadStatus를 여러 채널에 만들기 위해 채널 2개를 저장한다.
        // 대조군 사용자도 targetChannel에 ReadStatus를 갖게 해서, 같은 channel_id라도 user_id가 다르면 삭제되지 않아야 함을 확인한다.
        Channel savedTargetChannel = saveChannel(targetChannelCreateCommand);
        Channel savedOtherChannel = saveChannel(otherChannelCreateCommand);

        // 삭제 대상 사용자(savedUser)의 첫 번째 ReadStatus다.
        ReadStatus savedTargetReadStatus = saveReadStatus(savedTargetChannel, savedUser, readAt);

        // 삭제 대상 사용자(savedUser)의 두 번째 ReadStatus다.
        // 한 건만 저장하면 delete가 한 row만 지워도 테스트가 통과하므로, 복수 row 삭제를 확인하기 위해 추가한다.
        ReadStatus savedOtherChannelReadStatus = saveReadStatus(savedOtherChannel, savedUser, secondReadAt);

        // 대조군 사용자(savedOtherUser)의 ReadStatus다.
        // targetChannel에 함께 존재하지만 user_id가 다르므로 deleteByUser_Id(savedUserId) 이후에도 남아 있어야 한다.
        ReadStatus savedRemainingReadStatus = saveReadStatus(savedTargetChannel, savedOtherUser, remainingReadAt);

        UUID savedUserId = savedUser.getId();
        UUID savedOtherUserId = savedOtherUser.getId();
        UUID savedTargetChannelId = savedTargetChannel.getId();
        UUID savedOtherChannelId = savedOtherChannel.getId();
        UUID savedTargetReadStatusId = savedTargetReadStatus.getId();
        UUID savedOtherChannelReadStatusId = savedOtherChannelReadStatus.getId();
        UUID savedRemainingReadStatusId = savedRemainingReadStatus.getId();

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // id가 null이거나 사용자/채널/readStatus가 서로 구분되지 않으면 삭제 조건 검증이 의미 없어질 수 있다.
        assertThat(savedUserId).isNotNull();
        assertThat(savedOtherUserId).isNotNull();
        assertThat(savedTargetChannelId).isNotNull();
        assertThat(savedOtherChannelId).isNotNull();
        assertThat(savedTargetReadStatusId).isNotNull();
        assertThat(savedOtherChannelReadStatusId).isNotNull();
        assertThat(savedRemainingReadStatusId).isNotNull();
        assertThat(savedOtherUserId).isNotEqualTo(savedUserId);
        assertThat(savedOtherChannelId).isNotEqualTo(savedTargetChannelId);
        assertThat(savedOtherChannelReadStatusId).isNotEqualTo(savedTargetReadStatusId);
        assertThat(savedRemainingReadStatusId).isNotIn(savedTargetReadStatusId, savedOtherChannelReadStatusId);

        // 저장 직후 ReadStatus의 user/channel 연결을 확인한다.
        // 여기서 fixture가 어긋나면 삭제 검증 실패 원인을 Repository 문제가 아니라 given 구성 오류로 봐야 한다.
        assertThat(savedTargetReadStatus.getChannelId()).isEqualTo(savedTargetChannelId);
        assertThat(savedTargetReadStatus.getUserId()).isEqualTo(savedUserId);
        assertThat(savedOtherChannelReadStatus.getChannelId()).isEqualTo(savedOtherChannelId);
        assertThat(savedOtherChannelReadStatus.getUserId()).isEqualTo(savedUserId);
        assertThat(savedRemainingReadStatus.getChannelId()).isEqualTo(savedTargetChannelId);
        assertThat(savedRemainingReadStatus.getUserId()).isEqualTo(savedOtherUserId);

        // 영속성 컨텍스트를 비워, 아래 사전 조회와 삭제 검증이 DB의 실제 row를 기준으로 수행되게 한다.
        em.clear();

        // 삭제 전에 대상 사용자에게 ReadStatus 2건이 실제로 존재해야 한다.
        // 이 사전 검증이 있어야 삭제 후 빈 목록이 "원래 없어서 빈 목록"인 경우와 구분된다.
        assertThat(readStatusRepository.findByUserId(savedUserId))
                .hasSize(2)
                .extracting(ReadStatus::getId, ReadStatus::getUserId, ReadStatus::getChannelId)
                .containsExactlyInAnyOrder(
                        tuple(savedTargetReadStatusId, savedUserId, savedTargetChannelId),
                        tuple(savedOtherChannelReadStatusId, savedUserId, savedOtherChannelId)
                );

        // 대조군 사용자에게도 ReadStatus가 실제로 존재해야 한다.
        // 이 row가 삭제 후에도 남아 있어야 deleteByUser_Id(...)가 user_id 조건만 삭제했다는 점을 확인할 수 있다.
        assertThat(readStatusRepository.findByUserId(savedOtherUserId))
                .hasSize(1)
                .singleElement()
                .extracting(ReadStatus::getId, ReadStatus::getUserId, ReadStatus::getChannelId)
                .containsExactly(savedRemainingReadStatusId, savedOtherUserId, savedTargetChannelId);

        assertThat(readStatusRepository.count()).isEqualTo(3);

        // when
        // 삭제 대상 사용자 id로 ReadStatus를 삭제한다.
        // deleteByUser_Id(...)는 User 자체가 아니라 read_statuses 테이블의 user_id가 일치하는 row들을 삭제해야 한다.
        readStatusRepository.deleteByUser_Id(savedUserId);

        // delete SQL을 DB에 즉시 반영하고, 남은 영속 객체가 결과 검증에 영향을 주지 않도록 비운다.
        em.flush();
        em.clear();

        // then
        // 삭제 대상 사용자에 연결되어 있던 ReadStatus는 모두 삭제되어야 한다.
        assertThat(readStatusRepository.findByUserId(savedUserId)).isEmpty();

        // 반면 다른 사용자의 ReadStatus는 삭제 조건에 해당하지 않으므로 그대로 남아 있어야 한다.
        // 이 검증이 없으면 deleteByUser_Id(...)가 실수로 더 넓은 범위의 row를 삭제해도 놓칠 수 있다.
        assertThat(readStatusRepository.findByUserId(savedOtherUserId))
                .hasSize(1)
                .singleElement()
                .extracting(ReadStatus::getId, ReadStatus::getUserId, ReadStatus::getChannelId)
                .containsExactly(savedRemainingReadStatusId, savedOtherUserId, savedTargetChannelId);

        // targetChannel에는 대조군 사용자의 ReadStatus 1건만 남아 있어야 한다.
        // 같은 채널의 target user row는 삭제되고, other user row만 보존됐다는 점을 추가로 확인한다.
        assertThat(readStatusRepository.findByChannelId(savedTargetChannelId))
                .hasSize(1)
                .singleElement()
                .extracting(ReadStatus::getId, ReadStatus::getUserId, ReadStatus::getChannelId)
                .containsExactly(savedRemainingReadStatusId, savedOtherUserId, savedTargetChannelId);

        // 전체 ReadStatus 개수도 보조적으로 확인한다.
        // given에서 3개를 만들고 대상 사용자의 2개만 삭제했으므로, 최종적으로 1개만 남아야 한다.
        assertThat(readStatusRepository.count()).isEqualTo(1);
    }

    private UserCreateCommand userCreateCommand() {
        return userCreateCommand(
                "testUser",
                "testPassword",
                "test@gmail.com"
        );
    }

    private UserCreateCommand userCreateCommand(String username, String password, String email) {
        return new UserCreateCommand(username, password, email);
    }

    private ChannelCreatePublicCommand channelCreatePublicCommand() {
        return channelCreatePublicCommand(
                "testChannel",
                "testChannelDescription",
                ChannelType.PUBLIC
        );
    }

    private ChannelCreatePublicCommand channelCreatePublicCommand(
            String channelName,
            String channelDescription
    ) {
        return channelCreatePublicCommand(channelName, channelDescription, ChannelType.PUBLIC);
    }

    private ChannelCreatePublicCommand channelCreatePublicCommand(
            String channelName,
            String channelDescription,
            ChannelType channelType
    ) {
        return new ChannelCreatePublicCommand(channelName, channelDescription, channelType);
    }

    private User saveUser(UserCreateCommand command) {
        return userRepository.saveAndFlush(new User(command, null));
    }

    private User saveUserWithProfile(UserCreateCommand command, String originalFileName) {
        BinaryContent savedProfile = binaryContentRepository.saveAndFlush(
                new BinaryContent(originalFileName, "originFileContent", 1_000L)
        );

        return userRepository.saveAndFlush(new User(command, savedProfile));
    }

    private Channel saveChannel(ChannelCreatePublicCommand command) {
        return channelRepository.saveAndFlush(new Channel(command));
    }

    private ReadStatus saveReadStatus(Channel channel, User user, Instant readAt) {
        return readStatusRepository.saveAndFlush(
                new ReadStatus(channel, user, new ReadStatusCreateCommand(user.getId(), readAt))
        );
    }

    private PersistenceUnitUtil getPersistenceUnitUtil() {
        return em.getEntityManagerFactory().getPersistenceUnitUtil();
    }
}
