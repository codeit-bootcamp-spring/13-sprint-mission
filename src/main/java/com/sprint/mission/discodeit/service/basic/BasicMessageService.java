package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.MessageUpdateResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.FileException;
import com.sprint.mission.discodeit.exception.ObjectNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final MessageMapper messageMapper;

    //interface
    @Override
    @Transactional
    public MessageDto createMessage(MessageCreateRequest request, List<MultipartFile> files) {
        //유저 검색
        User userTemp = userRepository.findById(request.authorId())
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 유저는 데이터파일에 존재하지 않습니다."));
        //채널 검색
        Channel channelTemp = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 채널은 데이터파일에 존재하지 않습니다."));

        List<BinaryContent> binaryContentList = new ArrayList<>();
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
                        binaryContent = binaryContentRepository.save(binaryContent);
                        binaryContentList.add(binaryContent);

                    } catch (IOException e) {
                        throw new FileException(e.getMessage());
                    }
                }
            }
        }

        //메세지 생성
        Message message = new Message(request.content(), channelTemp, userTemp, binaryContentList);
        message = messageRepository.save(message);
        log.info("메시지: {}가 생성됨.", message.getContent());

        return messageMapper.toDto(message);
    }

    @Override
    @Transactional
    public List<MessageDto> findAllByChannelId(UUID channelId) {
        return messageRepository.findAllByChannelId(channelId).stream()
                .map(messageMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public MessageDto updateMessage(UUID messageId, MessageUpdateRequest request) {
        //메시지 검색
        Message messageTemp = messageRepository.findById(messageId)
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 메시지는 데이터파일에 존재하지 않습니다."));

        log.info("메시지: {}가 수정됨.\n->{}", messageTemp.getContent(), request.newContent());

        //메시지 업데이트
        messageTemp.updateMessage(request.newContent());
        //dirty checking
        //messageTemp = messageRepository.save(messageTemp);

        return messageMapper.toDto(messageTemp);
    }

    @Override
    @Transactional
    public void deleteMessage(UUID messageId) {
        //메시지 검색
        Message messageTemp = messageRepository.findById(messageId)
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 메시지는 데이터파일에 존재하지 않습니다."));

        //첨부파일 삭제
        for (BinaryContent attachment : messageTemp.getAttachments()) {
            binaryContentRepository.deleteById(attachment.getId());
        }

        //메시지 삭제
        messageRepository.deleteById(messageTemp.getId());

        log.info("메시지: {}가 삭제됨.", messageTemp.getContent());
    }


    // 들어온 userId 필드가 레포지터리에 존재하는지 검증하는 메서드
    private void validateUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ObjectNotFoundException("유저: " + userId + "이 존재하지 않습니다.");
        }
    }
    // 들어온 channelId필드가 레포지터리에 존재하는지 검증하는 메서드
    private void validateChannelExists(UUID channelId) {
        if (!channelRepository.existsById(channelId)) {
            throw new ObjectNotFoundException("채널: " + channelId + "이 존재하지 않습니다.");
        }
    }
}
