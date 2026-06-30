package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.BinaryContentCreate;
import com.sprint.mission.discodeit.dto.input.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.input.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
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
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {
    private final MessageRepository mr;
    private final UserRepository ur;
    private final ChannelRepository cr;
    private final BinaryContentRepository bcr;

    @Override
    public Message createMessage(MessageCreateRequest cmi, Optional<List<BinaryContentCreate>> olbcc){

        ur.findByID(cmi.authorId()).orElseThrow(
                () -> new DiscodeitException(
                        "no user by id " + cmi.authorId(),
                        "Message",
                        404
                )
        );
        cr.findById(cmi.channelId()).orElseThrow(
                () -> new DiscodeitException(
                        "no channel by id " + cmi.channelId(),
                        "Message",
                        404
                )
        );

        List<UUID> attsId = olbcc.map(
                lbcc -> lbcc.stream().map(
                        bcc -> {
                            BinaryContent bc = new BinaryContent(
                                    bcc.filename(),
                                    bcc.contentType(),
                                    bcc.size(),
                                    bcc.content()
                            );
                            bcr.save(bc);
                            return bc.getId();
                            }

                ).toList()
        ).orElse(null);



        Message res = new Message(
                cmi.content(),
                cmi.channelId(),
                cmi.authorId(),
                attsId
        );

        mr.save(res);
        return res;
    }

    @Override
    public List<Message> findallByChannelId(UUID cannelID){
        return mr.findByChannelID(cannelID);
    }

    @Override
    public Message updateMessageData(UUID id, MessageUpdateRequest umi){
        Message msg = mr.findById(id)
                .orElseThrow(
                        () -> new DiscodeitException("no message by id" + id,"Message",404)
                );

        msg.setContent(umi.newContent());
        msg.setUpdatedAt();
        mr.save(msg);
        return msg;
    }

    @Override
    public void deleteMessage(UUID id){
        Message msg = mr.findById(id).orElseThrow(
                () -> new DiscodeitException("no message by id" + id,"Message",404)
        );

        // delete attribute
        if (!msg.getAttachmentIds().isEmpty()){
            for (UUID att : msg.getAttachmentIds()){
                   bcr.delete(att);
            }
        }

        mr.delete(id);
    }
}
