package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.*;
import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.mapper.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;
import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentService binaryContentService;

    @Override
    public MessageDto find(UUID id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("메세지 아이디를 찾을 수 없습니다"));

        return messageMapper.toDto(message);
    }

    @Override
    @Transactional
    public MessageDto create(CreateMessageCommand command,
                             List<CreateBinaryContentCommand> attachmentCommands) {

        if (command == null) {
            throw new IllegalArgumentException("메시지 생성 요청은 필수입니다.");
        }

        Channel channel = channelRepository.findById(command.channelId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널입니다."));

        User author = userRepository.findById(command.authorId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 작성자입니다."));

        List<CreateBinaryContentCommand> commands =
                attachmentCommands == null
                        ? List.of()
                        : attachmentCommands;
        List<BinaryContent> attachments = commands.stream()
                .map(binaryContentService::create)
                .map(BinaryContentDto::id)
                .map(attachmentId ->
                        binaryContentRepository.findById(attachmentId)
                                .orElseThrow(() ->
                                        new IllegalStateException(
                                                "저장된 첨부파일을 찾을 수 없습니다."
                                        )
                                )
                )
                .toList();

        Message message = new Message(
                command.content(),
                channel,
                author,
                attachments
        );

        Message savedMessage =
                messageRepository.save(message);

        return messageMapper.toDto(savedMessage);
    }


    @Override
    public List<MessageDto> findAllByChannelId(UUID channelId) {
        if (channelId == null) {
            throw new IllegalArgumentException("채널 ID는 필수입니다.");
        }

        if (channelRepository.existsById(channelId)) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }

        List<Message> messages = messageRepository.findByChannelId(channelId);
        return messageMapper.toDtoList(messages);

    }

    @Override
    @Transactional
    public MessageDto update(UUID id, UpdateMessageCommand command) { // UpdateMessageCommand 타입으로 수정
        if (id == null) {
            throw new IllegalArgumentException("메시지 ID는 필수입니다.");
        }

        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("메시지 아이디를 찾을 수 없습니다."));

        message.update(command.content());

        return messageMapper.toDto(message);
    }


    @Override
    @Transactional
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("메세지 ID는 필수입니다.");
        }
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메세지 ID입니다."));

        messageRepository.delete(message);
    }

    @Override
    public PageResponse<MessageDto> getMessages(UUID channelId, int page) {
        if (channelId == null) {
            throw new IllegalArgumentException("채널 ID는 필수입니다."); }

            Pageable pageable = PageRequest.of(page, 50, Sort.by(Sort.Direction.DESC, "createdAt"));
            Slice<Message> messageSlice = messageRepository.findByChannelId(channelId, pageable);

            Slice<MessageDto> responseSlice = messageSlice.map(messageMapper::toDto);

            return PageResponseMapper.fromSlice(responseSlice);

        }
    }

