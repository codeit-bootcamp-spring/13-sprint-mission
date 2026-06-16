package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.input.CreateMessageInput;
import com.sprint.mission.discodeit.dto.input.IDRequestInput;
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


    // msg id 를 반납하도록 해야하나?
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public void sandMessage(
            @RequestBody CreateMessageInput msi
    ) {
        mss.createMessage(msi);
    }

    @RequestMapping(value = "/", method = RequestMethod.PATCH)
    public void modifyMessage(
            @RequestBody UpdateMessageInput msi
    ) {
        mss.updateMessageData(msi);
    }

    @RequestMapping(value = "/", method = RequestMethod.DELETE)
    public void deleteMessage(
            @RequestBody IDRequestInput id
    ){
        mss.deleteMessage(id.getID());
    }

    @RequestMapping(value = "/byChannel", method = RequestMethod.POST)
    public List<Message> findMessageByUser(
            @RequestBody IDRequestInput id
    ){
        return mss.findallByChannelId(id.getID());
    }

}
