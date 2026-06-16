package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.CreateMessageInput;
import com.sprint.mission.discodeit.dto.input.UpdateMessageInput;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.exception.DiscodeitException;
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

        ur.findByID(cmi.userID()).orElseThrow(
                () -> new DiscodeitException("no user by id" + cmi.userID(),"Message",400)
        );
        cr.findById(cmi.channelID()).orElseThrow(
                () -> new DiscodeitException("no channel by id" + cmi.channelID(),"Message",400)
        );

        mr.save(new Message(cmi.userID(), cmi.channelID(), cmi.message(), cmi.dataIDs()));
    }

    @Override
    public List<Message> findallByChannelId(UUID cannelID){
        return mr.findByChannelID(cannelID);
    }

    @Override
    public void updateMessageData(UpdateMessageInput umi){
        Message msg = mr.find(m -> m.getId().equals(umi.getMessageID()))
                .stream()
                .findFirst()
                .orElseThrow(
                        () -> new DiscodeitException("no message" + umi.getMessageID(),"Message",400)
                );

        if (umi.getText() != null) msg.setText(umi.getText());
        if (!umi.getDataIDs().isEmpty()) {
            msg.getAttrID().clear();
            for (UUID dataID : umi.getDataIDs()) {
                msg.getAttrID().add(dataID);
            }
        }
        msg.setUpdatedAt();
        mr.save(msg);
    }

    @Override
    public void deleteMessage(UUID id){
        mr.delete(id);
        bcr.findByAuthorID(id)
                .forEach(b -> bcr.delete(b.getId()));
    }
}
