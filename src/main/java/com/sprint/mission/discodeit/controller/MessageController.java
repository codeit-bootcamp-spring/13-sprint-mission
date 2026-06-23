package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.input.BinaryContentCreate;
import com.sprint.mission.discodeit.dto.input.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.input.UpdateMessageInput;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class MessageController {

    public final MessageService mss;



    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<Message>> findMessageByUser(
            @RequestParam(value = "channelId", required = true) UUID channelId
    ){
        List<Message> res =  mss.findallByChannelId(channelId);
        return ResponseEntity.ok(res);
    }


    @RequestMapping(
            value = "",
            method = RequestMethod.POST,
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseEntity<Message> create(
            @RequestPart(value = "messageCreateRequest") MessageCreateRequest mcr,
            @RequestPart(value = "attachments") List<MultipartFile> att
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


        Message res = mss.createMessage(mcr,lbcc);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @RequestMapping(value = "/{messageId}", method = RequestMethod.PATCH)
    public ResponseEntity<Message> modifyMessage(
            @PathVariable UUID messageId,
            @RequestBody UpdateMessageInput msi
    ) {
        Message res = mss.updateMessageData(messageId, msi);
        return ResponseEntity.ok(res);
    }

    @RequestMapping(value = "/{messageId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteMessage(
            @PathVariable UUID messageId
    ){
        mss.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }


}
