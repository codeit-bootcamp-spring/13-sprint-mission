package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageUpdateResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.exception.FileException;
import com.sprint.mission.discodeit.exception.ObjectNotFoundException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    //interface
    @Override
    public Message createMessage(MessageCreateRequest request, List<MultipartFile> files) {
        //입력값 검증 처리하겠습니다
//        validateString(request.content());
//        validateUUID(request.authorId());
//        validateUUID(request.channelId());

        //존재하는 유저, 채널인지 검증
        validateUserExists(request.authorId());
        validateChannelExists(request.channelId());

        List<UUID> binaryContentIdList = new ArrayList<>();
        //첨부파일 존재 시
        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    try {
                        //binaryContent 생성
                        BinaryContent binaryContent = new BinaryContent(
                                file.getOriginalFilename(),
                                (long) file.getBytes().length,
                                file.getContentType(),
                                file.getBytes()
                        );
                        binaryContentRepository.createBinaryContent(binaryContent);
                        binaryContentIdList.add(binaryContent.getId());

                    } catch (IOException e) {
                        throw new FileException(e.getMessage());
                    }
                }
            }
        }

        //메세지 생성
        Message message = new Message(request.content(), request.channelId(), request.authorId(), binaryContentIdList);
        messageRepository.createMessage(message);
        log.info("메시지: {}가 생성됨.", message.getContent());

        return message;
    }

    @Override
    public List<Message> findAllByChannelId(UUID channelId) {
        //입력값 검증 처리하겠습니다
//        validateUUID(channelId);

        return messageRepository.findAllMessagesByChannelId(channelId);
    }

    @Override
    public MessageUpdateResponse updateMessage(MessageUpdateRequest request, List<MultipartFile> files) {
        //입력값 검증 처리하겠습니다
//        validateUUID(request.messageId());
//        validateString(request.content());

        //메시지 검색
        Message messageTemp = messageRepository.findMessageById(request.messageId())
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 메시지는 데이터파일에 존재하지 않습니다."));

        List<UUID> binaryContentIdList = messageTemp.getAttachmentIds();
        //첨부파일 존재 시
        if (files != null) {
            //이전 첨부파일 삭제
            if (binaryContentIdList != null) {
                for (UUID attachmentId : binaryContentIdList) {
                    binaryContentRepository.deleteBinaryContent(attachmentId);
                }
            }

            List<UUID> newBinaryContentIdList = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    try {
                        //binaryContent 생성
                        BinaryContent binaryContent = new BinaryContent(
                                file.getOriginalFilename(),
                                (long) file.getBytes().length,
                                file.getContentType(),
                                file.getBytes()
                        );
                        binaryContentRepository.createBinaryContent(binaryContent);
                        newBinaryContentIdList.add(binaryContent.getId());

                    } catch (IOException e) {
                        throw new FileException(e.getMessage());
                    }
                }
            }
            binaryContentIdList = newBinaryContentIdList;
        }

        log.info("메시지: {}가 수정됨.\n->{}", messageTemp.getContent(), request.content());

        //메시지 업데이트
        messageTemp.updateMessage(request.content(), binaryContentIdList);
        messageRepository.save();

        return MessageUpdateResponse.from(messageTemp);
    }

    @Override
    public void deleteMessage(UUID messageId) {
        //입력값 검증 처리하겠습니다
//        validateUUID(messageId);

        //메시지 검색
        Message messageTemp = messageRepository.findMessageById(messageId)
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 메시지는 데이터파일에 존재하지 않습니다."));

        //첨부파일 삭제
        for (UUID attachmentId : messageTemp.getAttachmentIds()) {
            binaryContentRepository.deleteBinaryContent(attachmentId);
        }

        //메시지 삭제
        messageRepository.deleteMessageById(messageTemp.getId());

        log.info("메시지: {}가 삭제됨.", messageTemp.getContent());
    }


    // 들어온 String 필드가 null 혹은 공백인지 검증하는 메서드
    private void validateString(String str) {
        if (str == null || str.isBlank()) {
            throw new IllegalArgumentException("에러: 입력값이 Null 또는 공백입니다.");
        }
    }
    // 들어온 UUID 필드가 null인지 검증하는 메서드
    private void validateUUID(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("에러: 입력값이 Null입니다.");
        }
    }
    // 들어온 userId 필드가 레포지터리에 존재하는지 검증하는 메서드
    private void validateUserExists(UUID userId) {
        if (!userRepository.existsUserById(userId)) {
            throw new ObjectNotFoundException("유저: " + userId + "이 존재하지 않습니다.");
        }
    }
    // 들어온 channelId필드가 레포지터리에 존재하는지 검증하는 메서드
    private void validateChannelExists(UUID channelId) {
        if (!channelRepository.existsChannelById(channelId)) {
            throw new ObjectNotFoundException("채널: " + channelId + "이 존재하지 않습니다.");
        }
    }
}
