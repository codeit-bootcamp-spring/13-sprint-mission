package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.aspect.LogAction;
import com.sprint.mission.discodeit.dto.command.message.MessageCreateCommand;
import com.sprint.mission.discodeit.dto.command.message.MessageUpdateCommand;
import com.sprint.mission.discodeit.dto.repository.MessagePagingCondition;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.MessageFile;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicMessageService implements MessageService {
    private final MessageRepository messageRepository;
    private final MessageFileService messageFileService;
    private final UserReader userReader;
    private final ChannelReader channelReader;
    private final MessageMapper messageMapper;
    private final PageResponseMapper<MessageDto> messageDtoPageResponseMapper;

    @LogAction(value = "메시지 생성")
    @Override
    public MessageDto save(MessageCreateCommand command, List<MultipartFile> files) {
        Channel channel = getChannelRequireThrow(command.channelId());
        User author = getUserRequireThrow(command.userId());

        Message savedMessage = messageRepository.save(new Message(author, channel, command));
        List<MessageFile> savedMessageFiles = messageFileService.save(savedMessage, files);

        return messageMapper.toDto(savedMessage, savedMessageFiles);
    }

    @Transactional(readOnly = true)
    @Override
    public MessageDto findById(UUID messageId) {
        Message message = getMessageRequireThrow(messageId);

        return messageMapper.toDto(message, messageFileService.findAllByMessageId(messageId));
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Pageable pageable, UUID cursor) {

        MessagePagingCondition condition = new MessagePagingCondition(channelId, pageable, cursor);
        Slice<Message> messageSlice = getMessageSliceDsl(condition);

        List<Message> content = messageSlice.getContent();

        List<UUID> messageIds = getMessageIds(content);

        Map<UUID, List<MessageFile>> messageFilesByMessageId = getMessageFilesGroupedByMessageId(messageIds);

        Slice<MessageDto> dtoSlice = messageSlice.map(m ->
                messageMapper.toDto(
                        m,
                        messageFilesByMessageId.getOrDefault(m.getId(), List.of())
                )
        );

        Object nextCursor = null;
        if (messageSlice.hasNext() && !content.isEmpty()) {
            nextCursor = content.get(content.size() - 1).getId().toString();
        }

        return messageDtoPageResponseMapper.fromSlice(dtoSlice, nextCursor);
    }

    @LogAction(value = "메시지 수정")
    @Override
    @PreAuthorize("@messageGuard.isOwner(#messageId, authentication.principal.userDto.id)")
    public MessageDto update(UUID messageId, MessageUpdateCommand command) {

        Message message = getMessageRequireThrow(messageId);

        message.updateInfo(command);

        return messageMapper.toDto(messageRepository.save(message), messageFileService.findAllByMessageId(messageId));
    }

    @LogAction(value = "메시지 삭제", idName = "messageId", idParamIndex = 0)
    @Override
    @PreAuthorize("@messageGuard.isOwner(#messageId, authentication.principal.userDto.id)")
    public void delete(UUID messageId) {
        if (!messageRepository.existsById(messageId)) throw new MessageNotFoundException(messageId);

        messageFileService.deleteByMessageId(messageId);

        messageRepository.deleteById(messageId);

    }

    @Override
    public void deleteAllByChannelId(UUID channelId) {
        if (!messageRepository.existsByChannel_Id(channelId)) return;

        messageFileService.deleteAllByChannelId(channelId);

        messageRepository.deleteAllByChannel_Id(channelId);
    }

    @Override
    public void detachByAuthorId(UUID userId) {
        if(!messageRepository.existsByAuthor_Id(userId)) return;

        messageRepository.detachAuthorByAuthorId(userId);

    }

    private Channel getChannelRequireThrow(UUID channelId) {
        return channelReader.getChannel(channelId);
    }

    private User getUserRequireThrow(UUID userId) {
        return userReader.getUser(userId);
    }

    private Message getMessageRequireThrow(UUID messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(()-> new MessageNotFoundException(messageId));
    }

    private @NonNull Map<UUID, List<MessageFile>> getMessageFilesGroupedByMessageId(List<UUID> messageIds) {
        if (messageIds.isEmpty()) return Map.of();

        return messageFileService.findAllByMessageIds(messageIds).stream()
                .collect(Collectors.groupingBy(MessageFile::getMessageId, Collectors.toList()));
    }

    private @NonNull List<UUID> getMessageIds(List<Message> content) {
        return content
                .stream()
                .map(Message::getId)
                .toList();
    }

    private Slice<Message> getMessageSliceDsl(MessagePagingCondition condition) {

        return messageRepository.findAllByCondition(condition);
    }

}
