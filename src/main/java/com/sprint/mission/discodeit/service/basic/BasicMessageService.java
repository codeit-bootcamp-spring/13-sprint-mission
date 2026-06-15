package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.CreateMessageInput;
import com.sprint.mission.discodeit.dto.input.UpdateMessageInput;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {
    private final MessageRepository mr;
    private final UserRepository ur;
    private final ChannelRepository cr;
    private final BinaryContentRepository bcr;

    @Override
    public void createMessage(CreateMessageInput cmi){

        if (ur.findByID(cmi.getUserID()) == null) throw new RuntimeException();
        if (cr.findById(cmi.getChannelID()) == null) throw new RuntimeException();

        mr.save(new Message(cmi.getUserID(), cmi.getChannelID(), cmi.getMessage(), cmi.getDataIDs()));
    }

    @Override
    public List<Message> findallByChannelId(UUID cannelID){
        return mr.findByChannelID(cannelID);
    }

    @Override
    public void updateMessageData(UpdateMessageInput umi){
        Message msg = mr.find(m -> m.getId().equals(umi.getMessageID())).get(0);
        msg.setText(umi.getText());
        msg.getAttrID().clear();
        for (UUID dataID : umi.getDataIDs()) {
                msg.getAttrID().add(dataID);
        }
        mr.save(msg);
    }

    @Override
    public void deleteMessage(UUID id){
        mr.delete(id);
        bcr.findByAuthorID(id)
                .forEach(b -> bcr.delete(b.getId()));
    }

}
