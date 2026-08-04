package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContentResponse;
import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageResponse;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.InvalidMessageException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentService binaryContentService;
    private final PageResponseMapper pageResponseMapper;

    @Override
    public MessageResponse create(MessageCreateRequest request) {
        log.info("메시지 생성을 시작합니다.");

        if (request == null) {
            log.warn("메시지 생성 요청이 비어 있습니다.");
            throw new InvalidMessageException("메시지 생성 요청은 비어 있을 수 없습니다.");
        }

        String content = request.getContent();
        UUID authorId = request.getAuthorId();
        UUID channelId = request.getChannelId();
        List<BinaryContentCreateRequest> attachments = request.getAttachments();

        validateContent(content);

        if (authorId == null) {
            log.warn("메시지 생성에 실패했습니다. authorId가 null입니다.");
            throw new InvalidMessageException("메시지 작성자 id는 필수입니다.");
        }

        if (channelId == null) {
            log.warn("메시지 생성에 실패했습니다. channelId가 null입니다.");
            throw new InvalidMessageException("메시지를 작성할 채널 id는 필수입니다.");
        }

        log.debug(
                "메시지 생성 요청을 처리합니다. authorId={}, channelId={}, contentLength={}, attachmentCount={}",
                authorId,
                channelId,
                content.length(),
                attachments == null ? 0 : attachments.size()
        );

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> {
                    log.warn(
                            "메시지 작성자를 찾을 수 없습니다. authorId={}",
                            authorId
                    );

                    return new UserNotFoundException(authorId);
                });

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> {
                    log.warn(
                            "메시지를 작성할 채널을 찾을 수 없습니다. channelId={}",
                            channelId
                    );

                    return new ChannelNotFoundException(channelId);
                });

        try {
            Message message = new Message(
                    content,
                    author,
                    channel
            );

            if (attachments != null && !attachments.isEmpty()) {
                for (BinaryContentCreateRequest attachmentRequest : attachments) {
                    if (attachmentRequest == null) {
                        log.warn(
                                "null 첨부파일 요청을 건너뜁니다. authorId={}, channelId={}",
                                authorId,
                                channelId
                        );
                        continue;
                    }

                    validateAttachment(attachmentRequest);

                    log.debug(
                            "메시지 첨부파일을 저장합니다. fileName={}, contentType={}, size={}",
                            attachmentRequest.getFileName(),
                            attachmentRequest.getContentType(),
                            attachmentRequest.getBytes().length
                    );

                    BinaryContentResponse savedAttachment =
                            binaryContentService.create(attachmentRequest);

                    BinaryContent attachment =
                            binaryContentRepository.findById(savedAttachment.getId())
                                    .orElseThrow(() -> {
                                        log.error(
                                                "저장된 첨부파일 메타데이터를 찾을 수 없습니다. attachmentId={}",
                                                savedAttachment.getId()
                                        );

                                        return new IllegalStateException(
                                                "저장된 첨부파일을 찾을 수 없습니다. attachmentId="
                                                        + savedAttachment.getId()
                                        );
                                    });

                    message.addAttachment(attachment);

                    log.debug(
                            "메시지에 첨부파일을 연결했습니다. attachmentId={}, fileName={}",
                            attachment.getId(),
                            attachment.getFileName()
                    );
                }
            }

            Message savedMessage = messageRepository.save(message);

            log.info(
                    "메시지 생성이 완료되었습니다. messageId={}, authorId={}, channelId={}, attachmentCount={}",
                    savedMessage.getId(),
                    savedMessage.getAuthorId(),
                    savedMessage.getChannelId(),
                    savedMessage.getAttachmentIds() == null
                            ? 0
                            : savedMessage.getAttachmentIds().size()
            );

            return toResponse(savedMessage);

        } catch (RuntimeException e) {
            log.error(
                    "메시지 생성 중 오류가 발생했습니다. authorId={}, channelId={}",
                    authorId,
                    channelId,
                    e
            );
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponse read(UUID id) {
        Message message = findMessageById(id);

        return toResponse(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> findAllByChannelId(UUID channelId) {
        validateChannelId(channelId);

        log.debug(
                "채널 메시지 목록을 조회합니다. channelId={}, limit={}",
                channelId,
                50
        );

        List<Message> messages =
                messageRepository.findByChannel_IdOrderByCreatedAtDesc(
                        channelId,
                        PageRequest.of(0, 50)
                );

        log.debug(
                "채널 메시지 목록 조회가 완료되었습니다. channelId={}, resultCount={}",
                channelId,
                messages.size()
        );

        return toResponses(messages);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MessageResponse> findAllByChannelId(
            UUID channelId,
            Instant cursor,
            int size
    ) {
        validateChannelId(channelId);

        int safeSize = size <= 0 ? 50 : size;
        Pageable pageable = PageRequest.of(0, safeSize + 1);

        log.debug(
                "채널 메시지 커서 페이지 조회를 시작합니다. channelId={}, cursor={}, size={}",
                channelId,
                cursor,
                safeSize
        );

        List<Message> messages;

        if (cursor == null) {
            messages = messageRepository.findByChannel_IdOrderByCreatedAtDesc(
                    channelId,
                    pageable
            );
        } else {
            messages =
                    messageRepository.findByChannel_IdAndCreatedAtLessThanOrderByCreatedAtDesc(
                            channelId,
                            cursor,
                            pageable
                    );
        }

        boolean hasNext = messages.size() > safeSize;

        if (hasNext) {
            messages = new ArrayList<>(messages.subList(0, safeSize));
        }

        List<MessageResponse> content = toResponses(messages);

        Object nextCursor = null;

        if (hasNext && !messages.isEmpty()) {
            nextCursor = messages.get(messages.size() - 1).getCreatedAt();
        }

        log.debug(
                "채널 메시지 커서 페이지 조회가 완료되었습니다. channelId={}, resultCount={}, hasNext={}, nextCursor={}",
                channelId,
                content.size(),
                hasNext,
                nextCursor
        );

        return pageResponseMapper.toCursorPageResponse(
                content,
                nextCursor,
                safeSize,
                hasNext
        );
    }

    @Override
    public MessageResponse update(MessageUpdateRequest request) {
        log.info(
                "메시지 수정을 시작합니다. messageId={}",
                request == null ? null : request.getId()
        );

        if (request == null) {
            log.warn("메시지 수정 요청이 비어 있습니다.");
            throw new InvalidMessageException("메시지 수정 요청은 비어 있을 수 없습니다.");
        }

        if (request.getId() == null) {
            log.warn("메시지 수정에 실패했습니다. messageId가 null입니다.");
            throw new InvalidMessageException("수정할 메시지 id는 필수입니다.");
        }

        validateContent(request.getContent());

        try {
            Message message = findMessageById(request.getId());

            log.debug(
                    "메시지 수정 요청을 처리합니다. messageId={}, contentLength={}",
                    request.getId(),
                    request.getContent().length()
            );

            message.update(request.getContent());

            Message savedMessage = messageRepository.save(message);

            log.info(
                    "메시지 수정이 완료되었습니다. messageId={}",
                    savedMessage.getId()
            );

            return toResponse(savedMessage);

        } catch (RuntimeException e) {
            log.error(
                    "메시지 수정 중 오류가 발생했습니다. messageId={}",
                    request.getId(),
                    e
            );
            throw e;
        }
    }

    @Override
    public void delete(UUID id) {
        log.info("메시지 삭제를 시작합니다. messageId={}", id);

        Message message = findMessageById(id);
        List<UUID> attachmentIds = message.getAttachmentIds();

        try {
            log.debug(
                    "메시지 첨부파일 삭제를 준비합니다. messageId={}, attachmentCount={}",
                    id,
                    attachmentIds == null ? 0 : attachmentIds.size()
            );

            if (attachmentIds != null && !attachmentIds.isEmpty()) {
                for (UUID attachmentId : attachmentIds) {
                    if (attachmentId == null) {
                        continue;
                    }

                    log.debug(
                            "메시지 첨부파일을 삭제합니다. messageId={}, attachmentId={}",
                            id,
                            attachmentId
                    );

                    binaryContentService.delete(attachmentId);
                }
            }

            messageRepository.deleteById(id);

            log.info(
                    "메시지 삭제가 완료되었습니다. messageId={}, deletedAttachmentCount={}",
                    id,
                    attachmentIds == null ? 0 : attachmentIds.size()
            );

        } catch (RuntimeException e) {
            log.error(
                    "메시지 삭제 중 오류가 발생했습니다. messageId={}",
                    id,
                    e
            );
            throw e;
        }
    }

    private Message findMessageById(UUID id) {
        if (id == null) {
            log.warn("메시지 조회에 실패했습니다. messageId가 null입니다.");
            throw new InvalidMessageException("메시지 id는 필수입니다.");
        }

        return messageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "메시지를 찾을 수 없습니다. messageId={}",
                            id
                    );

                    return new MessageNotFoundException(id);
                });
    }

    private void validateChannelId(UUID channelId) {
        if (channelId == null) {
            log.warn("메시지 조회에 실패했습니다. channelId가 null입니다.");
            throw new InvalidMessageException(
                    "메시지를 조회할 채널 id는 필수입니다."
            );
        }

        if (!channelRepository.existsById(channelId)) {
            log.warn(
                    "메시지를 조회할 채널을 찾을 수 없습니다. channelId={}",
                    channelId
            );

            throw new ChannelNotFoundException(channelId);
        }
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            log.warn("메시지 내용 검증에 실패했습니다. 내용이 비어 있습니다.");
            throw new InvalidMessageException(
                    "메시지 내용은 비어 있을 수 없습니다."
            );
        }
    }

    private void validateAttachment(BinaryContentCreateRequest request) {
        if (request.getFileName() == null
                || request.getFileName().isBlank()) {

            log.warn("첨부파일 검증에 실패했습니다. 파일 이름이 비어 있습니다.");

            throw new InvalidMessageException(
                    "첨부파일 이름은 비어 있을 수 없습니다."
            );
        }

        if (request.getBytes() == null
                || request.getBytes().length == 0) {

            log.warn(
                    "첨부파일 검증에 실패했습니다. 파일 데이터가 비어 있습니다. fileName={}",
                    request.getFileName()
            );

            throw new InvalidMessageException(
                    "첨부파일 데이터는 비어 있을 수 없습니다."
            );
        }
    }

    private List<MessageResponse> toResponses(List<Message> messages) {
        List<MessageResponse> responses = new ArrayList<>();

        if (messages == null || messages.isEmpty()) {
            return responses;
        }

        for (Message message : messages) {
            responses.add(toResponse(message));
        }

        return responses;
    }

    private MessageResponse toResponse(Message message) {
        User author = message.getAuthor();
        UserResponse authorResponse = toAuthorResponse(author);

        List<UUID> attachmentIds = message.getAttachmentIds();
        List<BinaryContentResponse> attachments =
                toAttachmentResponses(message.getAttachments());

        return new MessageResponse(
                message.getId(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getContent(),
                message.getAuthorId(),
                authorResponse,
                message.getChannelId(),
                attachmentIds,
                attachments
        );
    }

    private UserResponse toAuthorResponse(User author) {
        if (author == null) {
            return null;
        }

        BinaryContentResponse profileResponse =
                toBinaryContentResponse(author.getProfile());

        return new UserResponse(
                author.getId(),
                author.getCreatedAt(),
                author.getUpdatedAt(),
                author.getUsername(),
                author.getEmail(),
                author.getProfileId(),
                profileResponse,
                false
        );
    }

    private List<BinaryContentResponse> toAttachmentResponses(
            List<BinaryContent> binaryContents
    ) {
        List<BinaryContentResponse> attachments = new ArrayList<>();

        if (binaryContents == null || binaryContents.isEmpty()) {
            return attachments;
        }

        for (BinaryContent binaryContent : binaryContents) {
            if (binaryContent != null) {
                attachments.add(
                        toBinaryContentResponse(binaryContent)
                );
            }
        }

        return attachments;
    }

    private BinaryContentResponse toBinaryContentResponse(
            BinaryContent binaryContent
    ) {
        if (binaryContent == null) {
            return null;
        }

        return new BinaryContentResponse(
                binaryContent.getId(),
                binaryContent.getCreatedAt(),
                binaryContent.getUpdatedAt(),
                binaryContent.getFileName(),
                safeContentType(binaryContent.getContentType()),
                binaryContent.getSize()
        );
    }

    private String safeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "application/octet-stream";
        }

        return contentType;
    }
}