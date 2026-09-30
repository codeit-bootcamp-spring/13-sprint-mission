package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.projection.MessageProjection;
import com.sprint.mission.discodeit.dto.projection.UserProjection;
import com.sprint.mission.discodeit.dto.request.MultipartFileDto;
import com.sprint.mission.discodeit.dto.request.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.message.MessageUpdateRequest;
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
import com.sprint.mission.discodeit.security.SessionService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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

    private final SessionService sessionService;

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
    public MessageDto createMessage(MessageCreateRequest request, List<MultipartFileDto> attachments){
        User user = getUserOrException(request.authorId());
        Channel channel = getChannelOrException(request.channelId());


        List<BinaryContent> attachFiles = binaryContentRepository.saveAllFromMultipartFileDtoList(attachments);

        Message message = new Message(
                request.content(),
                channel,
                user,
                attachFiles
        );

        messageRepository.save(message);

        log.debug("Message Created - {}", message.getId());


        List<BinaryContentDto> attachmentDtoList = binaryContentRepository
                .getBinaryContentsInIdList(
                        attachFiles.stream().map(BinaryContent::getId).toList()
                )
                .values()
                .stream()
                .toList();


        return mapStructMapper.toDto(
                message,
                getUserDtoFromUser(user),
                getBinaryContentDtoFromAttachments(attachFiles)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MessageDto> findallByChannelIdWithCursor(
            UUID channelId,
            Pageable pageable,
            Instant cursor
    ){
        // 1. query target messages id
        Slice<UUID> query =
                messageRepository.findMessageIdsBuChannelIdWithCursor(
                        channelId,
                        pageable,
                        Optional.of(cursor).orElse(Instant.now())
                );


        // 2. query required info about message.
        List<MessageProjection> messageInfo = messageRepository
                .getMessageProjectionFromIdList(query.getContent())
                .values()
                .stream()
                .toList();

        Map<UUID, UserDto> userList =
                getUserDtoFromUserIdList(
                        messageInfo.stream()
                                .map(MessageProjection::userId)
                                .toList()
                );

        Map<UUID, BinaryContentDto> attachmentList =
                binaryContentRepository.getBinaryContentsInIdList(
                        messageInfo.stream()
                                .map(MessageProjection::attachments)
                                .flatMap(List::stream)
                                .toList()
                );


        // 3. construct MessageDto
        List<MessageDto> contents = messageInfo.stream().map(
                messageProjection -> mapStructMapper.toDto(
                            messageProjection,
                            userList.get(messageProjection.userId()),
                            messageProjection.attachments().stream().map(attachmentList::get).toList()
                )
        ).toList();

        // 4. create Slice Object from query
        Slice<MessageDto> slice = new SliceImpl<>(contents,Pageable.ofSize(query.getSize()),query.hasNext());
        Instant newCursor = contents.get(contents.size() - 1).createdAt();

        return pageResponseMapper.fromSliceWithCursor(slice,newCursor);
    }


    @Override
    @Transactional
    public MessageDto updateMessageData(UUID id, MessageUpdateRequest request){
        Message message = getMessageOrException(id);

        message.update(request.newContent());
        messageRepository.save(message);

        log.info("Message Updated - {}", message.getId());

        return mapStructMapper.toDto(
                message,
                getUserDtoFromUser(message.getAuthor()),
                getBinaryContentDtoFromAttachments(message.getAttachment())
        );
    }

    @Override
    @Transactional
    public void deleteMessage(UUID id){
        Message message = getMessageOrException(id);

        message.getAttachment().forEach(binaryContentRepository::deleteBinaryContent);
        messageRepository.delete(message);

        log.info("Message Deleted - {}", message.getId());
    }




    private Message getMessageOrException(UUID id){
        return messageRepository.findById(id)
                .orElseThrow(
                        () -> new MessageNotFoundException("Message with id - {} not found",id)
                );
    }

    // convert userinfo to userDto
    // duplicated in channel class.
    private Map<UUID, UserDto> getUserDtoFromUserIdList(List<UUID> userIdList){
        List<UserProjection> users = userRepository.getUsersFromIds(userIdList).values().stream().toList();
        Map<UUID,BinaryContentDto> profileList = binaryContentRepository.getBinaryContentsInIdList(
                users.stream().map(UserProjection::profileId).filter(Objects::nonNull).toList()
        );

        return users.stream()
                .map(
                        userProjection -> mapStructMapper.toDto(
                                userProjection,
                                profileList.get(userProjection.profileId()),
                                sessionService.userOnline(userProjection.username())
                        )
                ).collect(Collectors.toMap(
                        UserDto::id,
                        dto -> dto
                ));
    }

    private UserDto getUserDtoFromUser(User user){
        UUID profileId =
                Optional.ofNullable(user.getProfile()).isEmpty()
                ? null
                : user.getProfile().getId();

        return mapStructMapper.toDto(
                user,
                binaryContentRepository.getBinaryContentById(profileId).orElse(null),
                sessionService.userOnline(user.getUsername())
        );
    }

    private List<BinaryContentDto> getBinaryContentDtoFromAttachments(List<BinaryContent> list){
        return binaryContentRepository
                .getBinaryContentsInIdList(
                        list.stream().map(BinaryContent::getId).toList()
                )
                .values()
                .stream()
                .toList();
    }



}
