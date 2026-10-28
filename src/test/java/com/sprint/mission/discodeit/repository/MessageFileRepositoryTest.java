package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.config.JpaAuditingTestConfig;
import com.sprint.mission.discodeit.config.P6SpySqlFormatter;
import com.sprint.mission.discodeit.config.QuerydslTestConfig;
import com.sprint.mission.discodeit.dto.command.channel.ChannelCreatePublicCommand;
import com.sprint.mission.discodeit.dto.command.message.MessageCreateCommand;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * 실제 JPA 저장·조회로 첨부 연결과 소속 채널 조회 쿼리를 검증한다.
 * 파일 ID에 해당하는 채널만 반환되는지, 미연결 파일은 빈 결과인지 DB에서 확인한다.
 */
@DataJpaTest(showSql = false)
@Import(value = {QuerydslTestConfig.class, JpaAuditingTestConfig.class, P6SpySqlFormatter.class})
// MessageFileRepository.bulkInsert(...) native query는 PostgreSQL의 gen_random_uuid()를 사용한다.
// @DataJpaTest는 embedded H2로 Repository SQL을 검증하므로,
// 테스트 DB에서 같은 함수 이름을 호출할 수 있도록 H2 alias를 등록한다.
// 운영 쿼리를 H2 전용 random_uuid()로 바꾸지 않고 native insert SQL의 동작을 검증하기 위한 테스트 전용 호환 설정이다.
@Sql(statements = "CREATE ALIAS IF NOT EXISTS gen_random_uuid FOR \"java.util.UUID.randomUUID\"")
@DisplayName("MessageFileRepository 슬라이스 테스트")
class MessageFileRepositoryTest {
    @Autowired
    MessageFileRepository messageFileRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    BinaryContentRepository binaryContentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ChannelRepository channelRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("파일이 첨부된 메시지의 채널만 조회한다")
    void findChannelByBinaryContentId_returnsOnlyLinkedChannel() {
        User author = saveAuthor();
        Channel targetChannel = savePublicChannel("target");
        Channel otherChannel = savePublicChannel("other");

        Message targetMessage = saveMessage(author, targetChannel, "target");
        Message otherMessage = saveMessage(author, otherChannel, "other");

        BinaryContent targetFile = saveBinaryContent("target.txt", "text/plain", 1L);
        BinaryContent otherFile = saveBinaryContent("other.txt", "text/plain", 1L);

        messageFileRepository.bulkInsert(
                List.of(targetFile.getId()), targetMessage.getId()
        );
        messageFileRepository.bulkInsert(
                List.of(otherFile.getId()), otherMessage.getId()
        );
        em.flush();
        em.clear();

        assertThat(
                messageFileRepository.findChannelByBinaryContentId(targetFile.getId())
                        .map(Channel::getId)
        ).contains(targetChannel.getId());
    }

    @Test
    @DisplayName("메시지에 첨부되지 않은 파일은 채널 조회 결과가 없다")
    void findChannelByBinaryContentId_returnsEmptyWhenNotAttached() {
        User author = saveAuthor();
        Channel channel = savePublicChannel("channel");
        Message message = saveMessage(author, channel, "message");

        BinaryContent attachedFile = saveBinaryContent("attached.txt", "text/plain", 1L);
        BinaryContent unassignedFile = saveBinaryContent("unassigned.txt", "text/plain", 1L);

        messageFileRepository.bulkInsert(
                List.of(attachedFile.getId()), message.getId()
        );
        em.flush();
        em.clear();

        assertThat(
                messageFileRepository.findChannelByBinaryContentId(unassignedFile.getId())
        ).isEmpty();

        assertThat(
                messageFileRepository.findChannelByBinaryContentId(UUID.randomUUID())
        ).isEmpty();
    }

    @Test
    @DisplayName("메시지 기준 첨부 파일 존재 여부 조회 성공 - 메시지에 첨부 파일이 있으면 true 반환")
    void existsByMessageId_returnsTrue_whenMessageHasFiles() {
        // given
        // 이 테스트의 대상은 MessageFileRepository.existsByMessage_Id(...) derived query다.
        // 특정 messageId에 연결된 MessageFile이 하나라도 있으면 true를 반환해야 한다.
        // 단순히 Message row만 저장하면 "메시지가 존재해서 true"인지,
        // "message_files row가 존재해서 true"인지 테스트 의도가 흐려질 수 있다.
        // 그래서 첨부 파일이 있는 targetMessage와, Message row는 있지만 첨부 파일이 없는 messageWithoutFile을 함께 준비한다.
        // 메시지는 author와 channel FK를 가지므로 실제 User와 Channel을 먼저 저장한다.
        User savedAuthor = saveAuthor();
        Channel savedChannel = savePublicChannel("testChannel");

        // targetMessage는 첨부 파일을 연결할 조회 대상 메시지다.
        Message savedTargetMessage = saveMessage(savedAuthor, savedChannel, "targetMessage");

        // messageWithoutFile은 Message row만 존재하는 대조군이다.
        // 이 메시지에 파일을 연결하지 않아야 existsByMessage_Id(...)가 Message 존재 여부가 아니라
        // MessageFile 존재 여부를 기준으로 판단한다는 점을 확인할 수 있다.
        Message savedMessageWithoutFile = saveMessage(savedAuthor, savedChannel, "messageWithoutFile");

        // bulkInsert(...)는 binary_contents에서 fileIds에 해당하는 row를 select해서 message_files에 insert한다.
        // 따라서 MessageFile을 직접 save하지 않고, 실제 BinaryContent row를 만든 뒤 bulkInsert(...)를 호출한다.
        BinaryContent savedBinaryContent = saveBinaryContent("testFileName", "image/png", 1_000L);

        UUID savedAuthorId = savedAuthor.getId();
        UUID savedChannelId = savedChannel.getId();
        UUID savedTargetMessageId = savedTargetMessage.getId();
        UUID savedMessageWithoutFileId = savedMessageWithoutFile.getId();
        UUID savedBinaryContentId = savedBinaryContent.getId();

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // id가 null이거나 두 메시지가 구분되지 않으면 exists 결과의 원인을 명확히 판단하기 어렵다.
        assertThat(savedAuthorId).isNotNull();
        assertThat(savedChannelId).isNotNull();
        assertThat(savedTargetMessageId).isNotNull();
        assertThat(savedMessageWithoutFileId).isNotNull();
        assertThat(savedBinaryContentId).isNotNull();
        assertThat(savedMessageWithoutFileId).isNotEqualTo(savedTargetMessageId);

        // targetMessage에만 첨부 파일 1건을 연결한다.
        // bulkInsert(...) 반환값이 1이어야 message_files row가 실제로 1건 생성됐다는 직접 근거가 된다.
        int savedCount = messageFileRepository.bulkInsert(List.of(savedBinaryContentId), savedTargetMessageId);

        assertThat(savedCount).isEqualTo(1);

        // native DML 이후에는 flush/clear로 DB 반영과 재조회 기준을 명확히 한다.
        // 아래 검증은 1차 캐시가 아니라 실제 message_files row를 기준으로 수행되어야 한다.
        em.flush();
        em.clear();

        // exists 조회 전에 targetMessage에는 MessageFile이 실제로 존재하고,
        // messageWithoutFile에는 MessageFile이 없다는 fixture 상태를 확인한다.
        // 이 검증이 있어야 true 결과가 given 구성 실패로 우연히 나온 것이 아님을 알 수 있다.
        assertThat(messageFileRepository.findAllByMessage_Id(savedTargetMessageId))
                .hasSize(1)
                .extracting(MessageFile::getMessageId, MessageFile::getFileId)
                .containsExactly(tuple(savedTargetMessageId, savedBinaryContentId));
        assertThat(messageFileRepository.findAllByMessage_Id(savedMessageWithoutFileId)).isEmpty();

        // when
        // MessageFile이 연결된 targetMessage id로 첨부 파일 존재 여부를 조회한다.
        boolean exists = messageFileRepository.existsByMessage_Id(savedTargetMessageId);

        // then
        // targetMessage에는 MessageFile이 하나 이상 존재하므로 true를 반환해야 한다.
        assertThat(exists).isTrue();

        // 전체 MessageFile 수는 targetMessage에 연결한 1건뿐이어야 한다.
        // existsByMessage_Id(...)가 조회 과정에서 데이터를 변경하지 않는다는 점도 보조적으로 확인한다.
        assertThat(messageFileRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("메시지 기준 첨부 파일 존재 여부 조회 성공 - 메시지에 첨부 파일이 없으면 false 반환")
    void existsByMessageId_returnsFalse_whenMessageHasNoFiles() {
        // given
        // 이 테스트의 대상은 MessageFileRepository.existsByMessage_Id(...) derived query다.
        // 특정 messageId에 연결된 MessageFile이 없으면 false를 반환해야 한다.
        // 여기서 검증하려는 상황은 "Message row가 없어서 false"가 아니라,
        // "Message row는 존재하지만 message_files row가 없어서 false"인 경우다.
        // 그래서 첨부 파일이 있는 targetMessage와 첨부 파일이 없는 messageWithoutFile을 함께 저장한다.
        // Message는 author와 channel을 참조하므로 실제 User와 Channel을 먼저 저장한다.
        User savedAuthor = saveAuthor();
        Channel savedChannel = savePublicChannel("testChannel");

        // targetMessage는 첨부 파일을 연결할 대조군 메시지다.
        // 이 row가 있어야 false 결과가 "message_files 테이블이 비어서" 나온 것이 아님을 확인할 수 있다.
        Message savedTargetMessage = saveMessage(savedAuthor, savedChannel, "targetMessage");

        // messageWithoutFile은 조회 대상 메시지다.
        // Message row는 실제로 존재하지만, 이 메시지에는 MessageFile을 연결하지 않는다.
        Message savedMessageWithoutFile = saveMessage(savedAuthor, savedChannel, "messageWithoutFile");

        // targetMessage에만 연결할 실제 파일 row를 저장한다.
        // existsByMessage_Id(messageWithoutFileId)는 이 파일이 다른 메시지에 연결되어 있어도 false여야 한다.
        BinaryContent savedBinaryContent = saveBinaryContent("testFileName", "image/png", 1_000L);

        UUID savedAuthorId = savedAuthor.getId();
        UUID savedChannelId = savedChannel.getId();
        UUID savedTargetMessageId = savedTargetMessage.getId();
        UUID savedMessageWithoutFileId = savedMessageWithoutFile.getId();
        UUID savedBinaryContentId = savedBinaryContent.getId();

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // id가 null이거나 두 메시지가 같은 row라면 false 결과의 원인을 정확히 판단할 수 없다.
        assertThat(savedAuthorId).isNotNull();
        assertThat(savedChannelId).isNotNull();
        assertThat(savedTargetMessageId).isNotNull();
        assertThat(savedMessageWithoutFileId).isNotNull();
        assertThat(savedBinaryContentId).isNotNull();
        assertThat(savedMessageWithoutFileId).isNotEqualTo(savedTargetMessageId);

        // targetMessage에만 파일 1건을 연결한다.
        // bulkInsert(...)는 binary_contents에서 fileIds에 해당하는 row를 select해 message_files에 insert하므로,
        // 반환값 1은 MessageFile row가 실제로 1건 생성됐다는 직접 근거다.
        int savedCount = messageFileRepository.bulkInsert(List.of(savedBinaryContentId), savedTargetMessageId);

        assertThat(savedCount).isEqualTo(1);

        // native DML 이후에는 flush/clear로 DB 반영과 재조회 기준을 명확히 한다.
        // 아래 검증은 영속성 컨텍스트가 아니라 실제 message_files 테이블 상태를 기준으로 수행되어야 한다.
        em.flush();
        em.clear();

        // exists 조회 전에 targetMessage에는 MessageFile이 실제로 존재하고,
        // messageWithoutFile에는 MessageFile이 없다는 fixture 상태를 확인한다.
        // 이 검증이 있어야 false 결과가 "조회 대상 메시지에 연결 row가 없어서" 나온 것임을 알 수 있다.
        assertThat(messageFileRepository.findAllByMessage_Id(savedTargetMessageId))
                .hasSize(1)
                .extracting(MessageFile::getMessageId, MessageFile::getFileId)
                .containsExactly(tuple(savedTargetMessageId, savedBinaryContentId));
        assertThat(messageFileRepository.findAllByMessage_Id(savedMessageWithoutFileId)).isEmpty();

        // when
        // Message row는 존재하지만 MessageFile이 하나도 연결되지 않은 messageWithoutFile id로 존재 여부를 조회한다.
        boolean exists = messageFileRepository.existsByMessage_Id(savedMessageWithoutFileId);

        // then
        // messageWithoutFile에는 message_files.message_id와 매칭되는 row가 없으므로 false를 반환해야 한다.
        assertThat(exists).isFalse();

        // 전체 MessageFile 수는 targetMessage에 연결한 1건뿐이어야 한다.
        // existsByMessage_Id(...)가 조회 과정에서 데이터를 변경하지 않는다는 점도 보조적으로 확인한다.
        assertThat(messageFileRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("메시지 기준 첨부 파일 삭제 성공 - 메시지에 연결된 첨부 파일 삭제")
    void deleteByMessageId_deletesOnlyMessageFiles_whenMessageHasFiles() {
        // given
        // 이 테스트의 대상은 MessageFileRepository.deleteByMessage_Id(...) derived delete query다.
        // 특정 messageId에 연결된 MessageFile을 모두 삭제하되,
        // 다른 메시지에 연결된 MessageFile은 같은 작성자/채널에 있더라도 삭제되면 안 된다.
        // 따라서 삭제 대상 targetMessage에는 파일 3건을 연결하고,
        // 대조군 otherMessage에는 파일 1건을 연결해 message_id 조건이 정확히 적용되는지 검증한다.
        // Message는 author와 channel을 참조하므로 실제 User와 Channel을 먼저 저장한다.
        User savedAuthor = saveAuthor();
        Channel savedChannel = savePublicChannel("testChannel");

        // targetMessage는 삭제 대상 MessageFile들이 연결될 메시지다.
        Message savedTargetMessage = saveMessage(savedAuthor, savedChannel, "targetMessage");

        // otherMessage는 삭제 대상이 아닌 대조군 메시지다.
        // 이 메시지에 연결된 MessageFile은 deleteByMessage_Id(targetMessageId) 이후에도 남아 있어야 한다.
        Message savedOtherMessage = saveMessage(savedAuthor, savedChannel, "otherMessage");

        // targetMessage에 연결할 파일 3건과 otherMessage에 연결할 파일 1건을 저장한다.
        // 파일 id가 모두 달라야 message_files의 unique(message_id, file_id) 제약을 피하면서
        // 삭제 전/후 남는 row를 명확히 구분할 수 있다.
        BinaryContent savedTargetBinaryContent = saveBinaryContent("targetFile1", "image/png", 1_000L);
        BinaryContent savedSecondTargetBinaryContent = saveBinaryContent("targetFile2", "image/png", 2_000L);
        BinaryContent savedThirdTargetBinaryContent = saveBinaryContent("targetFile3", "image/png", 3_000L);
        BinaryContent savedRemainingBinaryContent = saveBinaryContent("remainingFile", "image/png", 4_000L);

        UUID savedAuthorId = savedAuthor.getId();
        UUID savedChannelId = savedChannel.getId();
        UUID savedTargetMessageId = savedTargetMessage.getId();
        UUID savedOtherMessageId = savedOtherMessage.getId();
        UUID savedTargetBinaryContentId = savedTargetBinaryContent.getId();
        UUID savedSecondTargetBinaryContentId = savedSecondTargetBinaryContent.getId();
        UUID savedThirdTargetBinaryContentId = savedThirdTargetBinaryContent.getId();
        UUID savedRemainingBinaryContentId = savedRemainingBinaryContent.getId();

        List<UUID> targetFileIds = List.of(
                savedTargetBinaryContentId,
                savedSecondTargetBinaryContentId,
                savedThirdTargetBinaryContentId
        );

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // id가 null이거나 target/other 메시지가 구분되지 않으면 삭제 조건 검증이 의미 없어질 수 있다.
        assertThat(savedAuthorId).isNotNull();
        assertThat(savedChannelId).isNotNull();
        assertThat(savedTargetMessageId).isNotNull();
        assertThat(savedOtherMessageId).isNotNull();
        assertThat(savedTargetBinaryContentId).isNotNull();
        assertThat(savedSecondTargetBinaryContentId).isNotNull();
        assertThat(savedThirdTargetBinaryContentId).isNotNull();
        assertThat(savedRemainingBinaryContentId).isNotNull();
        assertThat(savedOtherMessageId).isNotEqualTo(savedTargetMessageId);
        assertThat(savedRemainingBinaryContentId).isNotIn(targetFileIds);

        // targetMessage에 파일 3건을 연결한다.
        // bulkInsert(...)는 binary_contents에서 fileIds에 해당하는 row를 select해 message_files에 insert한다.
        int targetSavedCount = messageFileRepository.bulkInsert(targetFileIds, savedTargetMessageId);

        // otherMessage에는 대조군 파일 1건을 연결한다.
        // 이 row가 삭제 후에도 남아 있어야 deleteByMessage_Id(...)가 message_id 조건만 삭제했다는 점을 확인할 수 있다.
        int remainingSavedCount = messageFileRepository.bulkInsert(List.of(savedRemainingBinaryContentId), savedOtherMessageId);

        assertThat(targetSavedCount).isEqualTo(3);
        assertThat(remainingSavedCount).isEqualTo(1);

        // native DML 이후에는 flush/clear로 DB 반영과 재조회 기준을 명확히 한다.
        em.flush();
        em.clear();

        // 삭제 전에 targetMessage에는 MessageFile 3건이 실제로 존재해야 한다.
        // 이 사전 검증이 있어야 삭제 후 빈 목록이 "원래 없어서 빈 목록"인 경우와 구분된다.
        List<MessageFile> messageFilesForTargetMessage = messageFileRepository.findAllByMessage_Id(savedTargetMessageId);
        assertThat(messageFilesForTargetMessage)
                .hasSize(3)
                .extracting(MessageFile::getMessageId, MessageFile::getFileId)
                .containsExactlyInAnyOrder(
                        tuple(savedTargetMessageId, savedTargetBinaryContentId),
                        tuple(savedTargetMessageId, savedSecondTargetBinaryContentId),
                        tuple(savedTargetMessageId, savedThirdTargetBinaryContentId)
                );

        // 대조군 메시지에도 MessageFile이 실제로 존재해야 한다.
        // 이 row가 삭제 후에도 남아 있어야 삭제 범위가 targetMessage로 제한됐다는 점을 확인할 수 있다.
        assertThat(messageFileRepository.findAllByMessage_Id(savedOtherMessageId))
                .hasSize(1)
                .singleElement()
                .extracting(MessageFile::getMessageId, MessageFile::getFileId)
                .containsExactly(savedOtherMessageId, savedRemainingBinaryContentId);

        assertThat(messageFileRepository.count()).isEqualTo(4);

        // 영속성 컨텍스트를 한 번 더 비워, delete와 이후 검증이 관리 객체 상태에 기대지 않도록 한다.
        em.clear();

        // when
        // 삭제 대상 메시지 id로 MessageFile을 삭제한다.
        // deleteByMessage_Id(...)는 Message 자체나 BinaryContent가 아니라
        // message_files 테이블의 message_id가 일치하는 row만 삭제해야 한다.
        messageFileRepository.deleteByMessage_Id(savedTargetMessageId);

        // delete SQL을 DB에 즉시 반영하고, 남아 있는 영속 객체가 결과 검증에 영향을 주지 않도록 다시 비운다.
        em.flush();
        em.clear();

        // then
        // targetMessage에 연결되어 있던 MessageFile은 모두 삭제되어야 한다.
        List<MessageFile> messageFileListAfterDelete = messageFileRepository.findAllByMessage_Id(savedTargetMessageId);
        assertThat(messageFileListAfterDelete).isEmpty();

        // 반면 otherMessage에 연결된 MessageFile은 삭제 조건에 해당하지 않으므로 그대로 남아 있어야 한다.
        // 이 검증이 없으면 deleteByMessage_Id(...)가 실수로 더 넓은 범위의 row를 삭제해도 놓칠 수 있다.
        assertThat(messageFileRepository.findAllByMessage_Id(savedOtherMessageId))
                .hasSize(1)
                .singleElement()
                .extracting(MessageFile::getMessageId, MessageFile::getFileId)
                .containsExactly(savedOtherMessageId, savedRemainingBinaryContentId);

        // 전체 MessageFile 개수도 보조적으로 확인한다.
        // given에서 4건을 만들고 targetMessage의 3건만 삭제했으므로, 최종적으로 1건만 남아야 한다.
        assertThat(messageFileRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("메시지별 첨부 파일 목록 조회 성공 - BinaryContent를 함께 조회")
    void findAllByMessageId_fetchesBinaryContent_whenMessageHasFiles() {
        // given
        // 이 테스트의 대상은 MessageFileRepository.findAllByMessage_Id(...) derived query다.
        // 특정 messageId에 연결된 MessageFile 목록을 조회하면서,
        // Repository에 선언된 EntityGraph(binaryContent)에 의해 BinaryContent도 함께 로딩되어야 한다.
        // 그래서 targetMessage에는 파일 3건을 연결하고,
        // otherMessage에는 별도 파일 1건을 연결해 message_id 조건과 EntityGraph를 함께 검증한다.
        // Message는 author와 channel을 참조하므로 실제 User와 Channel을 먼저 저장한다.
        User savedAuthor = saveAuthor();
        Channel savedChannel = savePublicChannel("testChannel");

        // targetMessage는 조회 대상 메시지다.
        // findAllByMessage_Id(targetMessageId)는 이 메시지에 연결된 MessageFile만 반환해야 한다.
        Message savedTargetMessage = saveMessage(savedAuthor, savedChannel, "targetMessage");

        // otherMessage는 대조군 메시지다.
        // 이 메시지에도 MessageFile을 연결해 두어, 조회 결과가 targetMessage로 제한되는지 확인한다.
        Message savedOtherMessage = saveMessage(savedAuthor, savedChannel, "otherMessage");

        // targetMessage에 연결할 파일 3건과 otherMessage에 연결할 파일 1건을 저장한다.
        // BinaryContent의 필드 값을 서로 다르게 구성하면, EntityGraph로 함께 조회된 실제 파일 엔티티를 더 명확히 검증할 수 있다.
        BinaryContent savedTargetBinaryContent = saveBinaryContent("targetFile1", "image/png", 1_000L);
        BinaryContent savedSecondTargetBinaryContent = saveBinaryContent("targetFile2", "image/jpeg", 2_000L);
        BinaryContent savedThirdTargetBinaryContent = saveBinaryContent("targetFile3", "application/pdf", 3_000L);
        BinaryContent savedRemainingBinaryContent = saveBinaryContent("remainingFile", "text/plain", 4_000L);

        UUID savedAuthorId = savedAuthor.getId();
        UUID savedChannelId = savedChannel.getId();
        UUID savedTargetMessageId = savedTargetMessage.getId();
        UUID savedOtherMessageId = savedOtherMessage.getId();
        UUID savedTargetBinaryContentId = savedTargetBinaryContent.getId();
        UUID savedSecondTargetBinaryContentId = savedSecondTargetBinaryContent.getId();
        UUID savedThirdTargetBinaryContentId = savedThirdTargetBinaryContent.getId();
        UUID savedRemainingBinaryContentId = savedRemainingBinaryContent.getId();

        List<UUID> targetFileIds = List.of(
                savedTargetBinaryContentId,
                savedSecondTargetBinaryContentId,
                savedThirdTargetBinaryContentId
        );

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // id가 null이거나 target/other 메시지와 파일이 서로 구분되지 않으면 조회 조건 검증이 의미 없어질 수 있다.
        assertThat(savedAuthorId).isNotNull();
        assertThat(savedChannelId).isNotNull();
        assertThat(savedTargetMessageId).isNotNull();
        assertThat(savedOtherMessageId).isNotNull();
        assertThat(savedTargetBinaryContentId).isNotNull();
        assertThat(savedSecondTargetBinaryContentId).isNotNull();
        assertThat(savedThirdTargetBinaryContentId).isNotNull();
        assertThat(savedRemainingBinaryContentId).isNotNull();
        assertThat(savedOtherMessageId).isNotEqualTo(savedTargetMessageId);
        assertThat(savedRemainingBinaryContentId).isNotIn(targetFileIds);

        // targetMessage에 파일 3건을 연결한다.
        // bulkInsert(...)는 binary_contents에서 fileIds에 해당하는 row를 select해 message_files에 insert한다.
        int targetSavedCount = messageFileRepository.bulkInsert(targetFileIds, savedTargetMessageId);

        // otherMessage에도 대조군 파일 1건을 연결한다.
        // 이 row가 있어야 findAllByMessage_Id(targetMessageId)가 다른 메시지의 파일을 제외하는지 확인할 수 있다.
        int remainingSavedCount = messageFileRepository.bulkInsert(List.of(savedRemainingBinaryContentId), savedOtherMessageId);

        assertThat(targetSavedCount).isEqualTo(3);
        assertThat(remainingSavedCount).isEqualTo(1);

        // native DML 이후에는 flush/clear로 DB 반영과 재조회 기준을 명확히 한다.
        // 아래 조회는 1차 캐시가 아니라 실제 message_files row를 기준으로 수행되어야 한다.
        em.flush();
        em.clear();

        // when
        // 조회 대상 메시지 id로 MessageFile 목록을 조회한다.
        List<MessageFile> messageFilesForTargetMessage = messageFileRepository.findAllByMessage_Id(savedTargetMessageId);

        PersistenceUnitUtil persistenceUnitUtil = getPersistenceUnitUtil();

        // then
        // findAllByMessage_Id(...)의 EntityGraph가 유지되면 MessageFile.binaryContent는 이미 로딩된 상태여야 한다.
        // 이 검증은 getFileId()나 getBinaryContent() 접근으로 지연 로딩이 발생하기 전에 먼저 수행한다.
        messageFilesForTargetMessage.forEach(messageFile -> {
            assertThat(persistenceUnitUtil.isLoaded(messageFile, "binaryContent")).isTrue();
        });

        // targetMessage에는 파일 3건이 모두 조회되어야 한다.
        // 순서는 Repository 계약이 아니므로 messageId, fileId 조합을 순서와 무관하게 검증한다.
        assertThat(messageFilesForTargetMessage)
                .hasSize(3)
                .extracting(MessageFile::getMessageId, MessageFile::getFileId)
                .containsExactlyInAnyOrder(
                        tuple(savedTargetMessageId, savedTargetBinaryContentId),
                        tuple(savedTargetMessageId, savedSecondTargetBinaryContentId),
                        tuple(savedTargetMessageId, savedThirdTargetBinaryContentId)
                );

        // BinaryContent가 함께 조회된 상태이므로 파일 엔티티의 실제 필드 값도 확인한다.
        // 이 검증은 단순 FK(file_id)만 맞는 것이 아니라, 연관 BinaryContent가 의도한 row인지까지 확인한다.
        assertThat(messageFilesForTargetMessage)
                .extracting(
                        MessageFile::getMessageId,
                        messageFile -> messageFile.getBinaryContent().getId(),
                        messageFile -> messageFile.getBinaryContent().getOriginalFileName(),
                        messageFile -> messageFile.getBinaryContent().getContentType(),
                        messageFile -> messageFile.getBinaryContent().getSize()
                )
                .containsExactlyInAnyOrder(
                        tuple(savedTargetMessageId, savedTargetBinaryContentId, "targetFile1", "image/png", 1_000L),
                        tuple(savedTargetMessageId, savedSecondTargetBinaryContentId, "targetFile2", "image/jpeg", 2_000L),
                        tuple(savedTargetMessageId, savedThirdTargetBinaryContentId, "targetFile3", "application/pdf", 3_000L)
                );

        // otherMessage에도 MessageFile이 실제로 존재해야 한다.
        // 이 row가 targetMessage 조회 결과에 섞이지 않아야 message_id 조건이 정확히 적용됐다고 볼 수 있다.
        assertThat(messageFileRepository.findAllByMessage_Id(savedOtherMessageId))
                .hasSize(1)
                .singleElement()
                .extracting(MessageFile::getMessageId, MessageFile::getFileId)
                .containsExactly(savedOtherMessageId, savedRemainingBinaryContentId);

        // 전체 MessageFile 수는 targetMessage 3건 + otherMessage 1건으로 총 4건이어야 한다.
        // findAllByMessage_Id(...)가 조회 과정에서 데이터를 변경하지 않는다는 점도 보조적으로 확인한다.
        assertThat(messageFileRepository.count()).isEqualTo(4);

    }

    @Test
    @DisplayName("메시지별 첨부 파일 목록 조회 성공 - 첨부 파일이 없으면 빈 목록 반환")
    void findAllByMessageId_returnsEmptyList_whenMessageHasNoFiles() {
        // given
        // 이 테스트의 대상은 MessageFileRepository.findAllByMessage_Id(...) derived query다.
        // 특정 messageId에 연결된 MessageFile이 없으면 빈 목록을 반환해야 한다.
        // 단순히 message_files 테이블을 비워 둔 상태에서 조회하면 "테이블이 비어서 빈 목록"인 경우와 구분하기 어렵다.
        // 그래서 파일이 연결된 messageWithFiles와, Message row는 존재하지만 파일이 없는 messageWithoutFiles를 함께 준비한다.
        // Message는 author와 channel을 참조하므로 실제 User와 Channel을 먼저 저장한다.
        User savedAuthor = saveAuthor();
        Channel savedChannel = savePublicChannel("testChannel");

        // messageWithFiles는 대조군 메시지다.
        // 이 메시지에는 MessageFile을 실제로 연결해 두어, 빈 결과가 전체 테이블 부재 때문이 아님을 확인한다.
        Message savedMessageWithFiles = saveMessage(savedAuthor, savedChannel, "messageWithFiles");

        // messageWithoutFiles는 조회 대상 메시지다.
        // Message row는 실제로 존재하지만 이 메시지에는 MessageFile을 하나도 연결하지 않는다.
        Message savedMessageWithoutFiles = saveMessage(savedAuthor, savedChannel, "messageWithoutFiles");

        // messageWithFiles에 연결할 실제 파일 3건을 저장한다.
        // 조회 대상 messageWithoutFiles에는 이 파일들을 연결하지 않아야 한다.
        BinaryContent savedFirstBinaryContent = saveBinaryContent("firstFile", "image/png", 1_000L);
        BinaryContent savedSecondBinaryContent = saveBinaryContent("secondFile", "image/jpeg", 2_000L);
        BinaryContent savedThirdBinaryContent = saveBinaryContent("thirdFile", "application/pdf", 3_000L);

        UUID savedAuthorId = savedAuthor.getId();
        UUID savedChannelId = savedChannel.getId();
        UUID savedMessageWithFilesId = savedMessageWithFiles.getId();
        UUID savedMessageWithoutFilesId = savedMessageWithoutFiles.getId();
        UUID savedFirstBinaryContentId = savedFirstBinaryContent.getId();
        UUID savedSecondBinaryContentId = savedSecondBinaryContent.getId();
        UUID savedThirdBinaryContentId = savedThirdBinaryContent.getId();

        List<UUID> fileIdsToConnect = List.of(
                savedFirstBinaryContentId,
                savedSecondBinaryContentId,
                savedThirdBinaryContentId
        );

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // id가 null이거나 두 메시지가 같은 row라면 빈 목록 결과의 원인을 정확히 판단할 수 없다.
        assertThat(savedAuthorId).isNotNull();
        assertThat(savedChannelId).isNotNull();
        assertThat(savedMessageWithFilesId).isNotNull();
        assertThat(savedMessageWithoutFilesId).isNotNull();
        assertThat(savedFirstBinaryContentId).isNotNull();
        assertThat(savedSecondBinaryContentId).isNotNull();
        assertThat(savedThirdBinaryContentId).isNotNull();
        assertThat(savedMessageWithoutFilesId).isNotEqualTo(savedMessageWithFilesId);
        assertThat(fileIdsToConnect).contains(savedFirstBinaryContentId, savedSecondBinaryContentId, savedThirdBinaryContentId);

        // messageWithFiles에만 파일 3건을 연결한다.
        // bulkInsert(...) 반환값이 3이어야 대조군 MessageFile row가 실제로 생성됐다는 직접 근거가 된다.
        int savedCount = messageFileRepository.bulkInsert(fileIdsToConnect, savedMessageWithFilesId);

        assertThat(savedCount).isEqualTo(3);

        // native DML 이후에는 flush/clear로 DB 반영과 재조회 기준을 명확히 한다.
        // 아래 조회는 1차 캐시가 아니라 실제 message_files 테이블 상태를 기준으로 수행되어야 한다.
        em.flush();
        em.clear();

        // 조회 전에 대조군 메시지에는 MessageFile이 실제로 존재한다는 점을 확인한다.
        // 이 검증이 있어야 아래 빈 목록이 "전체 테이블이 비어서" 나온 결과가 아님을 알 수 있다.
        assertThat(messageFileRepository.findAllByMessage_Id(savedMessageWithFilesId))
                .hasSize(3)
                .extracting(MessageFile::getMessageId, MessageFile::getFileId)
                .containsExactlyInAnyOrder(
                        tuple(savedMessageWithFilesId, savedFirstBinaryContentId),
                        tuple(savedMessageWithFilesId, savedSecondBinaryContentId),
                        tuple(savedMessageWithFilesId, savedThirdBinaryContentId)
                );

        // when
        // Message row는 존재하지만 MessageFile이 하나도 연결되지 않은 messageWithoutFiles id로 목록을 조회한다.
        List<MessageFile> messageFilesForMessageWithoutFiles =
                messageFileRepository.findAllByMessage_Id(savedMessageWithoutFilesId);

        // then
        // messageWithoutFiles에는 message_files.message_id와 매칭되는 row가 없으므로 빈 목록을 반환해야 한다.
        assertThat(messageFilesForMessageWithoutFiles).isEmpty();

        // 전체 MessageFile 수는 대조군 메시지에 연결한 3건뿐이어야 한다.
        // findAllByMessage_Id(...)가 조회 과정에서 데이터를 변경하지 않는다는 점도 보조적으로 확인한다.
        assertThat(messageFileRepository.count()).isEqualTo(3);

    }

    @Test
    @DisplayName("여러 메시지 첨부 파일 목록 조회 성공 - BinaryContent와 Message를 함께 조회")
    void findAllByMessageIdIn_fetchesBinaryContentAndMessage_whenMessageIdsExist() {
        // given
        // 이 테스트의 대상은 MessageFileRepository.findAllByMessageIdIn(...) derived query다.
        // messageIds 목록에 포함된 여러 메시지의 MessageFile만 반환하고,
        // Repository에 선언된 EntityGraph(binaryContent, message)에 의해 두 연관 객체가 함께 로딩되어야 한다.
        // 단일 메시지만 저장하면 IN 조건을 검증하기 어렵기 때문에,
        // 포함 메시지 2개와 제외 메시지 1개를 모두 같은 테스트 DB에 준비한다.
        // Message는 author와 channel을 참조하므로 실제 User와 Channel을 먼저 저장한다.
        User savedAuthor = saveAuthor();
        Channel savedChannel = savePublicChannel("testChannel");

        // firstIncludedMessage와 secondIncludedMessage는 조회 목록에 포함할 메시지다.
        // excludedMessage는 MessageFile이 존재하지만 조회 목록에는 넣지 않을 대조군 메시지다.
        Message savedFirstIncludedMessage = saveMessage(savedAuthor, savedChannel, "firstIncludedMessage");
        Message savedSecondIncludedMessage = saveMessage(savedAuthor, savedChannel, "secondIncludedMessage");
        Message savedExcludedMessage = saveMessage(savedAuthor, savedChannel, "excludedMessage");

        // 포함 메시지 2개와 제외 메시지 1개에 연결할 파일을 저장한다.
        // 파일 메타데이터를 서로 다르게 두면 EntityGraph로 함께 조회된 BinaryContent가 정확한 row인지 검증하기 쉽다.
        BinaryContent savedFirstBinaryContent = saveBinaryContent("firstFile", "image/png", 1_000L);
        BinaryContent savedSecondBinaryContent = saveBinaryContent("secondFile", "image/jpeg", 2_000L);
        BinaryContent savedThirdBinaryContent = saveBinaryContent("thirdFile", "application/pdf", 3_000L);
        BinaryContent savedExcludedBinaryContent = saveBinaryContent("excludedFile", "text/plain", 4_000L);

        UUID savedAuthorId = savedAuthor.getId();
        UUID savedChannelId = savedChannel.getId();
        UUID savedFirstIncludedMessageId = savedFirstIncludedMessage.getId();
        UUID savedSecondIncludedMessageId = savedSecondIncludedMessage.getId();
        UUID savedExcludedMessageId = savedExcludedMessage.getId();
        UUID savedFirstBinaryContentId = savedFirstBinaryContent.getId();
        UUID savedSecondBinaryContentId = savedSecondBinaryContent.getId();
        UUID savedThirdBinaryContentId = savedThirdBinaryContent.getId();
        UUID savedExcludedBinaryContentId = savedExcludedBinaryContent.getId();

        List<UUID> firstIncludedFileIds = List.of(
                savedFirstBinaryContentId,
                savedSecondBinaryContentId
        );
        List<UUID> messageIdsToFind = List.of(savedFirstIncludedMessageId, savedSecondIncludedMessageId);

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // id가 null이거나 포함/제외 메시지가 서로 구분되지 않으면 IN 조건 검증이 의미 없어질 수 있다.
        assertThat(savedAuthorId).isNotNull();
        assertThat(savedChannelId).isNotNull();
        assertThat(savedFirstIncludedMessageId).isNotNull();
        assertThat(savedSecondIncludedMessageId).isNotNull();
        assertThat(savedExcludedMessageId).isNotNull();
        assertThat(savedFirstBinaryContentId).isNotNull();
        assertThat(savedSecondBinaryContentId).isNotNull();
        assertThat(savedThirdBinaryContentId).isNotNull();
        assertThat(savedExcludedBinaryContentId).isNotNull();
        assertThat(savedSecondIncludedMessageId).isNotEqualTo(savedFirstIncludedMessageId);
        assertThat(savedExcludedMessageId).isNotIn(messageIdsToFind);
        assertThat(firstIncludedFileIds).contains(savedFirstBinaryContentId, savedSecondBinaryContentId);
        assertThat(savedThirdBinaryContentId).isNotIn(firstIncludedFileIds);
        assertThat(savedExcludedBinaryContentId).isNotIn(firstIncludedFileIds);

        // firstIncludedMessage에는 파일 2건을 연결한다.
        // secondIncludedMessage에는 파일 1건을 연결한다.
        // excludedMessage에도 파일 1건을 연결하지만, 조회 목록에는 excludedMessageId를 넣지 않는다.
        int firstIncludedSavedCount = messageFileRepository.bulkInsert(firstIncludedFileIds, savedFirstIncludedMessageId);
        int secondIncludedSavedCount = messageFileRepository.bulkInsert(List.of(savedThirdBinaryContentId), savedSecondIncludedMessageId);
        int excludedSavedCount = messageFileRepository.bulkInsert(List.of(savedExcludedBinaryContentId), savedExcludedMessageId);

        assertThat(firstIncludedSavedCount).isEqualTo(2);
        assertThat(secondIncludedSavedCount).isEqualTo(1);
        assertThat(excludedSavedCount).isEqualTo(1);

        // native DML 이후에는 flush/clear로 DB 반영과 재조회 기준을 명확히 한다.
        // 아래 조회는 1차 캐시가 아니라 실제 message_files row를 기준으로 수행되어야 한다.
        em.flush();
        em.clear();

        // 전체 MessageFile은 포함 메시지 3건 + 제외 메시지 1건으로 총 4건이어야 한다.
        // 이 사전 검증이 있어야 제외 메시지 row가 실제로 존재하는 상황에서 IN 조건이 적용되는지 확인할 수 있다.
        assertThat(messageFileRepository.count()).isEqualTo(4);

        // when
        // 조회 목록에는 포함 메시지 2개의 id만 전달하고, excludedMessageId는 전달하지 않는다.
        List<MessageFile> messageFileList = messageFileRepository.findAllByMessageIdIn(messageIdsToFind);
        PersistenceUnitUtil persistenceUnitUtil = getPersistenceUnitUtil();

        // then
        // findAllByMessageIdIn(...)의 EntityGraph가 유지되면 MessageFile.binaryContent와 MessageFile.message가
        // 조회 직후 이미 로딩된 상태여야 한다.
        // 이 검증은 getBinaryContent(), getMessage() 접근으로 지연 로딩이 발생하기 전에 먼저 수행한다.
        messageFileList.forEach(messageFile -> {
            assertThat(persistenceUnitUtil.isLoaded(messageFile, "binaryContent")).isTrue();
            assertThat(persistenceUnitUtil.isLoaded(messageFile, "message")).isTrue();
        });

        // 조회 결과는 포함 메시지 2개에 연결된 MessageFile 3건이어야 한다.
        // 순서는 Repository 계약이 아니므로 messageId, fileId 조합을 순서와 무관하게 검증한다.
        assertThat(messageFileList)
                .hasSize(3)
                .extracting(MessageFile::getMessageId, MessageFile::getFileId)
                .containsExactlyInAnyOrder(
                        tuple(savedFirstIncludedMessageId, savedFirstBinaryContentId),
                        tuple(savedFirstIncludedMessageId, savedSecondBinaryContentId),
                        tuple(savedSecondIncludedMessageId, savedThirdBinaryContentId)
                );

        // EntityGraph로 함께 조회된 Message와 BinaryContent의 실제 필드 값도 확인한다.
        // 이 검증은 단순 FK만 맞는 것이 아니라, 연관 엔티티가 의도한 row인지까지 확인한다.
        assertThat(messageFileList)
                .extracting(
                        messageFile -> messageFile.getMessage().getId(),
                        messageFile -> messageFile.getMessage().getContent(),
                        messageFile -> messageFile.getBinaryContent().getId(),
                        messageFile -> messageFile.getBinaryContent().getOriginalFileName(),
                        messageFile -> messageFile.getBinaryContent().getContentType(),
                        messageFile -> messageFile.getBinaryContent().getSize()
                )
                .containsExactlyInAnyOrder(
                        tuple(savedFirstIncludedMessageId, "firstIncludedMessage", savedFirstBinaryContentId, "firstFile", "image/png", 1_000L),
                        tuple(savedFirstIncludedMessageId, "firstIncludedMessage", savedSecondBinaryContentId, "secondFile", "image/jpeg", 2_000L),
                        tuple(savedSecondIncludedMessageId, "secondIncludedMessage", savedThirdBinaryContentId, "thirdFile", "application/pdf", 3_000L)
                );

        // excludedMessage에도 MessageFile이 실제로 존재하지만,
        // messageIdsToFind에 포함하지 않았으므로 조회 결과에는 섞이면 안 된다.
        assertThat(messageFileList)
                .extracting(MessageFile::getMessageId)
                .containsOnly(savedFirstIncludedMessageId, savedSecondIncludedMessageId)
                .doesNotContain(savedExcludedMessageId);

        assertThat(messageFileRepository.findAllByMessage_Id(savedExcludedMessageId))
                .hasSize(1)
                .singleElement()
                .extracting(MessageFile::getMessageId, MessageFile::getFileId)
                .containsExactly(savedExcludedMessageId, savedExcludedBinaryContentId);

        // 조회 결과는 3건이지만 DB 전체에는 제외 메시지의 1건까지 남아 있어야 한다.
        // findAllByMessageIdIn(...)이 조회 과정에서 데이터를 변경하지 않는다는 점도 보조적으로 확인한다.
        assertThat(messageFileRepository.count()).isEqualTo(4);
    }

    @Test
    @DisplayName("채널별 첨부 파일 목록 조회 성공 - 채널 메시지에 연결된 첨부 파일 반환")
    void findAllByChannelId_returnsMessageFiles_whenChannelHasMessageFiles() {
        // given
        // 이 테스트의 대상은 MessageFileRepository.findAllByChannelId(...) @Query 메서드다.
        // 특정 channelId에 속한 메시지들의 MessageFile만 반환하고,
        // Repository에 선언된 EntityGraph(binaryContent)에 의해 BinaryContent가 함께 로딩되어야 한다.
        // 단일 채널의 데이터만 저장하면 channel_id 조건이 정확히 적용되는지 확인하기 어렵다.
        // 그래서 대상 채널에는 메시지 2개와 파일 3건을 연결하고,
        // 다른 채널에는 메시지 1개와 파일 1건을 연결해 대조군으로 둔다.
        // Message는 author와 channel을 참조하므로 실제 User와 Channel을 먼저 저장한다.
        User savedAuthor = saveAuthor();
        Channel savedTargetChannel = savePublicChannel("testChannel");
        Channel savedOtherChannel = savePublicChannel("otherChannel");

        // firstTargetMessage와 secondTargetMessage는 조회 대상 채널에 속한 메시지다.
        // findAllByChannelId(targetChannelId)는 이 두 메시지에 연결된 MessageFile만 반환해야 한다.
        Message savedFirstTargetMessage = saveMessage(savedAuthor, savedTargetChannel, "firstTargetMessage");
        Message savedSecondTargetMessage = saveMessage(savedAuthor, savedTargetChannel, "secondTargetMessage");

        // otherChannelMessage는 다른 채널에 속한 대조군 메시지다.
        // 이 메시지에도 파일을 연결하지만 targetChannel 조회 결과에는 포함되면 안 된다.
        Message savedOtherChannelMessage = saveMessage(savedAuthor, savedOtherChannel, "otherChannelMessage");

        // 대상 채널 메시지들에 연결할 파일 3건과 다른 채널 메시지에 연결할 파일 1건을 저장한다.
        // 파일 메타데이터를 서로 다르게 두면 EntityGraph로 함께 조회된 BinaryContent가 정확한 row인지 검증하기 쉽다.
        BinaryContent savedFirstBinaryContent = saveBinaryContent("firstFile", "image/png", 1_000L);
        BinaryContent savedSecondBinaryContent = saveBinaryContent("secondFile", "image/jpeg", 2_000L);
        BinaryContent savedThirdBinaryContent = saveBinaryContent("thirdFile", "application/pdf", 3_000L);
        BinaryContent savedExcludedBinaryContent = saveBinaryContent("excludedFile", "text/plain", 4_000L);

        UUID savedAuthorId = savedAuthor.getId();
        UUID savedTargetChannelId = savedTargetChannel.getId();
        UUID savedOtherChannelId = savedOtherChannel.getId();
        UUID savedFirstTargetMessageId = savedFirstTargetMessage.getId();
        UUID savedSecondTargetMessageId = savedSecondTargetMessage.getId();
        UUID savedOtherChannelMessageId = savedOtherChannelMessage.getId();
        UUID savedFirstBinaryContentId = savedFirstBinaryContent.getId();
        UUID savedSecondBinaryContentId = savedSecondBinaryContent.getId();
        UUID savedThirdBinaryContentId = savedThirdBinaryContent.getId();
        UUID savedExcludedBinaryContentId = savedExcludedBinaryContent.getId();

        List<UUID> firstTargetFileIds = List.of(
                savedFirstBinaryContentId,
                savedSecondBinaryContentId
        );

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // id가 null이거나 대상/대조군 채널과 메시지가 서로 구분되지 않으면 channel_id 조건 검증이 의미 없어질 수 있다.
        assertThat(savedAuthorId).isNotNull();
        assertThat(savedTargetChannelId).isNotNull();
        assertThat(savedOtherChannelId).isNotNull();
        assertThat(savedFirstTargetMessageId).isNotNull();
        assertThat(savedSecondTargetMessageId).isNotNull();
        assertThat(savedOtherChannelMessageId).isNotNull();
        assertThat(savedFirstBinaryContentId).isNotNull();
        assertThat(savedSecondBinaryContentId).isNotNull();
        assertThat(savedThirdBinaryContentId).isNotNull();
        assertThat(savedExcludedBinaryContentId).isNotNull();
        assertThat(savedOtherChannelId).isNotEqualTo(savedTargetChannelId);
        assertThat(savedSecondTargetMessageId).isNotEqualTo(savedFirstTargetMessageId);
        assertThat(savedOtherChannelMessageId).isNotIn(savedFirstTargetMessageId, savedSecondTargetMessageId);
        assertThat(firstTargetFileIds).contains(savedFirstBinaryContentId, savedSecondBinaryContentId);
        assertThat(savedThirdBinaryContentId).isNotIn(firstTargetFileIds);
        assertThat(savedExcludedBinaryContentId).isNotIn(firstTargetFileIds);

        // 대상 채널의 첫 번째 메시지에는 파일 2건을 연결한다.
        int firstTargetSavedCount = messageFileRepository.bulkInsert(firstTargetFileIds, savedFirstTargetMessageId);

        // 대상 채널의 두 번째 메시지에는 파일 1건을 연결한다.
        int secondTargetSavedCount = messageFileRepository.bulkInsert(List.of(savedThirdBinaryContentId), savedSecondTargetMessageId);

        // 다른 채널의 메시지에도 파일 1건을 연결한다.
        // 이 row가 있어야 findAllByChannelId(targetChannelId)가 다른 채널의 MessageFile을 제외하는지 확인할 수 있다.
        int excludedSavedCount = messageFileRepository.bulkInsert(List.of(savedExcludedBinaryContentId), savedOtherChannelMessageId);

        assertThat(firstTargetSavedCount).isEqualTo(2);
        assertThat(secondTargetSavedCount).isEqualTo(1);
        assertThat(excludedSavedCount).isEqualTo(1);

        // native DML 이후에는 flush/clear로 DB 반영과 재조회 기준을 명확히 한다.
        // 아래 조회는 1차 캐시가 아니라 실제 message_files row를 기준으로 수행되어야 한다.
        em.flush();
        em.clear();

        // 전체 MessageFile은 대상 채널 3건 + 다른 채널 1건으로 총 4건이어야 한다.
        // 이 사전 검증이 있어야 대조군 row가 실제로 존재하는 상황에서 channel_id 조건이 적용되는지 확인할 수 있다.
        assertThat(messageFileRepository.count()).isEqualTo(4);

        // when
        // 대상 채널 id로 해당 채널 메시지들에 연결된 MessageFile 목록을 조회한다.
        List<MessageFile> messageFiles = messageFileRepository.findAllByChannelId(savedTargetChannelId);
        PersistenceUnitUtil persistenceUnitUtil = getPersistenceUnitUtil();

        // then
        // findAllByChannelId(...)의 EntityGraph는 binaryContent만 포함한다.
        // 따라서 조회 직후 BinaryContent가 이미 로딩되어 있는지 확인하고,
        // message 연관 로딩 여부는 이 테스트에서 고정하지 않는다.
        messageFiles.forEach(messageFile ->
                assertThat(persistenceUnitUtil.isLoaded(messageFile, "binaryContent")).isTrue()
        );

        // 대상 채널에 속한 두 메시지의 MessageFile 3건만 조회되어야 한다.
        // 순서는 Repository 계약이 아니므로 messageId, fileId 조합을 순서와 무관하게 검증한다.
        assertThat(messageFiles)
                .hasSize(3)
                .extracting(MessageFile::getMessageId, MessageFile::getFileId)
                .containsExactlyInAnyOrder(
                        tuple(savedFirstTargetMessageId, savedFirstBinaryContentId),
                        tuple(savedFirstTargetMessageId, savedSecondBinaryContentId),
                        tuple(savedSecondTargetMessageId, savedThirdBinaryContentId)
                );

        // BinaryContent가 함께 조회된 상태이므로 파일 엔티티의 실제 필드 값도 확인한다.
        assertThat(messageFiles)
                .extracting(
                        MessageFile::getMessageId,
                        messageFile -> messageFile.getBinaryContent().getId(),
                        messageFile -> messageFile.getBinaryContent().getOriginalFileName(),
                        messageFile -> messageFile.getBinaryContent().getContentType(),
                        messageFile -> messageFile.getBinaryContent().getSize()
                )
                .containsExactlyInAnyOrder(
                        tuple(savedFirstTargetMessageId, savedFirstBinaryContentId, "firstFile", "image/png", 1_000L),
                        tuple(savedFirstTargetMessageId, savedSecondBinaryContentId, "secondFile", "image/jpeg", 2_000L),
                        tuple(savedSecondTargetMessageId, savedThirdBinaryContentId, "thirdFile", "application/pdf", 3_000L)
                );

        // 다른 채널의 메시지에도 MessageFile이 실제로 존재하지만,
        // targetChannel 조회 결과에는 섞이면 안 된다.
        assertThat(messageFiles)
                .extracting(MessageFile::getMessageId)
                .containsOnly(savedFirstTargetMessageId, savedSecondTargetMessageId)
                .doesNotContain(savedOtherChannelMessageId);

        assertThat(messageFileRepository.findAllByMessage_Id(savedOtherChannelMessageId))
                .hasSize(1)
                .singleElement()
                .extracting(MessageFile::getMessageId, MessageFile::getFileId)
                .containsExactly(savedOtherChannelMessageId, savedExcludedBinaryContentId);

        // 조회 결과는 3건이지만 DB 전체에는 다른 채널의 1건까지 남아 있어야 한다.
        // findAllByChannelId(...)가 조회 과정에서 데이터를 변경하지 않는다는 점도 보조적으로 확인한다.
        assertThat(messageFileRepository.count()).isEqualTo(4);

    }

    @Test
    @DisplayName("채널별 첨부 파일 목록 조회 성공 - 첨부 파일이 없으면 빈 목록 반환")
    void findAllByChannelId_returnsEmptyList_whenChannelHasNoMessageFiles() {
        // given
        // 이 테스트의 대상은 MessageFileRepository.findAllByChannelId(...) @Query 메서드다.
        // 조회 대상 채널에 메시지는 존재하지만, 그 메시지들에는 MessageFile을 연결하지 않는다.
        // 이렇게 구성하면 "채널 자체가 없어서 빈 목록"인 경우가 아니라
        // "채널 메시지에 첨부 파일이 없어서 빈 목록"인 경우를 명확히 검증할 수 있다.
        //
        // 반대로 다른 채널에는 MessageFile을 실제로 저장해 둔다.
        // 이 대조군이 있어야 전체 DB가 비어 있어서 우연히 빈 목록이 반환되는 테스트가 되지 않는다.
        // Message는 author와 channel을 참조하므로 실제 User와 Channel을 먼저 저장한다.
        User savedAuthor = saveAuthor();
        Channel savedTargetChannel = savePublicChannel("targetChannel");
        Channel savedOtherChannel = savePublicChannel("otherChannel");

        // 조회 대상 채널에 메시지 2건을 만든다.
        // 이 메시지들에는 일부러 MessageFile을 연결하지 않는다.
        Message savedFirstTargetMessage = saveMessage(savedAuthor, savedTargetChannel, "firstTargetMessage");
        Message savedSecondTargetMessage = saveMessage(savedAuthor, savedTargetChannel, "secondTargetMessage");

        // 다른 채널에는 메시지 2건을 만들고, 여기에만 MessageFile을 연결한다.
        // 조회 대상 channelId가 정확히 적용된다면 이 MessageFile들은 결과에 포함되면 안 된다.
        Message savedFirstOtherChannelMessage = saveMessage(savedAuthor, savedOtherChannel, "firstOtherChannelMessage");
        Message savedSecondOtherChannelMessage = saveMessage(savedAuthor, savedOtherChannel, "secondOtherChannelMessage");

        // 대조군 채널 메시지에 연결할 파일 3건을 저장한다.
        // 대상 채널에는 이 파일들을 연결하지 않는다.
        BinaryContent savedFirstOtherBinaryContent = saveBinaryContent("otherFile1", "image/png", 1_000L);
        BinaryContent savedSecondOtherBinaryContent = saveBinaryContent("otherFile2", "image/jpeg", 2_000L);
        BinaryContent savedThirdOtherBinaryContent = saveBinaryContent("otherFile3", "application/pdf", 3_000L);

        UUID savedAuthorId = savedAuthor.getId();
        UUID savedTargetChannelId = savedTargetChannel.getId();
        UUID savedOtherChannelId = savedOtherChannel.getId();
        UUID savedFirstTargetMessageId = savedFirstTargetMessage.getId();
        UUID savedSecondTargetMessageId = savedSecondTargetMessage.getId();
        UUID savedFirstOtherChannelMessageId = savedFirstOtherChannelMessage.getId();
        UUID savedSecondOtherChannelMessageId = savedSecondOtherChannelMessage.getId();
        UUID savedFirstOtherBinaryContentId = savedFirstOtherBinaryContent.getId();
        UUID savedSecondOtherBinaryContentId = savedSecondOtherBinaryContent.getId();
        UUID savedThirdOtherBinaryContentId = savedThirdOtherBinaryContent.getId();

        List<UUID> firstOtherChannelFileIds = List.of(
                savedFirstOtherBinaryContentId,
                savedSecondOtherBinaryContentId
        );
        List<UUID> targetMessageIds = List.of(
                savedFirstTargetMessageId,
                savedSecondTargetMessageId
        );
        List<UUID> otherChannelMessageIds = List.of(
                savedFirstOtherChannelMessageId,
                savedSecondOtherChannelMessageId
        );

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // id가 null이거나 대상/대조군 채널과 메시지가 서로 구분되지 않으면 channel_id 조건 검증이 의미 없어질 수 있다.
        assertThat(savedAuthorId).isNotNull();
        assertThat(savedTargetChannelId).isNotNull();
        assertThat(savedOtherChannelId).isNotNull();
        assertThat(savedFirstTargetMessageId).isNotNull();
        assertThat(savedSecondTargetMessageId).isNotNull();
        assertThat(savedFirstOtherChannelMessageId).isNotNull();
        assertThat(savedSecondOtherChannelMessageId).isNotNull();
        assertThat(savedFirstOtherBinaryContentId).isNotNull();
        assertThat(savedSecondOtherBinaryContentId).isNotNull();
        assertThat(savedThirdOtherBinaryContentId).isNotNull();
        assertThat(savedOtherChannelId).isNotEqualTo(savedTargetChannelId);
        assertThat(savedSecondTargetMessageId).isNotEqualTo(savedFirstTargetMessageId);
        assertThat(otherChannelMessageIds).doesNotContainAnyElementsOf(targetMessageIds);
        assertThat(firstOtherChannelFileIds).contains(savedFirstOtherBinaryContentId, savedSecondOtherBinaryContentId);
        assertThat(savedThirdOtherBinaryContentId).isNotIn(firstOtherChannelFileIds);

        // Message 엔티티의 channel 연결도 확인한다.
        // 대상 채널 메시지와 대조군 채널 메시지가 뒤섞이면 빈 목록 검증의 의미가 약해진다.
        assertThat(savedFirstTargetMessage.getChannelId()).isEqualTo(savedTargetChannelId);
        assertThat(savedSecondTargetMessage.getChannelId()).isEqualTo(savedTargetChannelId);
        assertThat(savedFirstOtherChannelMessage.getChannelId()).isEqualTo(savedOtherChannelId);
        assertThat(savedSecondOtherChannelMessage.getChannelId()).isEqualTo(savedOtherChannelId);

        // 다른 채널의 첫 번째 메시지에는 파일 2건을 연결한다.
        int firstOtherChannelSavedCount = messageFileRepository.bulkInsert(firstOtherChannelFileIds, savedFirstOtherChannelMessageId);

        // 다른 채널의 두 번째 메시지에는 파일 1건을 연결한다.
        int secondOtherChannelSavedCount = messageFileRepository.bulkInsert(List.of(savedThirdOtherBinaryContentId), savedSecondOtherChannelMessageId);

        assertThat(firstOtherChannelSavedCount).isEqualTo(2);
        assertThat(secondOtherChannelSavedCount).isEqualTo(1);

        // native DML 이후에는 flush/clear로 DB 반영과 재조회 기준을 명확히 한다.
        // 아래 조회는 1차 캐시가 아니라 실제 message_files row를 기준으로 수행되어야 한다.
        em.flush();
        em.clear();

        // 전체 MessageFile은 대조군 채널에만 3건 존재해야 한다.
        // 이 사전 검증이 있어야 조회 대상 채널의 빈 결과가 "DB 전체가 비어서 빈 목록"이 아님을 확인할 수 있다.
        assertThat(messageFileRepository.count()).isEqualTo(3);

        assertThat(messageFileRepository.findAllByMessage_Id(savedFirstOtherChannelMessageId))
                .hasSize(2)
                .extracting(MessageFile::getMessageId, MessageFile::getFileId)
                .containsExactlyInAnyOrder(
                        tuple(savedFirstOtherChannelMessageId, savedFirstOtherBinaryContentId),
                        tuple(savedFirstOtherChannelMessageId, savedSecondOtherBinaryContentId)
                );

        assertThat(messageFileRepository.findAllByMessage_Id(savedSecondOtherChannelMessageId))
                .hasSize(1)
                .singleElement()
                .extracting(MessageFile::getMessageId, MessageFile::getFileId)
                .containsExactly(savedSecondOtherChannelMessageId, savedThirdOtherBinaryContentId);

        // when
        // MessageFile이 연결되지 않은 대상 채널 id로 첨부 파일 목록을 조회한다.
        List<MessageFile> messageFiles = messageFileRepository.findAllByChannelId(savedTargetChannelId);

        // then
        // 대상 채널에는 메시지가 존재하지만 MessageFile row가 없으므로 빈 목록을 반환해야 한다.
        // 이 검증은 findAllByChannelId(...)가 메시지 존재 여부가 아니라
        // message_files와 message.channel_id 조건을 기준으로 결과를 반환한다는 점을 확인한다.
        assertThat(messageFiles).isEmpty();

        // 조회는 읽기 작업이므로 대조군 채널의 MessageFile 3건은 그대로 남아 있어야 한다.
        assertThat(messageFileRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("첨부 파일 벌크 연결 성공 - 파일 ID 목록과 메시지 ID로 MessageFile 생성")
    void bulkInsert_insertsMessageFiles_whenFileIdsAndMessageIdExist() {
        // given
        // 이 테스트의 대상은 MessageFileRepository.bulkInsert(...) native DML 메서드다.
        // fileIds 목록과 messageId를 전달했을 때 message_files row가 한 번에 생성되는지 검증한다.
        //
        // 단순히 반환 count만 확인하면 실제 row의 message_id, file_id 연결이 올바른지 놓칠 수 있다.
        // 그래서 bulkInsert 실행 후 DB를 다시 조회해 MessageFile의 FK 조합과 BinaryContent 메타데이터까지 확인한다.
        //
        // 또한 bulkInsert 대상이 아닌 다른 메시지와 전달하지 않은 파일을 함께 준비한다.
        // 이 대조군이 있어야 bulkInsert가 입력으로 받은 messageId와 fileIds 범위 안에서만 row를 생성하는지 확인할 수 있다.
        // MessageFile은 Message와 BinaryContent를 연결하는 엔티티다.
        // 먼저 Message 생성에 필요한 author와 channel을 실제 엔티티로 저장한다.
        User savedAuthor = saveAuthor();
        Channel savedChannel = savePublicChannel("testChannel");

        // bulkInsert 대상 메시지다.
        // 아래 fileIdsToInsert에 포함된 파일들은 모두 이 메시지에 연결되어야 한다.
        Message savedTargetMessage = saveMessage(savedAuthor, savedChannel, "targetMessage");

        // bulkInsert 대상이 아닌 대조군 메시지다.
        // 같은 채널에 있어도 messageId를 전달하지 않았으므로 MessageFile이 생성되면 안 된다.
        Message savedOtherMessage = saveMessage(savedAuthor, savedChannel, "otherMessage");

        // bulkInsert 대상 파일 3건과 제외 파일 1건을 저장한다.
        // 제외 파일은 binary_contents에는 존재하지만 fileIdsToInsert에는 넣지 않아 MessageFile 생성 대상에서 제외되는지 확인한다.
        BinaryContent savedFirstBinaryContent = saveBinaryContent("firstFile", "image/png", 1_000L);
        BinaryContent savedSecondBinaryContent = saveBinaryContent("secondFile", "image/jpeg", 2_000L);
        BinaryContent savedThirdBinaryContent = saveBinaryContent("thirdFile", "application/pdf", 3_000L);
        BinaryContent savedExcludedBinaryContent = saveBinaryContent("excludedFile", "text/plain", 4_000L);

        UUID savedAuthorId = savedAuthor.getId();
        UUID savedChannelId = savedChannel.getId();
        UUID savedTargetMessageId = savedTargetMessage.getId();
        UUID savedOtherMessageId = savedOtherMessage.getId();
        UUID savedFirstBinaryContentId = savedFirstBinaryContent.getId();
        UUID savedSecondBinaryContentId = savedSecondBinaryContent.getId();
        UUID savedThirdBinaryContentId = savedThirdBinaryContent.getId();
        UUID savedExcludedBinaryContentId = savedExcludedBinaryContent.getId();

        List<UUID> fileIdsToInsert = List.of(
                savedFirstBinaryContentId,
                savedSecondBinaryContentId,
                savedThirdBinaryContentId
        );

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // id가 null이면 bulkInsert 실패인지 given 구성 실패인지 구분하기 어렵다.
        assertThat(savedAuthorId).isNotNull();
        assertThat(savedChannelId).isNotNull();
        assertThat(savedTargetMessageId).isNotNull();
        assertThat(savedOtherMessageId).isNotNull();
        assertThat(savedFirstBinaryContentId).isNotNull();
        assertThat(savedSecondBinaryContentId).isNotNull();
        assertThat(savedThirdBinaryContentId).isNotNull();
        assertThat(savedExcludedBinaryContentId).isNotNull();
        assertThat(savedOtherMessageId).isNotEqualTo(savedTargetMessageId);
        assertThat(fileIdsToInsert)
                .containsExactly(savedFirstBinaryContentId, savedSecondBinaryContentId, savedThirdBinaryContentId)
                .doesNotContain(savedExcludedBinaryContentId);

        // Message가 의도한 채널에 연결됐는지도 확인한다.
        // bulkInsert는 messageId만 FK로 사용하지만, 조회 검증에서 메시지 fixture가 뒤섞이면 원인 파악이 어려워진다.
        assertThat(savedTargetMessage.getChannelId()).isEqualTo(savedChannelId);
        assertThat(savedOtherMessage.getChannelId()).isEqualTo(savedChannelId);

        // bulkInsert 실행 전에는 MessageFile이 하나도 없어야 한다.
        // 이 사전 검증이 있어야 이후 count와 조회 결과가 이번 bulkInsert로 생긴 row임을 분명히 할 수 있다.
        assertThat(messageFileRepository.count()).isZero();
        assertThat(messageFileRepository.findAllByMessage_Id(savedTargetMessageId)).isEmpty();
        assertThat(messageFileRepository.findAllByMessage_Id(savedOtherMessageId)).isEmpty();

        // when
        // 존재하는 BinaryContent id 목록과 대상 Message id를 전달해 MessageFile을 벌크 생성한다.
        // Repository의 native query는 binary_contents에서 fileIds에 해당하는 row를 select한 뒤
        // message_files에 message_id, file_id 조합을 insert한다.
        int insertedRowsCount = messageFileRepository.bulkInsert(fileIdsToInsert, savedTargetMessageId);

        // then
        // 전달한 파일 id 3건이 모두 존재하므로 insert 영향 row 수도 3이어야 한다.
        assertThat(insertedRowsCount).isEqualTo(fileIdsToInsert.size());

        // native DML 이후에는 flush/clear로 DB 반영과 재조회 기준을 명확히 한다.
        // 아래 검증은 영속성 컨텍스트의 기존 객체가 아니라 실제 DB row를 다시 조회한 결과를 기준으로 한다.
        em.flush();
        em.clear();

        List<MessageFile> messageFilesForTargetMessage = messageFileRepository.findAllByMessage_Id(savedTargetMessageId);
        PersistenceUnitUtil persistenceUnitUtil = getPersistenceUnitUtil();

        // findAllByMessage_Id(...)는 EntityGraph(binaryContent)를 사용한다.
        // bulkInsert로 생성된 MessageFile도 조회 시 BinaryContent가 함께 로딩되는지 확인한다.
        messageFilesForTargetMessage.forEach(messageFile ->
                assertThat(persistenceUnitUtil.isLoaded(messageFile, "binaryContent")).isTrue()
        );

        // 대상 메시지에는 전달한 파일 ID 3건이 모두 연결되어야 한다.
        // bulkInsert 결과의 순서는 Repository 계약이 아니므로 messageId, fileId 조합을 순서와 무관하게 검증한다.
        assertThat(messageFilesForTargetMessage)
                .hasSize(3)
                .extracting(MessageFile::getMessageId, MessageFile::getFileId)
                .containsExactlyInAnyOrder(
                        tuple(savedTargetMessageId, savedFirstBinaryContentId),
                        tuple(savedTargetMessageId, savedSecondBinaryContentId),
                        tuple(savedTargetMessageId, savedThirdBinaryContentId)
                );

        // FK 조합뿐 아니라 함께 조회된 BinaryContent의 실제 메타데이터도 검증한다.
        // 이 검증은 file_id가 의도한 BinaryContent row를 가리키는지까지 확인한다.
        assertThat(messageFilesForTargetMessage)
                .extracting(
                        MessageFile::getMessageId,
                        messageFile -> messageFile.getBinaryContent().getId(),
                        messageFile -> messageFile.getBinaryContent().getOriginalFileName(),
                        messageFile -> messageFile.getBinaryContent().getContentType(),
                        messageFile -> messageFile.getBinaryContent().getSize()
                )
                .containsExactlyInAnyOrder(
                        tuple(savedTargetMessageId, savedFirstBinaryContentId, "firstFile", "image/png", 1_000L),
                        tuple(savedTargetMessageId, savedSecondBinaryContentId, "secondFile", "image/jpeg", 2_000L),
                        tuple(savedTargetMessageId, savedThirdBinaryContentId, "thirdFile", "application/pdf", 3_000L)
                );

        // fileIdsToInsert에 넣지 않은 파일은 MessageFile로 생성되면 안 된다.
        assertThat(messageFilesForTargetMessage)
                .extracting(MessageFile::getFileId)
                .doesNotContain(savedExcludedBinaryContentId);

        // 다른 메시지는 같은 채널에 존재하더라도 bulkInsert의 messageId 대상이 아니므로 첨부 파일이 없어야 한다.
        assertThat(messageFileRepository.findAllByMessage_Id(savedOtherMessageId)).isEmpty();

        // 최종 MessageFile 개수는 이번 bulkInsert로 생성된 3건뿐이어야 한다.
        assertThat(messageFileRepository.count()).isEqualTo(3);

    }

    @Test
    @DisplayName("첨부 파일 벌크 연결 성공 - 존재하지 않는 파일 ID는 생성 대상에서 제외")
    void bulkInsert_insertsOnlyExistingFiles_whenSomeFileIdsDoNotExist() {
        // given
        // 이 테스트의 대상은 MessageFileRepository.bulkInsert(...) native DML 메서드다.
        // bulkInsert는 전달받은 fileIds를 그대로 message_files에 넣는 방식이 아니라,
        // binary_contents 테이블에서 실제 존재하는 id만 select한 뒤 MessageFile row를 생성한다.
        //
        // 따라서 fileIds 안에 존재하지 않는 UUID가 섞여 있어도 예외가 발생하는 것이 아니라
        // DB에 존재하는 BinaryContent id만 생성 대상이 되어야 한다.
        // 이 테스트는 그 동작을 반환 row count와 실제 재조회 결과로 함께 검증한다.
        // MessageFile은 Message와 BinaryContent를 연결하는 엔티티다.
        // 먼저 Message 생성에 필요한 author와 channel을 실제 엔티티로 저장한다.
        User savedAuthor = saveAuthor();
        Channel savedChannel = savePublicChannel("testChannel");

        // bulkInsert 대상 메시지다.
        // 존재하는 파일 id 3건만 이 메시지에 연결되어야 하고, 존재하지 않는 파일 id는 무시되어야 한다.
        Message savedTargetMessage = saveMessage(savedAuthor, savedChannel, "targetMessage");

        // 대조군 메시지다.
        // 같은 채널에 있어도 bulkInsert에 이 messageId를 전달하지 않았으므로 MessageFile이 생성되면 안 된다.
        Message savedOtherMessage = saveMessage(savedAuthor, savedChannel, "otherMessage");

        // 존재하는 파일 3건은 bulkInsert 입력에 포함한다.
        BinaryContent savedFirstBinaryContent = saveBinaryContent("firstFile", "image/png", 1_000L);
        BinaryContent savedSecondBinaryContent = saveBinaryContent("secondFile", "image/jpeg", 2_000L);
        BinaryContent savedThirdBinaryContent = saveBinaryContent("thirdFile", "application/pdf", 3_000L);

        // 존재하지만 입력 목록에는 넣지 않는 파일이다.
        // 이 파일이 MessageFile로 생성되지 않아야 fileIds 조건이 정확히 적용된 것이다.
        BinaryContent savedExcludedBinaryContent = saveBinaryContent("excludedFile", "text/plain", 4_000L);

        UUID savedAuthorId = savedAuthor.getId();
        UUID savedChannelId = savedChannel.getId();
        UUID savedTargetMessageId = savedTargetMessage.getId();
        UUID savedOtherMessageId = savedOtherMessage.getId();
        UUID savedFirstBinaryContentId = savedFirstBinaryContent.getId();
        UUID savedSecondBinaryContentId = savedSecondBinaryContent.getId();
        UUID savedThirdBinaryContentId = savedThirdBinaryContent.getId();

        // binary_contents에 저장하지 않은 임의의 UUID다.
        // bulkInsert 입력에는 포함하지만, native query의 select 대상에는 걸리지 않아야 한다.
        UUID notSavedBinaryContentId = UUID.randomUUID();
        UUID savedExcludedBinaryContentId = savedExcludedBinaryContent.getId();

        // 입력 목록에는 존재하는 파일 3건과 존재하지 않는 파일 id 1건을 함께 넣는다.
        // 기대 결과는 전체 4건 생성이 아니라, 실제 binary_contents에 존재하는 3건 생성이다.
        List<UUID> fileIdsToInsert = List.of(
                savedFirstBinaryContentId,
                savedSecondBinaryContentId,
                savedThirdBinaryContentId,
                notSavedBinaryContentId
        );

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // id가 null이면 bulkInsert 실패인지 given 구성 실패인지 구분하기 어렵다.
        assertThat(savedAuthorId).isNotNull();
        assertThat(savedChannelId).isNotNull();
        assertThat(savedTargetMessageId).isNotNull();
        assertThat(savedOtherMessageId).isNotNull();
        assertThat(savedFirstBinaryContentId).isNotNull();
        assertThat(savedSecondBinaryContentId).isNotNull();
        assertThat(savedThirdBinaryContentId).isNotNull();
        assertThat(notSavedBinaryContentId).isNotNull();
        assertThat(savedExcludedBinaryContentId).isNotNull();
        assertThat(savedOtherMessageId).isNotEqualTo(savedTargetMessageId);
        assertThat(fileIdsToInsert)
                .containsExactly(savedFirstBinaryContentId, savedSecondBinaryContentId, savedThirdBinaryContentId, notSavedBinaryContentId)
                .doesNotContain(savedExcludedBinaryContentId);

        // notSavedBinaryContentId는 실제 저장된 BinaryContent id들과 달라야 한다.
        // 이 검증이 있어야 "존재하지 않는 파일 ID"라는 fixture 의도가 분명해진다.
        assertThat(notSavedBinaryContentId)
                .isNotIn(
                        savedFirstBinaryContentId,
                        savedSecondBinaryContentId,
                        savedThirdBinaryContentId,
                        savedExcludedBinaryContentId
                );

        // Message가 의도한 채널에 연결됐는지도 확인한다.
        // bulkInsert는 messageId만 FK로 사용하지만, 조회 검증에서 메시지 fixture가 뒤섞이면 원인 파악이 어려워진다.
        assertThat(savedTargetMessage.getChannelId()).isEqualTo(savedChannelId);
        assertThat(savedOtherMessage.getChannelId()).isEqualTo(savedChannelId);

        // bulkInsert 실행 전에는 MessageFile이 하나도 없어야 한다.
        // 이 사전 검증이 있어야 이후 count와 조회 결과가 이번 bulkInsert로 생긴 row임을 분명히 할 수 있다.
        assertThat(messageFileRepository.count()).isZero();
        assertThat(messageFileRepository.findAllByMessage_Id(savedTargetMessageId)).isEmpty();
        assertThat(messageFileRepository.findAllByMessage_Id(savedOtherMessageId)).isEmpty();

        // when
        // 존재하는 파일 id 3건과 존재하지 않는 파일 id 1건을 함께 전달한다.
        // Repository의 native query는 binary_contents에서 일치하는 row만 select하므로,
        // 존재하지 않는 fileId는 insert 대상에서 자연스럽게 제외되어야 한다.
        int insertedRowsCount = messageFileRepository.bulkInsert(fileIdsToInsert, savedTargetMessageId);

        // then
        // 입력 fileIds는 4건이지만 실제 존재하는 BinaryContent는 3건이다.
        // 따라서 insert 영향 row 수도 3이어야 한다.
        assertThat(insertedRowsCount)
                .isEqualTo(3)
                .isEqualTo(fileIdsToInsert.size() - 1);

        // native DML 이후에는 flush/clear로 DB 반영과 재조회 기준을 명확히 한다.
        // 아래 검증은 영속성 컨텍스트의 기존 객체가 아니라 실제 DB row를 다시 조회한 결과를 기준으로 한다.
        em.flush();
        em.clear();

        List<MessageFile> messageFilesForTargetMessage = messageFileRepository.findAllByMessage_Id(savedTargetMessageId);
        PersistenceUnitUtil persistenceUnitUtil = getPersistenceUnitUtil();

        // findAllByMessage_Id(...)는 EntityGraph(binaryContent)를 사용한다.
        // bulkInsert로 생성된 MessageFile도 조회 시 BinaryContent가 함께 로딩되는지 확인한다.
        messageFilesForTargetMessage.forEach(messageFile ->
                assertThat(persistenceUnitUtil.isLoaded(messageFile, "binaryContent")).isTrue()
        );

        // 대상 메시지에는 실제 존재한 파일 ID 3건만 연결되어야 한다.
        // 존재하지 않는 fileId는 message_files row로 생성되면 안 된다.
        // bulkInsert 결과의 순서는 Repository 계약이 아니므로 messageId, fileId 조합을 순서와 무관하게 검증한다.
        assertThat(messageFilesForTargetMessage)
                .hasSize(3)
                .extracting(MessageFile::getMessageId, MessageFile::getFileId)
                .containsExactlyInAnyOrder(
                        tuple(savedTargetMessageId, savedFirstBinaryContentId),
                        tuple(savedTargetMessageId, savedSecondBinaryContentId),
                        tuple(savedTargetMessageId, savedThirdBinaryContentId)
                )
                .doesNotContain(tuple(savedTargetMessageId, notSavedBinaryContentId));

        // FK 조합뿐 아니라 함께 조회된 BinaryContent의 실제 메타데이터도 검증한다.
        // 이 검증은 생성된 MessageFile이 의도한 BinaryContent row를 가리키는지까지 확인한다.
        assertThat(messageFilesForTargetMessage)
                .extracting(
                        MessageFile::getMessageId,
                        messageFile -> messageFile.getBinaryContent().getId(),
                        messageFile -> messageFile.getBinaryContent().getOriginalFileName(),
                        messageFile -> messageFile.getBinaryContent().getContentType(),
                        messageFile -> messageFile.getBinaryContent().getSize()
                )
                .containsExactlyInAnyOrder(
                        tuple(savedTargetMessageId, savedFirstBinaryContentId, "firstFile", "image/png", 1_000L),
                        tuple(savedTargetMessageId, savedSecondBinaryContentId, "secondFile", "image/jpeg", 2_000L),
                        tuple(savedTargetMessageId, savedThirdBinaryContentId, "thirdFile", "application/pdf", 3_000L)
                );

        // 존재하지만 입력 목록에 없는 파일과, 입력에는 있지만 DB에 없는 파일은 모두 생성 결과에서 제외되어야 한다.
        assertThat(messageFilesForTargetMessage)
                .extracting(MessageFile::getFileId)
                .doesNotContain(savedExcludedBinaryContentId, notSavedBinaryContentId);

        // 다른 메시지는 같은 채널에 존재하더라도 bulkInsert의 messageId 대상이 아니므로 첨부 파일이 없어야 한다.
        assertThat(messageFileRepository.findAllByMessage_Id(savedOtherMessageId)).isEmpty();

        // 최종 MessageFile 개수는 실제 존재한 파일 id 3건으로 생성된 row뿐이어야 한다.
        assertThat(messageFileRepository.count()).isEqualTo(3);

    }

    @Test
    @DisplayName("채널 기준 첨부 파일 존재 여부 조회 성공 - 채널 메시지에 첨부 파일이 있으면 true 반환")
    void existsByMessage_Channel_Id_returnsTrue_whenChannelHasMessageFiles() {
        // given
        // 이 테스트의 대상은 MessageFileRepository.existsByMessage_Channel_Id(...) derived query 메서드다.
        // MessageFile 자체에는 channel_id 컬럼이 없고, MessageFile -> Message -> Channel 연관관계를 따라가야 한다.
        // 따라서 "메시지에 파일이 있는가"가 아니라 "해당 채널의 메시지들 중 파일이 연결된 메시지가 하나라도 있는가"를 검증한다.
        //
        // 대상 채널에는 메시지 2건을 만들고, 그중 targetMessage에만 파일 3건을 연결한다.
        // otherMessage는 같은 채널에 있지만 파일을 연결하지 않는다.
        // 이 구성을 통해 채널 안의 모든 메시지가 파일을 가져야 true가 아니라,
        // 채널 안에 MessageFile이 하나 이상 존재하면 true라는 Repository 계약을 확인한다.
        // MessageFile은 Message를 통해 Channel을 판단하므로 실제 User, Channel, Message를 저장한다.
        User savedAuthor = saveAuthor();
        Channel savedChannel = savePublicChannel("testChannel");

        // 파일을 연결할 대상 메시지다.
        // 이 메시지에 MessageFile이 생성되면 existsByMessage_Channel_Id(channelId)는 true를 반환해야 한다.
        Message savedTargetMessage = saveMessage(savedAuthor, savedChannel, "targetMessage");

        // 같은 채널에 속하지만 파일이 없는 대조군 메시지다.
        // 이 메시지가 비어 있어도 채널 전체 기준으로는 targetMessage의 파일 때문에 true가 되어야 한다.
        Message savedOtherMessage = saveMessage(savedAuthor, savedChannel, "otherMessage");

        // targetMessage에 연결할 파일 3건을 저장한다.
        BinaryContent savedFirstBinaryContent = saveBinaryContent("firstFile", "image/png", 1_000L);
        BinaryContent savedSecondBinaryContent = saveBinaryContent("secondFile", "image/jpeg", 2_000L);
        BinaryContent savedThirdBinaryContent = saveBinaryContent("thirdFile", "application/pdf", 3_000L);

        // 존재하지만 bulkInsert 입력에는 넣지 않는 파일이다.
        // 이 파일이 MessageFile로 생성되지 않아야 fileIds 조건이 정확히 적용된 것이다.
        BinaryContent savedExcludedBinaryContent = saveBinaryContent("excludedFile", "text/plain", 4_000L);

        UUID savedAuthorId = savedAuthor.getId();
        UUID savedChannelId = savedChannel.getId();
        UUID savedTargetMessageId = savedTargetMessage.getId();
        UUID savedOtherMessageId = savedOtherMessage.getId();
        UUID savedFirstBinaryContentId = savedFirstBinaryContent.getId();
        UUID savedSecondBinaryContentId = savedSecondBinaryContent.getId();
        UUID savedThirdBinaryContentId = savedThirdBinaryContent.getId();
        UUID savedExcludedBinaryContentId = savedExcludedBinaryContent.getId();

        // bulkInsert 대상 파일 목록이다.
        // savedExcludedBinaryContentId는 실제 파일로 저장되어 있지만 입력 목록에는 포함하지 않는다.
        List<UUID> fileIdsToInsert = List.of(
                savedFirstBinaryContentId,
                savedSecondBinaryContentId,
                savedThirdBinaryContentId
        );

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // id가 null이면 exists 쿼리 실패인지 given 구성 실패인지 구분하기 어렵다.
        assertThat(savedAuthorId).isNotNull();
        assertThat(savedChannelId).isNotNull();
        assertThat(savedTargetMessageId).isNotNull();
        assertThat(savedOtherMessageId).isNotNull();
        assertThat(savedFirstBinaryContentId).isNotNull();
        assertThat(savedSecondBinaryContentId).isNotNull();
        assertThat(savedThirdBinaryContentId).isNotNull();
        assertThat(savedExcludedBinaryContentId).isNotNull();
        assertThat(savedOtherMessageId).isNotEqualTo(savedTargetMessageId);
        assertThat(fileIdsToInsert)
                .containsExactly(savedFirstBinaryContentId, savedSecondBinaryContentId, savedThirdBinaryContentId)
                .doesNotContain(savedExcludedBinaryContentId);

        // 두 메시지가 모두 조회 대상 채널에 속해 있는지 확인한다.
        // 이 fixture가 어긋나면 채널 기준 exists 검증의 의미가 사라진다.
        assertThat(savedTargetMessage.getChannelId()).isEqualTo(savedChannelId);
        assertThat(savedOtherMessage.getChannelId()).isEqualTo(savedChannelId);

        // bulkInsert 실행 전에는 MessageFile이 하나도 없어야 한다.
        // 이 사전 검증이 있어야 이후 true 결과가 이번 bulkInsert로 생성한 row 때문임을 분명히 할 수 있다.
        assertThat(messageFileRepository.count()).isZero();
        assertThat(messageFileRepository.findAllByMessage_Id(savedTargetMessageId)).isEmpty();
        assertThat(messageFileRepository.findAllByMessage_Id(savedOtherMessageId)).isEmpty();

        // 대상 메시지에 파일 3건을 연결한다.
        // existsByMessage_Channel_Id(...)는 이 MessageFile들의 message.channel.id를 기준으로 true를 반환해야 한다.
        int insertedRowsCount = messageFileRepository.bulkInsert(fileIdsToInsert, savedTargetMessageId);

        assertThat(insertedRowsCount).isEqualTo(3);

        // native DML 이후에는 flush/clear로 DB 반영과 재조회 기준을 명확히 한다.
        // 아래 exists 쿼리는 1차 캐시가 아니라 실제 message_files row를 기준으로 수행되어야 한다.
        em.flush();
        em.clear();

        // exists 검증 전에 대상 채널에 MessageFile이 실제로 존재하는지 확인한다.
        // 이 검증이 있어야 true 결과가 우연한 fixture 누락이 아니라 실제 저장 데이터에서 나온 것임을 확인할 수 있다.
        assertThat(messageFileRepository.findAllByChannelId(savedChannelId))
                .hasSize(3)
                .extracting(MessageFile::getMessageId, MessageFile::getFileId)
                .containsExactlyInAnyOrder(
                        tuple(savedTargetMessageId, savedFirstBinaryContentId),
                        tuple(savedTargetMessageId, savedSecondBinaryContentId),
                        tuple(savedTargetMessageId, savedThirdBinaryContentId)
                );

        // 같은 채널의 otherMessage에는 파일을 연결하지 않았다.
        // 그래도 채널 전체에는 targetMessage의 파일이 있으므로 existsByMessage_Channel_Id(channelId)는 true여야 한다.
        assertThat(messageFileRepository.findAllByMessage_Id(savedOtherMessageId)).isEmpty();

        // when
        // 채널 id를 기준으로 해당 채널 메시지들의 첨부 파일 존재 여부를 조회한다.
        boolean exists = messageFileRepository.existsByMessage_Channel_Id(savedChannelId);

        // then
        // 대상 채널의 메시지 중 하나라도 MessageFile을 가지고 있으므로 true를 반환해야 한다.
        assertThat(exists).isTrue();

        // 조회는 읽기 작업이므로 저장된 MessageFile 수는 그대로 3건이어야 한다.
        assertThat(messageFileRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("채널 기준 첨부 파일 존재 여부 조회 성공 - 채널 메시지에 첨부 파일이 없으면 false 반환")
    void existsByMessage_Channel_Id_returnsFalse_whenChannelHasNoMessageFiles() {
        // given
        // 이 테스트의 대상은 MessageFileRepository.existsByMessage_Channel_Id(...) derived query 메서드다.
        // MessageFile은 channel_id를 직접 들고 있지 않으므로,
        // MessageFile -> Message -> Channel 연관관계를 따라 해당 채널의 첨부 파일 존재 여부를 판단해야 한다.
        //
        // 조회 대상 채널에는 메시지를 실제로 저장하지만 MessageFile은 연결하지 않는다.
        // 이렇게 해야 "채널이 없어서 false"가 아니라
        // "채널 메시지에 첨부 파일이 없어서 false"인 경우를 검증할 수 있다.
        //
        // 반대로 다른 채널에는 MessageFile을 실제로 저장해 둔다.
        // 이 대조군이 있어야 DB 전체가 비어 있어서 우연히 false가 되는 테스트가 되지 않는다.
        // MessageFile 존재 여부는 Message의 channel을 기준으로 판단하므로 실제 User와 Channel을 먼저 저장한다.
        User savedAuthor = saveAuthor();
        Channel savedChannelWithoutMessageFiles = savePublicChannel("channelWithoutMessageFiles");
        Channel savedChannelWithMessageFiles = savePublicChannel("channelWithMessageFiles");

        // 조회 대상 채널에 속한 메시지다.
        // 이 메시지는 실제로 존재하지만 첨부 파일을 연결하지 않는다.
        Message savedMessageWithoutFiles = saveMessage(savedAuthor, savedChannelWithoutMessageFiles, "messageWithoutFiles");

        // 다른 채널에 속한 메시지다.
        // 여기에만 첨부 파일을 연결해 exists 쿼리가 channel_id 조건을 정확히 적용하는지 확인한다.
        Message savedMessageWithFiles = saveMessage(savedAuthor, savedChannelWithMessageFiles, "messageWithFiles");

        // 대조군 채널 메시지에 연결할 파일 3건을 저장한다.
        BinaryContent savedFirstBinaryContent = saveBinaryContent("firstFile", "image/png", 1_000L);
        BinaryContent savedSecondBinaryContent = saveBinaryContent("secondFile", "image/jpeg", 2_000L);
        BinaryContent savedThirdBinaryContent = saveBinaryContent("thirdFile", "application/pdf", 3_000L);

        // 존재하지만 bulkInsert 입력에는 넣지 않는 파일이다.
        // 이 파일이 MessageFile로 생성되지 않아야 fileIds 조건이 정확히 적용된 것이다.
        BinaryContent savedExcludedBinaryContent = saveBinaryContent("excludedFile", "text/plain", 4_000L);

        UUID savedAuthorId = savedAuthor.getId();
        UUID savedChannelWithoutMessageFilesId = savedChannelWithoutMessageFiles.getId();
        UUID savedChannelWithMessageFilesId = savedChannelWithMessageFiles.getId();
        UUID savedMessageWithoutFilesId = savedMessageWithoutFiles.getId();
        UUID savedMessageWithFilesId = savedMessageWithFiles.getId();
        UUID savedFirstBinaryContentId = savedFirstBinaryContent.getId();
        UUID savedSecondBinaryContentId = savedSecondBinaryContent.getId();
        UUID savedThirdBinaryContentId = savedThirdBinaryContent.getId();
        UUID savedExcludedBinaryContentId = savedExcludedBinaryContent.getId();

        // bulkInsert 대상 파일 목록이다.
        // savedExcludedBinaryContentId는 실제 파일로 저장되어 있지만 입력 목록에는 포함하지 않는다.
        List<UUID> fileIdsToInsert = List.of(
                savedFirstBinaryContentId,
                savedSecondBinaryContentId,
                savedThirdBinaryContentId
        );

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // id가 null이거나 조회 대상/대조군 채널이 구분되지 않으면 false 검증의 의미가 약해진다.
        assertThat(savedAuthorId).isNotNull();
        assertThat(savedChannelWithoutMessageFilesId).isNotNull();
        assertThat(savedChannelWithMessageFilesId).isNotNull();
        assertThat(savedMessageWithoutFilesId).isNotNull();
        assertThat(savedMessageWithFilesId).isNotNull();
        assertThat(savedFirstBinaryContentId).isNotNull();
        assertThat(savedSecondBinaryContentId).isNotNull();
        assertThat(savedThirdBinaryContentId).isNotNull();
        assertThat(savedExcludedBinaryContentId).isNotNull();
        assertThat(savedChannelWithMessageFilesId).isNotEqualTo(savedChannelWithoutMessageFilesId);
        assertThat(savedMessageWithFilesId).isNotEqualTo(savedMessageWithoutFilesId);
        assertThat(fileIdsToInsert)
                .containsExactly(savedFirstBinaryContentId, savedSecondBinaryContentId, savedThirdBinaryContentId)
                .doesNotContain(savedExcludedBinaryContentId);

        // 각 메시지가 의도한 채널에 연결됐는지 확인한다.
        // 이 fixture가 어긋나면 existsByMessage_Channel_Id(...)의 channel 조건 검증이 부정확해진다.
        assertThat(savedMessageWithoutFiles.getChannelId()).isEqualTo(savedChannelWithoutMessageFilesId);
        assertThat(savedMessageWithFiles.getChannelId()).isEqualTo(savedChannelWithMessageFilesId);

        // bulkInsert 실행 전에는 MessageFile이 하나도 없어야 한다.
        // 이 사전 검증이 있어야 이후 count와 조회 결과가 이번 bulkInsert로 생긴 row임을 분명히 할 수 있다.
        assertThat(messageFileRepository.count()).isZero();
        assertThat(messageFileRepository.findAllByMessage_Id(savedMessageWithoutFilesId)).isEmpty();
        assertThat(messageFileRepository.findAllByMessage_Id(savedMessageWithFilesId)).isEmpty();
        assertThat(messageFileRepository.findAllByChannelId(savedChannelWithoutMessageFilesId)).isEmpty();
        assertThat(messageFileRepository.findAllByChannelId(savedChannelWithMessageFilesId)).isEmpty();

        // 대조군 채널의 메시지에만 파일 3건을 연결한다.
        // 조회 대상 채널에는 MessageFile을 하나도 만들지 않는다.
        int insertedRowsCount = messageFileRepository.bulkInsert(fileIdsToInsert, savedMessageWithFilesId);

        assertThat(insertedRowsCount).isEqualTo(3);

        // native DML 이후에는 flush/clear로 DB 반영과 재조회 기준을 명확히 한다.
        // 아래 exists 쿼리는 1차 캐시가 아니라 실제 message_files row를 기준으로 수행되어야 한다.
        em.flush();
        em.clear();

        // 대조군 채널에는 MessageFile이 실제로 3건 존재해야 한다.
        // 이 검증이 있어야 DB 전체가 비어 있는 상태에서 false가 나온 것이 아님을 확인할 수 있다.
        assertThat(messageFileRepository.findAllByChannelId(savedChannelWithMessageFilesId))
                .hasSize(3)
                .extracting(MessageFile::getMessageId, MessageFile::getFileId)
                .containsExactlyInAnyOrder(
                        tuple(savedMessageWithFilesId, savedFirstBinaryContentId),
                        tuple(savedMessageWithFilesId, savedSecondBinaryContentId),
                        tuple(savedMessageWithFilesId, savedThirdBinaryContentId)
                );

        // 조회 대상 채널에는 메시지가 존재하지만 MessageFile은 없어야 한다.
        // 이 상태에서 existsByMessage_Channel_Id(channelWithoutMessageFilesId)는 false를 반환해야 한다.
        assertThat(messageFileRepository.findAllByChannelId(savedChannelWithoutMessageFilesId)).isEmpty();
        assertThat(messageFileRepository.findAllByMessage_Id(savedMessageWithoutFilesId)).isEmpty();

        // when
        // MessageFile이 없는 채널 id로 첨부 파일 존재 여부를 조회한다.
        boolean exists = messageFileRepository.existsByMessage_Channel_Id(savedChannelWithoutMessageFilesId);

        // then
        // 조회 대상 채널의 메시지에는 첨부 파일이 없으므로 false를 반환해야 한다.
        assertThat(exists).isFalse();

        // 조회는 읽기 작업이므로 대조군 채널의 MessageFile 3건은 그대로 남아 있어야 한다.
        assertThat(messageFileRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("첨부 파일 목록 삭제 성공 - ID 목록에 포함된 MessageFile 삭제")
    void deleteAllByIdIn_deletesMessageFilesMatchingIds() {
        // given
        // 이 테스트의 대상은 MessageFileRepository.deleteAllByIdIn(...) derived delete query다.
        // 메서드 이름처럼 message_id가 아니라 MessageFile의 id 목록을 기준으로 삭제해야 한다.
        //
        // 삭제 대상 MessageFile 2건과 삭제 대상이 아닌 MessageFile 1건을 함께 저장한다.
        // 이렇게 하면 deleteAllByIdIn(ids)가 전달받은 id에 해당하는 row만 삭제하고,
        // 목록에 포함되지 않은 row는 그대로 남기는지 검증할 수 있다.
        // MessageFile은 Message와 BinaryContent를 연결하는 엔티티다.
        // 먼저 Message 생성에 필요한 author와 channel을 실제 엔티티로 저장한다.
        User savedAuthor = saveAuthor();
        Channel savedChannel = savePublicChannel("testChannel");

        // 삭제 대상 MessageFile 2건을 연결할 메시지다.
        Message savedMessageWithFilesToDelete = saveMessage(savedAuthor, savedChannel, "messageWithFilesToDelete");

        // 삭제 대상이 아닌 MessageFile 1건을 연결할 메시지다.
        // 이 row가 삭제 후에도 남아 있어야 id 조건이 정확히 적용됐음을 확인할 수 있다.
        Message savedMessageWithRemainingFile = saveMessage(savedAuthor, savedChannel, "messageWithRemainingFile");

        // 삭제 대상 메시지에 연결할 파일 2건과 남겨 둘 파일 1건을 저장한다.
        BinaryContent savedFirstBinaryContent = saveBinaryContent("firstFile", "image/png", 1_000L);
        BinaryContent savedSecondBinaryContent = saveBinaryContent("secondFile", "image/jpeg", 2_000L);
        BinaryContent savedThirdBinaryContent = saveBinaryContent("thirdFile", "application/pdf", 3_000L);

        UUID savedAuthorId = savedAuthor.getId();
        UUID savedChannelId = savedChannel.getId();
        UUID savedMessageWithFilesToDeleteId = savedMessageWithFilesToDelete.getId();
        UUID savedMessageWithRemainingFileId = savedMessageWithRemainingFile.getId();
        UUID savedFirstBinaryContentId = savedFirstBinaryContent.getId();
        UUID savedSecondBinaryContentId = savedSecondBinaryContent.getId();
        UUID savedThirdBinaryContentId = savedThirdBinaryContent.getId();

        List<UUID> fileIdsToDeleteLater = List.of(
                savedFirstBinaryContentId,
                savedSecondBinaryContentId
        );

        // 테스트 fixture가 의도대로 저장됐는지 확인한다.
        // id가 null이거나 삭제 대상/비대상 메시지가 구분되지 않으면 삭제 조건 검증이 의미 없어질 수 있다.
        assertThat(savedAuthorId).isNotNull();
        assertThat(savedChannelId).isNotNull();
        assertThat(savedMessageWithFilesToDeleteId).isNotNull();
        assertThat(savedMessageWithRemainingFileId).isNotNull();
        assertThat(savedFirstBinaryContentId).isNotNull();
        assertThat(savedSecondBinaryContentId).isNotNull();
        assertThat(savedThirdBinaryContentId).isNotNull();
        assertThat(savedMessageWithRemainingFileId).isNotEqualTo(savedMessageWithFilesToDeleteId);
        assertThat(fileIdsToDeleteLater)
                .containsExactly(savedFirstBinaryContentId, savedSecondBinaryContentId)
                .doesNotContain(savedThirdBinaryContentId);

        // 두 메시지가 같은 채널에 있더라도 deleteAllByIdIn(...)는 message_id가 아니라 MessageFile id로 삭제해야 한다.
        // 같은 채널에 배치하면 채널 조건으로 잘못 삭제되는 구현을 더 쉽게 드러낼 수 있다.
        assertThat(savedMessageWithFilesToDelete.getChannelId()).isEqualTo(savedChannelId);
        assertThat(savedMessageWithRemainingFile.getChannelId()).isEqualTo(savedChannelId);

        // bulkInsert 실행 전에는 MessageFile이 하나도 없어야 한다.
        // 이 사전 검증이 있어야 이후 count와 조회 결과가 이번 bulkInsert로 생긴 row임을 분명히 할 수 있다.
        assertThat(messageFileRepository.count()).isZero();
        assertThat(messageFileRepository.findAllByMessage_Id(savedMessageWithFilesToDeleteId)).isEmpty();
        assertThat(messageFileRepository.findAllByMessage_Id(savedMessageWithRemainingFileId)).isEmpty();

        // 삭제 대상 메시지에는 파일 2건을 연결한다.
        int insertedRowsCount = messageFileRepository.bulkInsert(fileIdsToDeleteLater, savedMessageWithFilesToDeleteId);

        // 비대상 메시지에는 파일 1건을 연결한다.
        // 이 MessageFile은 deleteAllByIdIn(...)에 전달하지 않을 것이므로 삭제 후에도 남아야 한다.
        int insertedRemainingRowsCount = messageFileRepository.bulkInsert(List.of(savedThirdBinaryContentId), savedMessageWithRemainingFileId);

        assertThat(insertedRowsCount).isEqualTo(2);
        assertThat(insertedRemainingRowsCount).isEqualTo(1);

        // native DML 이후에는 flush/clear로 DB 반영과 재조회 기준을 명확히 한다.
        // deleteAllByIdIn(...)에 전달할 MessageFile id는 DB 재조회 결과에서 얻어야 한다.
        em.flush();
        em.clear();

        List<MessageFile> messageFilesToDelete = messageFileRepository.findAllByMessage_Id(savedMessageWithFilesToDeleteId);
        List<MessageFile> remainingMessageFiles = messageFileRepository.findAllByMessage_Id(savedMessageWithRemainingFileId);

        // 삭제 전에는 총 3건이 존재해야 한다.
        // 이 검증이 있어야 삭제 후 1건만 남는 결과가 "원래 1건만 있었기 때문"이 아님을 확인할 수 있다.
        assertThat(messageFileRepository.count()).isEqualTo(3);
        assertThat(messageFilesToDelete)
                .hasSize(2)
                .extracting(MessageFile::getMessageId, MessageFile::getFileId)
                .containsExactlyInAnyOrder(
                        tuple(savedMessageWithFilesToDeleteId, savedFirstBinaryContentId),
                        tuple(savedMessageWithFilesToDeleteId, savedSecondBinaryContentId)
                );
        assertThat(remainingMessageFiles)
                .hasSize(1)
                .singleElement()
                .extracting(MessageFile::getMessageId, MessageFile::getFileId)
                .containsExactly(savedMessageWithRemainingFileId, savedThirdBinaryContentId);

        List<UUID> messageFileIdsToDelete = messageFilesToDelete.stream()
                .map(MessageFile::getId)
                .toList();
        UUID remainingMessageFileId = remainingMessageFiles.get(0).getId();

        // deleteAllByIdIn(...)는 MessageFile id 목록을 받으므로,
        // 삭제 대상 row의 id가 null이 아니고 비대상 row의 id와 겹치지 않는지 확인한다.
        assertThat(messageFileIdsToDelete)
                .hasSize(2)
                .doesNotContainNull()
                .doesNotContain(remainingMessageFileId);
        assertThat(remainingMessageFileId).isNotNull();

        // when
        // 삭제 대상 MessageFile id 목록만 전달해 삭제한다.
        // message_id 기준 삭제가 아니므로 같은 채널과 같은 작성자를 공유하는 비대상 MessageFile은 남아야 한다.
        messageFileRepository.deleteAllByIdIn(messageFileIdsToDelete);

        // delete 쿼리를 DB에 즉시 반영하고, 남아 있는 영속 객체가 결과 검증에 영향을 주지 않도록 비운다.
        em.flush();
        em.clear();

        // then
        // id 목록에 포함된 MessageFile 2건은 삭제되어야 한다.
        assertThat(messageFileRepository.findAllByMessage_Id(savedMessageWithFilesToDeleteId)).isEmpty();

        // id 목록에 포함하지 않은 MessageFile 1건은 그대로 남아 있어야 한다.
        // 이 검증이 없으면 deleteAllByIdIn(...)가 실수로 더 넓은 범위의 row를 삭제해도 놓칠 수 있다.
        assertThat(messageFileRepository.findAllByMessage_Id(savedMessageWithRemainingFileId))
                .hasSize(1)
                .singleElement()
                .extracting(MessageFile::getId, MessageFile::getMessageId, MessageFile::getFileId)
                .containsExactly(remainingMessageFileId, savedMessageWithRemainingFileId, savedThirdBinaryContentId);

        // 전체 MessageFile 개수도 보조적으로 확인한다.
        // given에서 3건을 만들고 id 목록에 포함된 2건만 삭제했으므로 최종적으로 1건만 남아야 한다.
        assertThat(messageFileRepository.count()).isEqualTo(1);
    }

    // MessageFileRepository 테스트는 MessageFile -> Message -> Channel, MessageFile -> BinaryContent 연관 쿼리를
    // 실제 DB와 JPA 매핑으로 검증한다. 따라서 author는 mock이 아니라 실제 User row로 저장한다.
    private User saveAuthor() {
        UserCreateCommand command = new UserCreateCommand(
                "testUser",
                "testPassword",
                "test@gmail.com"
        );

        return userRepository.saveAndFlush(new User(command, null));
    }

    // Message.channel은 nullable = false 연관관계다.
    // 채널 기준 조회/존재 여부 테스트에서 channel_id 조건이 실제 FK를 따라 동작하도록 PUBLIC Channel row를 저장한다.
    private Channel savePublicChannel(String channelName) {
        ChannelCreatePublicCommand command = new ChannelCreatePublicCommand(
                channelName,
                channelName + "Description",
                ChannelType.PUBLIC
        );

        return channelRepository.saveAndFlush(new Channel(command));
    }

    // MessageFile은 message_id를 FK로 가지므로, MessageFile fixture를 만들기 전에 실제 Message row가 필요하다.
    // content만 테스트마다 다르게 넘겨 given 절에는 "어떤 메시지가 대상인지"만 남긴다.
    private Message saveMessage(User author, Channel channel, String content) {
        MessageCreateCommand command = new MessageCreateCommand(
                content,
                author.getId(),
                channel.getId()
        );

        return messageRepository.saveAndFlush(new Message(author, channel, command));
    }

    // bulkInsert(...)는 전달받은 fileIds를 그대로 insert하지 않고 binary_contents에서 존재하는 row를 select한다.
    // 그래서 BinaryContent도 실제 row로 저장해야 native insert의 file_id 필터링을 검증할 수 있다.
    private BinaryContent saveBinaryContent(String originalFileName, String contentType, long size) {
        return binaryContentRepository.saveAndFlush(
                 BinaryContent.createProcessing(originalFileName, contentType, size)
        );
    }

    private PersistenceUnitUtil getPersistenceUnitUtil() {
        return em.getEntityManagerFactory().getPersistenceUnitUtil();
    }
}
