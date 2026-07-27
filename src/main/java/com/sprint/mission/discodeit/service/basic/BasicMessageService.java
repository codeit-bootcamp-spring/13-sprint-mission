package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreate;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicMessageService implements MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentStorage binaryContentStorage;
    private final PageResponseMapper pageResponseMapper;
    private final MapStructMapper mapStructMapper;

    private User getUserOrException(UUID id){
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User with id - {} not found",id)
        );
    }

    private Channel getChannelOrException(UUID id){
        return channelRepository.findById(id).orElseThrow(
                () -> new ChannelNotFoundException("Channel with id - {} not found",id)
        );
    }


    @Override
    @Transactional
    public MessageDto createMessage(MessageCreateRequest cmi, Optional<List<BinaryContentCreate>> olbcc){

        User user = getUserOrException(cmi.authorId());
        Channel channel = getChannelOrException(cmi.channelId());

        log.debug("Message Create - author: {}, channel: {}", user.getId(), channel.getId());

        List<BinaryContent> atts = olbcc.map(
                lbcc -> lbcc.stream().map(
                        bcc -> {
                            byte[] dumi = {0x40};
                            BinaryContent bc = new BinaryContent(
                                    bcc.filename(),
                                    bcc.contentType(),
                                    bcc.size(),
                                    dumi
                            );
                            binaryContentRepository.save(bc);
                            binaryContentStorage.put(bc.getId(),bcc.content());
                            return bc;
                        }

                ).toList()
        ).orElse(null);



        Message res = new Message(
                cmi.content(),
                channel,
                user,
                atts
        );

        messageRepository.save(res);

        log.info("Message Created - {}", res.getId());

        return mapStructMapper.toDto(res,userDto(res),attrDto(res));
    }

    @Override
    @Transactional
    public PageResponse<MessageDto> findallByChannelId(UUID cannelID, Pageable pageable){
        return pageResponseMapper.fromSlice(messageRepository.findByChannelIdForMessageDto(cannelID,pageable)
                .map(m -> mapStructMapper.toDto(m,userDto(m),attrDto(m))));
    }

    @Transactional
    @Override
    public PageResponse<MessageDto> findallByChannelIdWithCursor(UUID cannelID, Pageable pageable, Instant cursor){
        if (cursor == null) cursor = Instant.now();
        Slice<Message> res = messageRepository.findByChannelWithCursor(cannelID,pageable,cursor);
        List<Message> content = res.getContent();
        Instant newCursor = content.isEmpty() ? null : content.get(content.size()-1).getCreatedAt();
        return pageResponseMapper.fromSliceWithCursor(
                res.map(m -> mapStructMapper.toDto(m,userDto(m),attrDto(m))),newCursor);
    }

    @Override
    @Transactional
    public MessageDto updateMessageData(UUID id, MessageUpdateRequest umi){
        Message msg = getMessageOrException(id);

        msg.setContent(umi.newContent());
        msg.setUpdatedAt(Instant.now());
        messageRepository.save(msg);

        log.info("Message Updated - {}", msg.getId());

        return mapStructMapper.toDto(msg,userDto(msg),attrDto(msg));
    }

    private Message getMessageOrException(UUID id){
        return messageRepository.findById(id)
                .orElseThrow(
                        () -> new MessageNotFoundException("Message with id - {} not found",id)
                );
    }

    @Override
    @Transactional
    public void deleteMessage(UUID id){
        Message msg = getMessageOrException(id);

        // delete attribute
        if (!msg.getAttachment().isEmpty()){
            binaryContentRepository.deleteAll(msg.getAttachment());
        }

        messageRepository.delete(msg);

        log.info("Message Deleted - {}", msg.getId());
    }

    private List<BinaryContentDto> attrDto(Message msg){
        if (msg.getAttachment() == null) return null;
        return msg.getAttachment().stream().map(this::binaryContentDto).toList();
    }

    private UserDto userDto(Message msg){
        User user = msg.getAuthor();
        BinaryContent profile = user.getProfile();
        return mapStructMapper.toDto(user,binaryContentDto(profile),user.online());
    }

    private BinaryContentDto binaryContentDto(BinaryContent bc){
        return mapStructMapper.toDto(bc,bytesFromBinaryContent(bc));
    }

    private byte[] bytesFromBinaryContent(BinaryContent bc){
        try (InputStream in = binaryContentStorage.get(bc.getId())){
            return in.readAllBytes();
        } catch (IOException e) {
            log.error("read data error - {}",bc.getId().toString(), e);
            return null;
        }
    }
}
