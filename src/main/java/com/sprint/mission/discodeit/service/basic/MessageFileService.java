package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.MessageFile;
import com.sprint.mission.discodeit.exception.message.MessageFileSaveFailedException;
import com.sprint.mission.discodeit.repository.MessageFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MessageFileService {
    private final MessageFileRepository messageFileRepository;
    private final BinaryContentService binaryContentService;

    public List<MessageFile> save(Message message, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return List.of();

        List<UUID> fileIds = files.stream()
                .map(binaryContentService::create)
                .filter(Optional::isPresent)
                .map(content -> content.get().getId())
                .toList();

        if (fileIds.isEmpty()) return List.of();

        int insertedCount = messageFileRepository.bulkInsert(fileIds, message.getId());
        if (insertedCount != fileIds.size()) {
            throw new MessageFileSaveFailedException(message.getId(), fileIds.size(), insertedCount);
        }

        return messageFileRepository.findAllByMessage_Id(message.getId());
    }

    public void deleteByMessageId(UUID messageId) {
        if (!messageFileRepository.existsByMessage_Id(messageId)) return;

        List<MessageFile> messageFiles = messageFileRepository.findAllByMessage_Id(messageId);

        List<BinaryContent> binaryContents = convertToBinaryContents(messageFiles);
        messageFileRepository.deleteByMessage_Id(messageId);
        // 파일을 참조하는 연결 행의 삭제 SQL을 먼저 실행해 파일 삭제 시 외래 키 충돌을 막는다.
        // flush는 커밋이 아니므로 이후 실패 시 같은 트랜잭션의 DB 변경은 롤백 대상이다.
        messageFileRepository.flush();

        binaryContentService.deleteAll(binaryContents);

    }

    public void deleteAllByChannelId(UUID channelId) {
        if (!messageFileRepository.existsByMessage_Channel_Id(channelId)) return;

        List<MessageFile> messageFiles = messageFileRepository.findAllByChannelId(channelId);

        List<BinaryContent> binaryContents = convertToBinaryContents(messageFiles);

        List<UUID> messageFileIds = messageFiles.stream().map(MessageFile::getId).toList();

        messageFileRepository.deleteAllByIdIn(messageFileIds);
        // 파일을 참조하는 연결 행의 삭제 SQL을 먼저 실행해 파일 삭제 시 외래 키 충돌을 막는다.
        // flush는 커밋이 아니므로 이후 실패 시 같은 트랜잭션의 DB 변경은 롤백 대상이다.
        messageFileRepository.flush();

        binaryContentService.deleteAll(binaryContents);
    }

    @Transactional(readOnly = true)
    public List<MessageFile> findAllByMessageId(UUID messageId) {
        return messageFileRepository.findAllByMessage_Id(messageId);
    }

    @Transactional(readOnly = true)
    public List<MessageFile> findAllByMessageIds(List<UUID> messageIds) {
        return messageFileRepository.findAllByMessageIdIn(messageIds);
    }

    private List<BinaryContent> convertToBinaryContents(List<MessageFile> messageFiles) {
        return messageFiles
                .stream()
                .map(MessageFile::getBinaryContent)
                .toList();
    }
}
