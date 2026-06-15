package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.input.CreateMessageInput;
import com.sprint.mission.discodeit.dto.input.UpdateMessageInput;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping({"/api/message","/api/v1/message"})
public class MessageController {

    public final MessageService mss;


    @RequestMapping(value = "", method = RequestMethod.POST)
    public void createMessage(
            @RequestBody CreateMessageInput msi
    ) {
        mss.createMessage(msi);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public void updateMessage(
            @RequestBody UpdateMessageInput msi
    ) {
        mss.updateMessageData(msi);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public void deleteMessage(
            @RequestBody String id
    ){
        mss.deleteMessage(UUID.fromString(id));
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public List<Message> findMessageByUer(
            @RequestBody String userID
    ){
        return mss.findallByChannelId(UUID.fromString(userID));
    }

}
