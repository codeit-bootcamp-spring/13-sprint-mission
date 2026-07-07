package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.controller.docs.MessageControllerDoc;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreate;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping({"/api/messages"})
public class MessageController implements MessageControllerDoc {

    public final MessageService messageService;
    public final PageResponseMapper pageResponseMapper;


    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<PageResponse<MessageDto>> findMessageByChannel(
            @RequestParam(value = "channelId") UUID channelId
            ,@PageableDefault(size = 50) Pageable pageable
//            ,@RequestParam(required = false) Integer page
//            ,@RequestParam(required = false) Integer size
//            ,@RequestParam(required = false) List<String> sort
    ){
        PageResponse<MessageDto> res =  messageService.findallByChannelId(channelId,pageable);
        return ResponseEntity.ok(res);
    }


    @RequestMapping(
            value = "",
            method = RequestMethod.POST,
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseEntity<MessageDto> create(
            @RequestPart(value = "messageCreateRequest") MessageCreateRequest mcr,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> att
    ) {
        Optional<List<BinaryContentCreate>> lbcc =  Optional.ofNullable(att).map( mp ->
                mp.stream().map(m -> {
                    try {
                        return new BinaryContentCreate(
                                m.getOriginalFilename(),
                                m.getContentType(),
                                m.getSize(),
                                m.getBytes()
                        );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
        }).toList());


        MessageDto res = messageService.createMessage(mcr,lbcc);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @RequestMapping(value = "/{messageId}", method = RequestMethod.PATCH)
    public ResponseEntity<MessageDto> modifyMessage(
            @PathVariable UUID messageId,
            @RequestBody MessageUpdateRequest msi
    ) {
        MessageDto res = messageService.updateMessageData(messageId, msi);
        return ResponseEntity.ok(res);
    }

    @RequestMapping(value = "/{messageId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteMessage(
            @PathVariable UUID messageId
    ){
        messageService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }


}
