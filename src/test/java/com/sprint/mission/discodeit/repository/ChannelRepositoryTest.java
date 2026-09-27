package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.config.JpaAuditingTestConfig;
import com.sprint.mission.discodeit.config.P6SpySqlFormatter;
import com.sprint.mission.discodeit.config.QuerydslTestConfig;
import com.sprint.mission.discodeit.dto.command.channel.ChannelCreatePrivateCommand;
import com.sprint.mission.discodeit.dto.command.channel.ChannelCreatePublicCommand;
import com.sprint.mission.discodeit.dto.command.message.MessageCreateCommand;
import com.sprint.mission.discodeit.dto.command.readStatus.ReadStatusCreateCommand;
import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import com.sprint.mission.discodeit.dto.repository.ChannelSummary;
import com.sprint.mission.discodeit.entity.*;
import jakarta.persistence.EntityManager;
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
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest(showSql = false)
@Import(value = {QuerydslTestConfig.class, JpaAuditingTestConfig.class, P6SpySqlFormatter.class})
@DisplayName("ChannelRepository 슬라이스 테스트")
class ChannelRepositoryTest {

    @Autowired
    ChannelRepository channelRepository;

    @Autowired
    ReadStatusRepository readStatusRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("여러 채널이 있어도 지정한 공개 채널은 참여 기록 없이 접근할 수 있다")
    void isChannelAccessible_allowsPublicChannelWithoutMembership() {
        User requester = saveUser("accessRequester");
        Channel target = savePublicChannel().channel();
        savePublicChannel();
        Channel participating = savePrivateChannelFor(requester).channel();
        joinChannel(participating, requester);
        em.flush();
        em.clear();

        assertThat(channelRepository.isChannelAccessible(target.getId(), requester.getId())).isTrue();
    }

    @Test
    @DisplayName("비공개 채널은 대상 채널의 참여 기록으로만 접근을 판단한다")
    void isChannelAccessible_requiresMembershipInTheRequestedPrivateChannel() {
        User requester = saveUser("accessRequester");
        User other = saveUser("accessOther");
        Channel participating = savePrivateChannelFor(requester).channel();
        joinChannel(participating, requester);
        Channel target = savePrivateChannelFor(other).channel();
        joinChannel(target, other);
        savePublicChannel();
        em.flush();
        em.clear();

        assertThat(channelRepository.isChannelAccessible(participating.getId(), requester.getId())).isTrue();
        assertThat(channelRepository.isChannelAccessible(target.getId(), requester.getId())).isFalse();
        assertThat(channelRepository.isChannelAccessible(target.getId(), other.getId())).isTrue();
        assertThat(channelRepository.isChannelAccessible(UUID.randomUUID(), requester.getId())).isFalse();
    }

    @Test
    @DisplayName("채널이 없으면 접근 결과는 false다")
    void isChannelAccessible_returnsFalseWhenNoChannelsExist() {
        assertThat(channelRepository.isChannelAccessible(UUID.randomUUID(), UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("사용자별 채널 목록 조회 성공 - 공개 채널과 참여 중인 비공개 채널 반환")
    void findVisibleChannels_returnsPublicAndParticipatingPrivateChannels_whenUserExists() {
        // given
        // 이 테스트의 대상은 ChannelRepository.findVisibleChannels(...) JPQL 쿼리다.
        // Repository 슬라이스 테스트이므로 Mock을 사용하지 않고 실제 User, Channel, ReadStatus 엔티티를
        // H2 테스트 DB에 저장한 뒤, 쿼리가 사용자별로 보여야 하는 채널만 반환하는지 검증한다.
        //
        // findVisibleChannels(...)의 가시성 규칙은 두 가지다.
        // 1. PUBLIC 채널은 ReadStatus 참여 여부와 관계없이 반환한다.
        // 2. PRIVATE 채널은 해당 userId의 ReadStatus가 있는, 즉 참여 중인 채널만 반환한다.
        // 조회 대상 사용자와 비교 대상 사용자를 모두 실제 DB에 저장한다.
        // 비교 대상 사용자는 "다른 사용자만 참여한 PRIVATE 채널"을 만들기 위한 데이터다.
        User user = saveUser("testUser");
        User otherUser = saveUser("otherUser");

        // PUBLIC 채널은 ReadStatus 없이도 조회되어야 한다.
        // 이 구성이 있어야 publicType 조건(c.type = PUBLIC)이 실제로 적용되는지 확인할 수 있다.
        PublicChannelFixture publicChannelFixture = savePublicChannel();
        ChannelCreatePublicCommand createPublicCommand = publicChannelFixture.command();
        Channel publicChannel = publicChannelFixture.channel();

        // 조회 대상 사용자가 참여한 PRIVATE 채널이다.
        // findVisibleChannels(...)는 exists(ReadStatus) 조건으로 이 채널을 포함해야 한다.
        PrivateChannelFixture participatingPrivateChannelFixture = savePrivateChannelFor(user);
        ChannelCreatePrivateCommand createParticipatingPrivateCommand = participatingPrivateChannelFixture.command();
        Channel participatingPrivateChannel = participatingPrivateChannelFixture.channel();

        // 다른 사용자만 참여한 PRIVATE 채널이다.
        // 이 채널이 결과에 포함되면 userId 기반 exists 조건이 제대로 동작하지 않는 것이다.
        PrivateChannelFixture otherPrivateChannelFixture = savePrivateChannelFor(otherUser);
        Channel otherPrivateChannel = otherPrivateChannelFixture.channel();

        // PRIVATE 채널 참여 여부는 ReadStatus row로 표현된다.
        // PUBLIC 채널에는 일부러 ReadStatus를 만들지 않아도 조회되는지 확인한다.
        ReadStatus participatingPrivateReadStatus = joinChannel(participatingPrivateChannel, user);
        ReadStatus otherPrivateReadStatus = joinChannel(otherPrivateChannel, otherUser);

        UUID userId = user.getId();
        UUID otherUserId = otherUser.getId();
        UUID publicChannelId = publicChannel.getId();
        UUID participatingPrivateChannelId = participatingPrivateChannel.getId();
        UUID otherPrivateChannelId = otherPrivateChannel.getId();
        UUID participatingPrivateReadStatusId = participatingPrivateReadStatus.getId();
        UUID otherPrivateReadStatusId = otherPrivateReadStatus.getId();

        // em.clear() 이후에는 저장 직후 영속성 컨텍스트에 남아 있는 객체가 반환될 수 없다.
        // 아래 when 절의 결과는 실제 JPQL SELECT 결과라는 점이 분명해진다.
        em.clear();

        // 사전 조건을 먼저 확인한다.
        // 조회 대상 사용자와 비교 대상 사용자가 서로 달라야 하고,
        // 세 채널도 서로 다른 row여야 포함/제외 검증이 의미를 가진다.
        assertThat(userId).isNotNull();
        assertThat(otherUserId)
                .isNotNull()
                .isNotEqualTo(userId);
        assertThat(publicChannelId).isNotNull();
        assertThat(participatingPrivateChannelId)
                .isNotNull()
                .isNotEqualTo(publicChannelId);
        assertThat(otherPrivateChannelId)
                .isNotNull()
                .isNotIn(publicChannelId, participatingPrivateChannelId);
        assertThat(participatingPrivateReadStatusId).isNotNull();
        assertThat(otherPrivateReadStatusId).isNotNull();

        // when
        // 조회 대상 사용자 기준으로 visible channel 목록을 조회한다.
        // 두 번째 인자는 PUBLIC 채널을 항상 포함하기 위한 publicType 조건 값이다.
        List<ChannelSummary> visibleChannels = channelRepository.findVisibleChannels(userId, ChannelType.PUBLIC);

        // then
        // 결과에는 PUBLIC 채널과 조회 대상 사용자가 참여한 PRIVATE 채널만 있어야 한다.
        // 다른 사용자만 참여한 PRIVATE 채널은 DB에 실제로 존재하지만 결과에서 제외되어야 한다.
        //
        // findVisibleChannels(...)는 ChannelSummary projection을 반환하므로,
        // Channel 엔티티가 아니라 projection 필드를 기준으로 검증한다.
        // 쿼리에 order by가 없으므로 반환 순서를 고정하지 않고 순서 무관 비교를 사용한다.
        assertThat(visibleChannels)
                .hasSize(2)
                .extracting(
                        ChannelSummary::id,
                        ChannelSummary::type,
                        ChannelSummary::name,
                        ChannelSummary::description,
                        ChannelSummary::lastMessageAt
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                publicChannelId,
                                ChannelType.PUBLIC,
                                createPublicCommand.channelName(),
                                createPublicCommand.channelDescription(),
                                null
                        ),
                        tuple(
                                participatingPrivateChannelId,
                                ChannelType.PRIVATE,
                                createParticipatingPrivateCommand.channelName(),
                                createParticipatingPrivateCommand.channelDescription(),
                                null
                        )
                );

        // 제외 검증을 따로 두면 실패했을 때 원인을 더 빨리 볼 수 있다.
        // 이 assertion은 PRIVATE 채널 참여 조건이 userId별로 적용되는지 직접 보여준다.
        assertThat(visibleChannels)
                .extracting(ChannelSummary::id)
                .doesNotContain(otherPrivateChannelId);
    }

    @Test
    @DisplayName("사용자별 채널 목록 조회 성공 - 참여하지 않은 비공개 채널 제외")
    void findVisibleChannels_excludesPrivateChannelsWithoutReadStatus_whenUserIsNotParticipant() {

        // user는 PRIVATE 채널에 참여한 사용자다.
        // otherUser는 이번 조회 대상 사용자이며, PRIVATE 채널에 참여하지 않는다.
        User user = saveUser("testUser");
        User otherUser = saveUser("otherUser");

        // PUBLIC 채널은 조회 대상 사용자의 참여 여부와 관계없이 반환되어야 한다.
        // 이 채널까지 제외되면 publicType 조건(c.type = PUBLIC)이 깨진 것이다.
        PublicChannelFixture publicChannelFixture = savePublicChannel();
        ChannelCreatePublicCommand createPublicCommand = publicChannelFixture.command();
        Channel publicChannel = publicChannelFixture.channel();

        // PRIVATE 채널은 실제로 존재하지만 조회 대상 사용자가 참여하지 않은 채널이다.
        // ChannelCreatePrivateCommand의 participantIds는 command 값일 뿐이고,
        // 현재 쿼리에서 실제 참여 여부는 ReadStatus row 존재 여부로 판단한다.
        PrivateChannelFixture participatingPrivateChannelFixture = savePrivateChannelFor(user);
        Channel participatingPrivateChannel = participatingPrivateChannelFixture.channel();

        // PRIVATE 채널 참여 상태를 user에게만 만든다.
        // otherUser에게는 같은 채널의 ReadStatus를 만들지 않는다.
        // 따라서 otherUser 기준 findVisibleChannels(...) 결과에는 이 PRIVATE 채널이 포함되면 안 된다.
        ReadStatus participatingPrivateReadStatus = joinChannel(participatingPrivateChannel, user);

        UUID userId = user.getId();
        UUID otherUserId = otherUser.getId();
        UUID publicChannelId = publicChannel.getId();
        UUID participatingPrivateChannelId = participatingPrivateChannel.getId();
        UUID participatingPrivateReadStatusId = participatingPrivateReadStatus.getId();
        UUID participatingPrivateReadStatusUserId = participatingPrivateReadStatus.getUserId();
        UUID participatingPrivateReadStatusChannelId = participatingPrivateReadStatus.getChannelId();

        // em.clear() 이후에는 저장 직후 영속성 컨텍스트에 남아 있는 객체가 결과에 영향을 주지 않는다.
        // 아래 when 절의 결과는 실제 JPQL SELECT 결과라는 점이 분명해진다.
        em.clear();

        // 사전 조건을 먼저 확인한다.
        // 조회 대상 사용자와 PRIVATE 채널 참여자는 서로 달라야 한다.
        assertThat(userId).isNotNull();
        assertThat(otherUserId)
                .isNotNull()
                .isNotEqualTo(userId);

        // PUBLIC 채널과 PRIVATE 채널도 서로 다른 실제 row여야 한다.
        // 그래야 PUBLIC 포함과 PRIVATE 제외를 분리해서 검증할 수 있다.
        assertThat(publicChannelId).isNotNull();
        assertThat(participatingPrivateChannelId)
                .isNotNull()
                .isNotEqualTo(publicChannelId);

        // PRIVATE 채널의 ReadStatus가 조회 대상 otherUser가 아니라 user에게 연결되어 있음을 확인한다.
        // 이 전제가 맞아야 "참여하지 않은 PRIVATE 채널 제외" 테스트가 의미를 가진다.
        assertThat(participatingPrivateReadStatusId).isNotNull();
        assertThat(participatingPrivateReadStatusUserId).isEqualTo(userId);
        assertThat(participatingPrivateReadStatusUserId).isNotEqualTo(otherUserId);
        assertThat(participatingPrivateReadStatusChannelId).isEqualTo(participatingPrivateChannelId);

        // when
        // PRIVATE 채널에 참여하지 않은 otherUser 기준으로 visible channel 목록을 조회한다.
        // PUBLIC 채널은 포함되어야 하지만, user만 참여한 PRIVATE 채널은 제외되어야 한다.
        List<ChannelSummary> visibleChannels = channelRepository.findVisibleChannels(otherUserId, ChannelType.PUBLIC);

        // then
        // 결과에는 PUBLIC 채널 하나만 있어야 한다.
        // ChannelSummary projection을 반환하므로 엔티티가 아니라 projection 필드를 기준으로 검증한다.
        // 쿼리에 order by가 없지만 결과가 하나뿐이므로 containsExactlyInAnyOrder로 값 자체만 확인한다.
        assertThat(visibleChannels)
                .hasSize(1)
                .extracting(
                        ChannelSummary::id,
                        ChannelSummary::type,
                        ChannelSummary::name,
                        ChannelSummary::description,
                        ChannelSummary::lastMessageAt
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                publicChannelId,
                                ChannelType.PUBLIC,
                                createPublicCommand.channelName(),
                                createPublicCommand.channelDescription(),
                                null
                        )
                );

        // 제외 조건을 별도 assertion으로 한 번 더 명확히 표현한다.
        // 이 검증은 PRIVATE 채널의 ReadStatus가 존재하더라도,
        // 그 ReadStatus의 userId가 조회 대상 사용자가 아니면 채널이 보이지 않아야 한다는 점을 보여준다.
        assertThat(visibleChannels)
                .extracting(ChannelSummary::id)
                .doesNotContain(participatingPrivateChannelId);
    }

    @Test
    @DisplayName("사용자별 채널 목록 조회 성공 - 참여 비공개 채널이 없으면 공개 채널만 반환")
    void findVisibleChannels_returnsPublicChannelsOnly_whenUserHasNoPrivateChannels() {
        // given
        // 이 테스트는 "조회 대상 사용자의 ReadStatus가 하나도 없는 경우"를 검증한다.
        // findVisibleChannels(...)에서 PRIVATE 채널 참여 여부는 ChannelCreatePrivateCommand의 participantIds가 아니라
        // read_statuses 테이블에 해당 userId + channelId 조합의 row가 존재하는지로 판단된다.
        //
        // 따라서 아래 데이터는 세 가지 상황을 동시에 만든다.
        // 1. PUBLIC 채널: 조회 대상 사용자의 참여 여부와 관계없이 항상 반환되어야 한다.
        // 2. 다른 사용자가 ReadStatus로 참여 중인 PRIVATE 채널: 조회 대상 사용자에게는 보이면 안 된다.
        // 3. 조회 대상 사용자 id가 command에 들어간 PRIVATE 채널: ReadStatus가 없으므로 보이면 안 된다.
        // user는 PRIVATE 채널에 실제로 참여한 사용자다.
        // otherUser는 이번 조회 대상 사용자이며, 어떤 ReadStatus도 갖지 않도록 구성한다.
        User user = saveUser("testUser");
        User otherUser = saveUser("otherUser");

        // PUBLIC 채널은 ReadStatus 없이도 결과에 포함되어야 한다.
        // 이 채널이 빠지면 publicType 조건(c.type = PUBLIC)이 깨진 것이다.
        PublicChannelFixture publicChannelFixture = savePublicChannel();
        ChannelCreatePublicCommand createPublicCommand = publicChannelFixture.command();
        Channel publicChannel = publicChannelFixture.channel();

        // user가 실제로 참여한 PRIVATE 채널이다.
        // 아래에서 user의 ReadStatus를 만들기 때문에 user 기준 조회에는 보이지만,
        // ReadStatus가 없는 otherUser 기준 조회에는 보이면 안 된다.
        PrivateChannelFixture participatingPrivateChannelFixture = savePrivateChannelFor(user);
        Channel participatingPrivateChannel = participatingPrivateChannelFixture.channel();

        // otherUser id가 command에 포함된 PRIVATE 채널이다.
        // 현재 Channel 엔티티 생성자는 participantIds를 별도 참여 row로 저장하지 않는다.
        // findVisibleChannels(...)도 command 값이 아니라 ReadStatus row를 조회하므로,
        // 이 채널에 대한 otherUser의 ReadStatus를 만들지 않으면 결과에서 제외되어야 한다.
        PrivateChannelFixture otherPrivateChannelFixture = savePrivateChannelFor(otherUser);
        Channel otherPrivateChannel = otherPrivateChannelFixture.channel();

        // PRIVATE 채널 참여 여부를 user에게만 부여한다.
        // otherUser의 ReadStatus는 일부러 만들지 않는다.
        ReadStatus participatingPrivateReadStatus = joinChannel(participatingPrivateChannel, user);

        UUID userId = user.getId();
        UUID otherUserId = otherUser.getId();
        UUID publicChannelId = publicChannel.getId();
        UUID participatingPrivateChannelId = participatingPrivateChannel.getId();
        UUID otherPrivateChannelId = otherPrivateChannel.getId();
        UUID participatingPrivateReadStatusId = participatingPrivateReadStatus.getId();
        UUID participatingPrivateReadStatusUserId = participatingPrivateReadStatus.getUserId();
        UUID participatingPrivateReadStatusChannelId = participatingPrivateReadStatus.getChannelId();

        // em.clear() 이후에는 저장 직후 영속성 컨텍스트에 남아 있는 객체가 반환될 수 없다.
        // 아래 when 절의 결과는 실제 JPQL SELECT 결과라는 점이 분명해진다.
        em.clear();

        // 사전 조건을 먼저 고정한다.
        // 조회 대상 otherUser가 실제 참여자인 user와 다르고, otherUser에게 생성한 ReadStatus가 없어야
        // "참여 비공개 채널이 없는 사용자"라는 테스트 조건이 성립한다.
        assertThat(userId).isNotNull();
        assertThat(otherUserId)
                .isNotNull()
                .isNotEqualTo(userId);

        // 세 채널은 모두 실제 DB에 저장된 서로 다른 row여야 한다.
        // 그래야 PUBLIC 포함, user의 PRIVATE 제외, ReadStatus 없는 PRIVATE 제외를 각각 검증할 수 있다.
        assertThat(publicChannelId).isNotNull();
        assertThat(participatingPrivateChannelId)
                .isNotNull()
                .isNotEqualTo(publicChannelId);
        assertThat(otherPrivateChannelId)
                .isNotNull()
                .isNotIn(publicChannelId, participatingPrivateChannelId);

        // 현재 생성된 ReadStatus는 user와 participatingPrivateChannel에만 연결되어 있다.
        // 이 검증은 조회 대상 otherUser가 PRIVATE 채널 참여 row를 갖지 않는다는 전제를 드러낸다.
        assertThat(participatingPrivateReadStatusId).isNotNull();
        assertThat(participatingPrivateReadStatusUserId).isEqualTo(userId);
        assertThat(participatingPrivateReadStatusUserId).isNotEqualTo(otherUserId);
        assertThat(participatingPrivateReadStatusChannelId).isEqualTo(participatingPrivateChannelId);

        // when
        // ReadStatus가 없는 otherUser 기준으로 visible channel 목록을 조회한다.
        // PUBLIC 채널만 반환되고, DB에 존재하는 모든 PRIVATE 채널은 제외되어야 한다.
        List<ChannelSummary> visibleChannels = channelRepository.findVisibleChannels(otherUserId, ChannelType.PUBLIC);

        // then
        // 결과에는 PUBLIC 채널 하나만 있어야 한다.
        // ChannelSummary projection을 반환하므로 엔티티가 아니라 projection 필드를 기준으로 검증한다.
        // 메시지를 만들지 않았으므로 lastMessageAt은 left join + max(m.createdAt) 결과로 null이어야 한다.
        assertThat(visibleChannels)
                .hasSize(1)
                .extracting(
                        ChannelSummary::id,
                        ChannelSummary::type,
                        ChannelSummary::name,
                        ChannelSummary::description,
                        ChannelSummary::lastMessageAt
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                publicChannelId,
                                ChannelType.PUBLIC,
                                createPublicCommand.channelName(),
                                createPublicCommand.channelDescription(),
                                null
                        )
                );

        // 제외 조건은 id 기준으로 별도 검증한다.
        // hasSize(1)과 PUBLIC tuple 검증만으로도 암묵적으로 제외되지만,
        // 이 assertion은 이 테스트가 막으려는 회귀를 직접 표현한다.
        assertThat(visibleChannels)
                .extracting(ChannelSummary::id)
                .doesNotContain(participatingPrivateChannelId, otherPrivateChannelId);
    }

    @Test
    @DisplayName("사용자별 채널 목록 조회 성공 - 마지막 메시지 시간이 최신 메시지 생성 시각으로 계산")
    void findVisibleChannels_setsLastMessageAtToLatestMessageCreatedAt_whenMessagesExist() throws InterruptedException {
        // given
        // 이 테스트의 관심사는 findVisibleChannels(...)의 lastMessageAt 계산이다.
        // 쿼리는 Channel을 Message와 left join 한 뒤 max(m.createdAt)을 ChannelSummary.lastMessageAt으로 반환한다.
        //
        // 따라서 한 채널에 메시지 2개를 저장하고, 더 늦게 생성된 메시지의 createdAt이
        // 해당 채널의 lastMessageAt으로 선택되는지 실제 DB 쿼리 결과로 검증한다.
        // Repository 슬라이스 테스트이므로 User, Channel, ReadStatus, Message는 모두 실제 엔티티로 저장한다.
        // 조회 대상 사용자다.
        // 이 사용자가 참여한 PRIVATE 채널도 함께 반환되도록 ReadStatus를 만들 것이다.
        User user = saveUser("testUser");

        // PUBLIC 채널은 참여 여부와 관계없이 visible channel 결과에 포함된다.
        // 메시지는 이 PUBLIC 채널에만 저장해서 lastMessageAt 계산을 명확히 검증한다.
        PublicChannelFixture publicChannelFixture = savePublicChannel();
        ChannelCreatePublicCommand createPublicCommand = publicChannelFixture.command();
        Channel publicChannel = publicChannelFixture.channel();

        // 조회 대상 사용자가 참여한 PRIVATE 채널이다.
        // 이 채널에는 메시지를 저장하지 않는다.
        // 그래서 같은 조회 결과 안에서 "메시지가 있는 채널은 최신 createdAt", "메시지가 없는 채널은 null"을 함께 확인할 수 있다.
        PrivateChannelFixture participatingPrivateChannelFixture = savePrivateChannelFor(user);
        ChannelCreatePrivateCommand createParticipatingPrivateCommand = participatingPrivateChannelFixture.command();
        Channel participatingPrivateChannel = participatingPrivateChannelFixture.channel();

        // PRIVATE 채널의 visible 여부는 ReadStatus row 존재 여부로 판단된다.
        // 이 row가 없으면 participatingPrivateChannel은 user 기준 결과에 포함되지 않는다.
        ReadStatus participatingPrivateReadStatus = joinChannel(participatingPrivateChannel, user);


        UUID userId = user.getId();
        UUID publicChannelId = publicChannel.getId();
        UUID participatingPrivateChannelId = participatingPrivateChannel.getId();
        UUID participatingPrivateReadStatusId = participatingPrivateReadStatus.getId();
        UUID participatingPrivateReadStatusUserId = participatingPrivateReadStatus.getUserId();
        UUID participatingPrivateReadStatusChannelId = participatingPrivateReadStatus.getChannelId();

        // 사전 조건을 먼저 확인한다.
        // PUBLIC 채널과 PRIVATE 채널이 서로 다른 실제 row여야 각 채널의 lastMessageAt을 분리해 검증할 수 있다.
        assertThat(userId).isNotNull();
        assertThat(publicChannelId).isNotNull();
        assertThat(participatingPrivateChannelId)
                .isNotNull()
                .isNotEqualTo(publicChannelId);

        // ReadStatus가 조회 대상 user와 PRIVATE 채널을 정확히 연결하고 있어야 한다.
        // 이 전제가 맞아야 findVisibleChannels(...) 결과에 PRIVATE 채널이 함께 나타난다.
        assertThat(participatingPrivateReadStatusId).isNotNull();
        assertThat(participatingPrivateReadStatusUserId).isEqualTo(userId);
        assertThat(participatingPrivateReadStatusChannelId).isEqualTo(participatingPrivateChannelId);

        // 같은 PUBLIC 채널에 메시지 2개를 순서대로 저장한다.
        // createdAt은 JPA Auditing이 저장 시점에 채우므로, 두 메시지의 생성 시각이 같아지지 않도록 짧게 대기한다.
        // 이 간격이 있어야 max(m.createdAt)이 두 번째 메시지를 선택했다는 검증이 의미를 가진다.
        Message firstMessage = saveMessage(user, publicChannel, "messageContent1");
        Instant firstMessageCreatedAt = firstMessage.getCreatedAt();

        Thread.sleep(100);

        Message latestMessage = saveMessage(user, publicChannel, "messageContent2");
        Instant latestMessageCreatedAt = latestMessage.getCreatedAt();

        // 메시지 두 건이 모두 같은 PUBLIC 채널에 저장되었고, 두 번째 메시지가 실제로 더 늦게 생성되었는지 확인한다.
        // 이 검증이 실패하면 Repository 쿼리 문제가 아니라 테스트 데이터 구성 문제가 먼저 의심된다.
        assertThat(firstMessage.getId()).isNotNull();
        assertThat(latestMessage.getId())
                .isNotNull()
                .isNotEqualTo(firstMessage.getId());
        assertThat(firstMessage.getChannelId()).isEqualTo(publicChannelId);
        assertThat(latestMessage.getChannelId()).isEqualTo(publicChannelId);
        assertThat(firstMessageCreatedAt).isNotNull();
        assertThat(latestMessageCreatedAt)
                .isNotNull()
                .isAfter(firstMessageCreatedAt);

        // em.clear() 이후에는 영속성 컨텍스트에 남아 있던 Channel, Message 객체가 조회 결과에 영향을 줄 수 없다.
        // 아래 when 절의 visibleChannels는 실제 JPQL SELECT와 group by, max 집계 결과다.
        em.clear();

        // when
        // user 기준으로 visible channel 목록을 조회한다.
        // PUBLIC 채널과 user가 ReadStatus로 참여 중인 PRIVATE 채널이 반환 대상이다.
        List<ChannelSummary> visibleChannels = channelRepository.findVisibleChannels(userId, ChannelType.PUBLIC);

        // then
        // 쿼리에는 order by가 없으므로 반환 순서를 고정하지 않는다.
        // 채널 id와 함께 projection 전체를 비교해야 어느 채널의 lastMessageAt이 어떤 값인지 명확히 검증할 수 있다.
        //
        // 먼저 lastMessageAt을 제외한 projection 기본 필드를 검증한다.
        // lastMessageAt은 DB에서 max(m.createdAt)으로 다시 읽은 값이라 저장 직후 엔티티의 Instant와
        // 나노초 단위까지 정확히 같지 않을 수 있으므로 아래에서 별도로 검증한다.
        assertThat(visibleChannels)
                .hasSize(2)
                .extracting(
                        ChannelSummary::id,
                        ChannelSummary::type,
                        ChannelSummary::name,
                        ChannelSummary::description
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                publicChannelId,
                                ChannelType.PUBLIC,
                                createPublicCommand.channelName(),
                                createPublicCommand.channelDescription()
                        ),
                        tuple(
                                participatingPrivateChannelId,
                                ChannelType.PRIVATE,
                                createParticipatingPrivateCommand.channelName(),
                                createParticipatingPrivateCommand.channelDescription()
                        )
                );

        // PUBLIC 채널은 메시지 2개 중 최신 메시지의 createdAt을 lastMessageAt으로 가져야 한다.
        // H2/JDBC가 timestamp를 저장하고 다시 읽는 과정에서 나노초 일부가 반올림될 수 있으므로
        // "두 번째 메시지 시각에 충분히 가깝고, 첫 번째 메시지보다는 늦다"는 방식으로 검증한다.
        // 두 메시지 사이에 100ms 간격을 둔 이유도 이 허용 범위와 오래된 메시지를 명확히 구분하기 위해서다.
        assertThat(visibleChannels)
                .filteredOn(channel -> channel.id().equals(publicChannelId))
                .singleElement()
                .satisfies(channel -> {
                    assertThat(channel.lastMessageAt()).isNotNull();
                    assertThat(channel.lastMessageAt()).isAfter(firstMessageCreatedAt);
                    assertThat(channel.lastMessageAt())
                            .isAfterOrEqualTo(latestMessageCreatedAt.minusMillis(1))
                            .isBeforeOrEqualTo(latestMessageCreatedAt.plusMillis(1));
                });

        // PRIVATE 채널은 visible 목록에는 포함되지만 메시지가 없으므로 lastMessageAt이 null이어야 한다.
        // 이 검증은 left join 결과에서 메시지가 없는 채널도 누락되지 않고, 집계 결과만 null이 되는지를 보여준다.
        assertThat(visibleChannels)
                .filteredOn(channel -> channel.id().equals(participatingPrivateChannelId))
                .singleElement()
                .extracting(ChannelSummary::lastMessageAt)
                .isNull();
    }

    @Test
    @DisplayName("사용자별 채널 목록 조회 성공 - 메시지가 없으면 마지막 메시지 시간은 null")
    void findVisibleChannels_setsLastMessageAtToNull_whenChannelHasNoMessages() {
        // given
        // 이 테스트의 관심사는 "visible channel에 연결된 Message가 없을 때"의 집계 결과다.
        // findVisibleChannels(...)는 Channel을 기준으로 Message를 left join하고 max(m.createdAt)을 구한다.
        // 따라서 메시지가 없는 채널도 조회 결과에서 빠지면 안 되고, lastMessageAt만 null이어야 한다.
        //
        // 최신 메시지 선택 로직은 findVisibleChannels_setsLastMessageAtToLatestMessageCreatedAt_whenMessagesExist()
        // 테스트에서 검증하므로, 이 테스트에서는 Message를 하나도 저장하지 않는다.
        // 조회 대상 사용자다.
        // PUBLIC 채널은 참여 여부와 관계없이 보이고, PRIVATE 채널은 이 사용자의 ReadStatus가 있어야 보인다.
        User user = saveUser("testUser");

        // 메시지가 없는 PUBLIC 채널이다.
        // left join이 inner join처럼 동작하면 이 채널은 결과에서 누락되므로, null 집계 검증에 필요하다.
        PublicChannelFixture publicChannelFixture = savePublicChannel();
        ChannelCreatePublicCommand createPublicCommand = publicChannelFixture.command();
        Channel publicChannel = publicChannelFixture.channel();

        // 메시지가 없는 PRIVATE 채널이다.
        // 조회 대상 사용자의 visible 목록에 포함시키기 위해 아래에서 ReadStatus를 별도로 저장한다.
        PrivateChannelFixture participatingPrivateChannelFixture = savePrivateChannelFor(user);
        ChannelCreatePrivateCommand createParticipatingPrivateCommand = participatingPrivateChannelFixture.command();
        Channel participatingPrivateChannel = participatingPrivateChannelFixture.channel();

        // PRIVATE 채널 참여 여부는 ReadStatus row로 판단된다.
        // Message가 없어도 ReadStatus가 있으면 visible channel 목록에는 포함되어야 한다.
        ReadStatus participatingPrivateReadStatus = joinChannel(participatingPrivateChannel, user);

        UUID userId = user.getId();
        UUID publicChannelId = publicChannel.getId();
        UUID participatingPrivateChannelId = participatingPrivateChannel.getId();
        UUID participatingPrivateReadStatusId = participatingPrivateReadStatus.getId();
        UUID participatingPrivateReadStatusUserId = participatingPrivateReadStatus.getUserId();
        UUID participatingPrivateReadStatusChannelId = participatingPrivateReadStatus.getChannelId();

        // 사전 조건을 먼저 확인한다.
        // 두 채널이 실제로 서로 다른 row여야 각 채널의 null 집계 결과를 분리해서 검증할 수 있다.
        assertThat(userId).isNotNull();
        assertThat(publicChannelId).isNotNull();
        assertThat(participatingPrivateChannelId)
                .isNotNull()
                .isNotEqualTo(publicChannelId);

        // ReadStatus가 조회 대상 user와 PRIVATE 채널을 정확히 연결하고 있어야 한다.
        // 이 전제가 맞아야 PRIVATE 채널이 메시지 없이도 visible 목록에 포함된다.
        assertThat(participatingPrivateReadStatusId).isNotNull();
        assertThat(participatingPrivateReadStatusUserId).isEqualTo(userId);
        assertThat(participatingPrivateReadStatusChannelId).isEqualTo(participatingPrivateChannelId);

        // 이 테스트에서는 Message를 하나도 만들지 않는다.
        // 이 사전 조건이 깨지면 lastMessageAt null 검증은 테스트 데이터와 맞지 않게 된다.
        assertThat(messageRepository.count()).isZero();

        // em.clear() 이후에는 저장 직후 영속성 컨텍스트의 Channel, ReadStatus 객체가 조회 결과에 영향을 줄 수 없다.
        // 아래 when 절의 visibleChannels는 실제 JPQL SELECT, left join, group by, max 집계 결과다.
        em.clear();

        // when
        // user 기준 visible channel 목록을 조회한다.
        // PUBLIC 채널과 user가 ReadStatus로 참여 중인 PRIVATE 채널이 모두 반환되어야 한다.
        List<ChannelSummary> visibleChannels = channelRepository.findVisibleChannels(userId, ChannelType.PUBLIC);

        // then
        // 결과에는 메시지가 없는 PUBLIC 채널과 메시지가 없는 참여 PRIVATE 채널이 모두 있어야 한다.
        // 쿼리에 order by가 없으므로 반환 순서를 고정하지 않고 순서 무관 비교를 사용한다.
        // 두 채널 모두 연결된 Message가 없으므로 max(m.createdAt)의 결과인 lastMessageAt은 null이어야 한다.
        assertThat(visibleChannels)
                .hasSize(2)
                .extracting(
                        ChannelSummary::id,
                        ChannelSummary::type,
                        ChannelSummary::name,
                        ChannelSummary::description,
                        ChannelSummary::lastMessageAt
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                publicChannelId,
                                ChannelType.PUBLIC,
                                createPublicCommand.channelName(),
                                createPublicCommand.channelDescription(),
                                null
                        ),
                        tuple(
                                participatingPrivateChannelId,
                                ChannelType.PRIVATE,
                                createParticipatingPrivateCommand.channelName(),
                                createParticipatingPrivateCommand.channelDescription(),
                                null
                        )
                );

        // null 검증을 id 기준으로 한 번 더 표현한다.
        // 이 assertion은 두 visible 채널 모두 "반환은 되지만 마지막 메시지 시간만 없다"는 의도를 직접 보여준다.
        assertThat(visibleChannels)
                .extracting(ChannelSummary::lastMessageAt)
                .containsOnlyNulls();
    }

    @Test
    @DisplayName("채널 상세 조회 성공 - 메시지가 있으면 마지막 메시지 시간을 포함한 ChannelSummary 반환")
    void findByDetail_returnsChannelSummaryWithLastMessageAt_whenChannelExistsAndMessagesExist() throws InterruptedException {
        // given
        // 이 테스트의 대상은 ChannelRepository.findByDetail(...) JPQL 쿼리다.
        // findByDetail(...)은 channelId 하나로 채널 상세 projection을 조회하고,
        // Message를 left join 한 뒤 max(m.createdAt)을 ChannelSummary.lastMessageAt으로 반환한다.
        //
        // 따라서 같은 채널에 메시지 2개를 저장한 뒤, 더 늦게 생성된 메시지 시간이
        // lastMessageAt으로 계산되는지 실제 DB 조회 결과로 검증한다.
        // 메시지를 저장하려면 작성자 User가 필요하다.
        // findByDetail(...) 자체는 사용자 가시성 조건을 보지 않지만, Message 엔티티의 author를 채우기 위한 데이터다.
        User user = saveUser("testUser");

        // 상세 조회 대상 채널이다.
        // PUBLIC/PRIVATE 여부와 관계없이 findByDetail(...)은 channelId가 일치하는 채널만 조회한다.
        PublicChannelFixture publicChannelFixture = savePublicChannel();
        ChannelCreatePublicCommand createPublicCommand = publicChannelFixture.command();
        Channel publicChannel = publicChannelFixture.channel();

        UUID userId = user.getId();
        UUID publicChannelId = publicChannel.getId();

        assertThat(userId).isNotNull();
        assertThat(publicChannelId).isNotNull();

        // 같은 채널에 메시지를 2개 저장한다.
        // createdAt은 JPA Auditing이 저장 시점에 채우므로, 두 메시지의 생성 시각이 명확히 달라지도록 짧게 대기한다.
        // 이 데이터가 있어야 max(m.createdAt)이 최신 메시지를 선택했다는 점을 검증할 수 있다.
        Message firstMessage = saveMessage(user, publicChannel, "messageContent1");
        Instant firstMessageCreatedAt = firstMessage.getCreatedAt();

        Thread.sleep(100);

        Message latestMessage = saveMessage(user, publicChannel, "messageContent2");
        Instant latestMessageCreatedAt = latestMessage.getCreatedAt();

        // 사전 조건을 확인한다.
        // 두 메시지가 같은 채널에 저장되었고, 두 번째 메시지가 실제로 더 늦게 생성되어야
        // lastMessageAt이 최신 메시지 시각인지 검증할 수 있다.
        assertThat(firstMessage.getId()).isNotNull();
        assertThat(latestMessage.getId()).isNotNull();
        assertThat(latestMessage.getId()).isNotEqualTo(firstMessage.getId());
        assertThat(firstMessage.getChannelId()).isEqualTo(publicChannelId);
        assertThat(latestMessage.getChannelId()).isEqualTo(publicChannelId);
        assertThat(firstMessageCreatedAt).isNotNull();
        assertThat(latestMessageCreatedAt).isNotNull();
        assertThat(latestMessageCreatedAt).isAfter(firstMessageCreatedAt);

        // em.clear() 이후에는 저장 직후 영속성 컨텍스트에 남아 있던 Channel, Message 객체가 결과에 영향을 줄 수 없다.
        // 아래 when 절의 foundChannelSummary는 실제 JPQL SELECT, left join, group by, max 집계 결과다.
        em.clear();

        // when
        ChannelSummary foundChannelSummary = channelRepository.findByDetail(publicChannelId).orElseThrow(AssertionError::new);

        // then
        // foundChannelSummary는 단건 객체다.
        // 단건 객체에 extracting(...).containsExactlyInAnyOrder(tuple(...))를 쓰면
        // Iterable projection 검증처럼 동작하지 않아 assertion이 실패할 수 있다.
        // 단건 projection은 각 필드를 직접 검증하는 방식이 가장 명확하다.
        assertThat(foundChannelSummary)
                .satisfies(channelSummary -> {
                    assertThat(channelSummary.id()).isEqualTo(publicChannelId);
                    assertThat(channelSummary.type()).isEqualTo(ChannelType.PUBLIC);
                    assertThat(channelSummary.name()).isEqualTo(createPublicCommand.channelName());
                    assertThat(channelSummary.description()).isEqualTo(createPublicCommand.channelDescription());

                    // H2/JDBC가 timestamp를 저장하고 다시 읽는 과정에서 나노초 일부가 반올림될 수 있다.
                    // 그래서 저장 직후 엔티티의 Instant와 정확히 같은지보다,
                    // 최신 메시지 시각에 충분히 가깝고 첫 번째 메시지보다는 늦은지를 검증한다.
                    assertThat(channelSummary.lastMessageAt()).isNotNull();
                    assertThat(channelSummary.lastMessageAt()).isAfter(firstMessageCreatedAt);
                    assertThat(channelSummary.lastMessageAt())
                            .isAfterOrEqualTo(latestMessageCreatedAt.minusMillis(1))
                            .isBeforeOrEqualTo(latestMessageCreatedAt.plusMillis(1));
                });
    }

    @Test
    @DisplayName("채널 상세 조회 성공 - 메시지가 없으면 마지막 메시지 시간은 null")
    void findByDetail_returnsChannelSummaryWithNullLastMessageAt_whenChannelExistsAndMessagesDoNotExist() {
        // given
        // 이 테스트는 findByDetail(...)이 메시지가 없는 채널도 상세 조회 결과로 반환하는지 검증한다.
        // 쿼리가 Message를 left join 하므로, 연결된 Message row가 없어도 ChannelSummary는 반환되어야 하고
        // max(m.createdAt)의 결과인 lastMessageAt만 null이어야 한다.
        //
        // findByDetail(...)은 channelId만 조건으로 사용한다.
        // 메시지를 만들지 않는 이 케이스에서는 User나 ReadStatus가 필요하지 않다.
        PublicChannelFixture publicChannelFixture = savePublicChannel();
        ChannelCreatePublicCommand createPublicCommand = publicChannelFixture.command();
        Channel publicChannel = publicChannelFixture.channel();

        UUID publicChannelId = publicChannel.getId();

        // 사전 조건을 확인한다.
        // 채널은 실제 DB에 저장되어 있어야 하고, Message는 하나도 없어야 한다.
        assertThat(publicChannelId).isNotNull();
        assertThat(messageRepository.count()).isZero();

        // em.clear() 이후에는 저장 직후 영속성 컨텍스트에 남아 있던 Channel 객체가 결과에 영향을 줄 수 없다.
        // 아래 when 절의 foundChannelSummary는 실제 JPQL SELECT, left join, group by, max 집계 결과다.
        em.clear();

        // when
        ChannelSummary foundChannelSummary = channelRepository.findByDetail(publicChannelId).orElseThrow(AssertionError::new);

        // then
        // 단건 projection이므로 tuple을 이용한 Iterable 스타일 검증 대신 필드를 직접 확인한다.
        // 메시지가 없더라도 left join 덕분에 채널 정보는 반환되고, lastMessageAt만 null이어야 한다.
        assertThat(foundChannelSummary)
                .satisfies(channelSummary -> {
                    assertThat(channelSummary.id()).isEqualTo(publicChannelId);
                    assertThat(channelSummary.type()).isEqualTo(ChannelType.PUBLIC);
                    assertThat(channelSummary.name()).isEqualTo(createPublicCommand.channelName());
                    assertThat(channelSummary.description()).isEqualTo(createPublicCommand.channelDescription());
                    assertThat(channelSummary.lastMessageAt()).isNull();
                });
    }

    @Test
    @DisplayName("채널 상세 조회 성공 - 존재하지 않는 채널이면 Optional.empty 반환")
    void findByDetail_returnsEmpty_whenChannelDoesNotExist() {
        // given
        // 이 테스트는 findByDetail(...)에 전달한 channelId와 일치하는 Channel row가 없을 때
        // Optional.empty를 반환하는지 검증한다.
        //
        // 삭제 동작은 이 테스트의 관심사가 아니다.
        // 채널을 저장한 뒤 deleteById(...)로 지우면 findByDetail(...)뿐 아니라 삭제 쿼리와 flush 동작까지
        // 함께 검증하게 되므로, 여기서는 처음부터 존재하지 않는 UUID를 조회한다.
        //
        // 다만 테이블이 완전히 비어 있으면 "어떤 id로 조회해도 empty"라는 너무 약한 상황이 된다.
        // 그래서 다른 채널을 하나 저장해 둔 상태에서, 그 id와 다른 missingChannelId로 조회한다.
        PublicChannelFixture publicChannelFixture = savePublicChannel();
        Channel publicChannel = publicChannelFixture.channel();

        UUID existingChannelId = publicChannel.getId();
        UUID missingChannelId = UUID.randomUUID();

        // 사전 조건을 먼저 확인한다.
        // DB에는 실제 채널이 하나 존재해야 하고, 조회할 missingChannelId는 그 채널의 id와 달라야 한다.
        // 이 전제가 맞아야 findByDetail(...)의 where c.id = :channelId 조건이 제대로 적용되는지 검증할 수 있다.
        assertThat(existingChannelId).isNotNull();
        assertThat(missingChannelId).isNotNull();
        assertThat(missingChannelId).isNotEqualTo(existingChannelId);
        assertThat(channelRepository.existsById(existingChannelId)).isTrue();
        assertThat(channelRepository.existsById(missingChannelId)).isFalse();

        // em.clear() 이후에는 저장 직후 영속성 컨텍스트에 남아 있던 Channel 객체가 조회 결과에 영향을 줄 수 없다.
        // 아래 when 절의 channelSummary는 실제 JPQL SELECT 결과다.
        em.clear();

        // when
        Optional<ChannelSummary> channelSummary = channelRepository.findByDetail(missingChannelId);

        // then
        // channelId가 일치하는 Channel row가 없으므로 projection을 만들 수 없고 Optional.empty가 반환되어야 한다.
        // DB에 다른 채널이 존재하더라도 다른 id의 채널이 잘못 반환되면 안 된다.
        assertThat(channelSummary).isEmpty();
    }

    // ChannelRepository는 JPQL projection, exists subquery, left join 집계를 실제 DB에서 검증하는 테스트 대상이다.
    // 따라서 User를 mock으로 대체하지 않고 실제 row로 저장해 ReadStatus와 Message의 FK 조건이 JPA 매핑을 통과하게 한다.
    private User saveUser(String username) {
        UserCreateCommand command = new UserCreateCommand(
                username,
                username + "Password",
                username + "Email@gmail.com"
        );

        return userRepository.saveAndFlush(new User(command, null));
    }

    // PUBLIC 채널은 findVisibleChannels(...)에서 ReadStatus 없이도 항상 보이는 대상이다.
    // command와 entity를 함께 반환해 given 절은 짧게 유지하고, then 절에서는 projection의 name/description을 원본 command와 비교한다.
    private PublicChannelFixture savePublicChannel() {
        ChannelCreatePublicCommand command = new ChannelCreatePublicCommand(
                "publicChannel",
                "publicChannelDescription",
                ChannelType.PUBLIC
        );

        return new PublicChannelFixture(channelRepository.saveAndFlush(new Channel(command)), command);
    }

    // PRIVATE 채널의 command.participantIds는 생성 입력값이고, visible 여부의 실제 기준은 ReadStatus row다.
    // 그래서 private channel 생성과 참여 row 생성을 분리해 테스트마다 "ReadStatus를 만들었는지"가 드러나게 한다.
    private PrivateChannelFixture savePrivateChannelFor(User participant) {
        ChannelCreatePrivateCommand command = new ChannelCreatePrivateCommand(
                List.of(participant.getId()),
                ChannelType.PRIVATE
        );

        return new PrivateChannelFixture(channelRepository.saveAndFlush(new Channel(command)), command);
    }

    // findVisibleChannels(...)의 PRIVATE 참여 조건은 read_statuses 테이블에 해당 user/channel 조합이 존재하는지다.
    // 참여가 필요한 테스트에서만 이 헬퍼를 호출해, 참여 여부 fixture를 명시적으로 만든다.
    private ReadStatus joinChannel(Channel channel, User user) {
        return readStatusRepository.saveAndFlush(
                new ReadStatus(
                        channel,
                        user,
                        new ReadStatusCreateCommand(user.getId(), Instant.now())
                )
        );
    }

    // lastMessageAt 검증은 Message.createdAt을 DB에서 max(...)로 집계하는지 확인한다.
    // 실제 Message row를 저장해야 left join과 집계가 Repository 쿼리 경로 그대로 검증된다.
    private Message saveMessage(User author, Channel channel, String content) {
        MessageCreateCommand command = new MessageCreateCommand(
                content,
                author.getId(),
                channel.getId()
        );

        return messageRepository.saveAndFlush(new Message(author, channel, command));
    }

    private record PublicChannelFixture(Channel channel, ChannelCreatePublicCommand command) {
    }

    private record PrivateChannelFixture(Channel channel, ChannelCreatePrivateCommand command) {
    }
}
