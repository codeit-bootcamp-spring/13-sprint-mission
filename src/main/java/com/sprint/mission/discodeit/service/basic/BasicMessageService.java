package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.FileException;
import com.sprint.mission.discodeit.exception.ResourceNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicMessageService implements MessageService {

    //필드
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final MessageMapper messageMapper;
    private final BinaryContentStorage binaryContentStorage;
    private final PageResponseMapper pageResponseMapper;

    //interface
    @Override
    @Transactional
    public MessageDto createMessage(MessageCreateRequest request, List<MultipartFile> files) {
        log.debug("메시지 생성 시작");

        //유저 검색
        User userTemp = userRepository.findById(request.authorId())
                .orElseThrow(() -> {
                    log.warn("메시지 생성 실패: 해당 유저는 데이터파일에 존재하지 않습니다.");
                    return new ResourceNotFoundException("에러: 해당 유저는 데이터파일에 존재하지 않습니다.");
                });
        //채널 검색
        Channel channelTemp = channelRepository.findById(request.channelId())
                .orElseThrow(() -> {
                    log.warn("메시지 생성 실패: 해당 채널은 데이터파일에 존재하지 않습니다.");
                    return new ResourceNotFoundException("에러: 해당 채널은 데이터파일에 존재하지 않습니다.");
                });

        List<BinaryContent> binaryContentList = new ArrayList<>();
        //첨부파일 존재 시
        if (files != null) {
            log.debug("첨부 파일 업로드 처리 시작");

            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    try {
                        //binaryContent 생성
                        BinaryContent binaryContent = new BinaryContent(
                                file.getOriginalFilename(),
                                (long) file.getBytes().length,
                                file.getContentType()
                        );
                        binaryContent = binaryContentRepository.save(binaryContent);
                        binaryContentStorage.put(binaryContent.getId(), file.getBytes());

                        binaryContentList.add(binaryContent);
                    } catch (IOException e) {
                        log.error("첨부 파일 업로드 실패");
                        throw new FileException(e.getMessage());
                    }
                }
            }
            log.info("첨부 파일 업로드 완료");
        }

        //메세지 생성
        Message message = new Message(request.content(), channelTemp, userTemp, binaryContentList);
        message = messageRepository.save(message);
        log.info("메시지: {}가 생성됨.", message.getContent());

        log.info("메시지 생성 완료");

        return messageMapper.toDto(message);
    }

    @Transactional(readOnly = true)
    public PageResponse<MessageDto> getMessagesByChannelId(UUID channelId, Instant cursor, Pageable pageable) {
        Slice<MessageDto> messageDtoSlice = ((cursor == null)
                ? messageRepository.findAllByChannelId(channelId, pageable)
                : messageRepository.findAllByChannelId(channelId, cursor, pageable))
                .map(messageMapper::toDto);

        return pageResponseMapper.fromSlice(messageDtoSlice);
    }

    @Override
    @Transactional
    public MessageDto updateMessage(UUID messageId, MessageUpdateRequest request) {
        log.debug("메시지 수정 시작");

        //메시지 검색
        Message messageTemp = messageRepository.findById(messageId)
                .orElseThrow(() -> {
                    log.warn("메시지 수정 실패: 해당 메시지는 데이터파일에 존재하지 않습니다.");
                    return new ResourceNotFoundException("에러: 해당 메시지는 데이터파일에 존재하지 않습니다.");
                });

        log.info("메시지: {}가 수정됨.\n->{}", messageTemp.getContent(), request.newContent());

        //메시지 업데이트
        messageTemp.updateMessage(request.newContent());

        log.info("메시지 수정 완료");

        return messageMapper.toDto(messageTemp);
    }

    @Override
    @Transactional
    public void deleteMessage(UUID messageId) {
        log.debug("메시지 삭제 시작");

        //메시지 검색
        Message messageTemp = messageRepository.findById(messageId)
                .orElseThrow(() -> {
                    log.warn("메시지 삭제 실패: 해당 메시지는 데이터파일에 존재하지 않습니다.");
                    return new ResourceNotFoundException("에러: 해당 메시지는 데이터파일에 존재하지 않습니다.");
                });

        //첨부파일 삭제
        for (BinaryContent attachment : messageTemp.getAttachments()) {
            binaryContentRepository.deleteById(attachment.getId());
        }

        //메시지 삭제
        messageRepository.deleteById(messageTemp.getId());

        log.info("메시지: {}가 삭제됨.", messageTemp.getContent());

        log.info("메시지 삭제 완료");
    }


    // 들어온 userId 필드가 레포지터리에 존재하는지 검증하는 메서드
    private void validateUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            log.warn("유저: {}이 존재하지 않습니다.", userId);
            throw new ResourceNotFoundException("유저: " + userId + "이 존재하지 않습니다.");
        }
    }
    // 들어온 channelId필드가 레포지터리에 존재하는지 검증하는 메서드
    private void validateChannelExists(UUID channelId) {
        if (!channelRepository.existsById(channelId)) {
            log.warn("채널: {}이 존재하지 않습니다.", channelId);
            throw new ResourceNotFoundException("채널: " + channelId + "이 존재하지 않습니다.");
        }
    }
}
