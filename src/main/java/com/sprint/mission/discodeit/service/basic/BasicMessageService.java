package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.BinaryContentCreate;
import com.sprint.mission.discodeit.dto.input.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.input.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.repository.JPABinaryContentRepository;
import com.sprint.mission.discodeit.repository.JPAChannelRepository;
import com.sprint.mission.discodeit.repository.JPAMessageRepository;
import com.sprint.mission.discodeit.repository.JPAUserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {
    private final JPAMessageRepository JPAMessageRepository;
    private final JPAUserRepository JPAUserRepository;
    private final JPAChannelRepository JPAChannelRepository;
    private final JPABinaryContentRepository binaryContentRepository;

    @Override
    @Transactional
    public Message createMessage(MessageCreateRequest cmi, Optional<List<BinaryContentCreate>> olbcc){

        User user = JPAUserRepository.findById(cmi.authorId()).orElseThrow(
                () -> new DiscodeitException(
                        "no user by id " + cmi.authorId(),
                        "Message",
                        404
                )
        );
        Channel channel = JPAChannelRepository.findById(cmi.channelId()).orElseThrow(
                () -> new DiscodeitException(
                        "no channel by id " + cmi.channelId(),
                        "Message",
                        404
                )
        );

        List<BinaryContent> attsId = olbcc.map(
                lbcc -> lbcc.stream().map(
                        bcc -> {
                            BinaryContent bc = new BinaryContent(
                                    bcc.filename(),
                                    bcc.contentType(),
                                    bcc.size(),
                                    bcc.content()
                            );
                            return binaryContentRepository.save(bc);
                        }

                ).toList()
        ).orElse(null);



        Message res = new Message(
                cmi.content(),
                channel,
                user,
                attsId
        );

        JPAMessageRepository.save(res);
        return res;
    }

    @Override
    public List<Message> findallByChannelId(UUID cannelID){
        return JPAMessageRepository.findByChannelId(cannelID);
    }

    @Override
    @Transactional
    public Message updateMessageData(UUID id, MessageUpdateRequest umi){
        Message msg = JPAMessageRepository.findById(id)
                .orElseThrow(
                        () -> new DiscodeitException("no message by id" + id,"Message",404)
                );

        msg.setContent(umi.newContent());
        msg.setUpdatedAt(Instant.now());
        JPAMessageRepository.save(msg);
        return msg;
    }

    @Override
    @Transactional
    public void deleteMessage(UUID id){
        Message msg = JPAMessageRepository.findById(id).orElseThrow(
                () -> new DiscodeitException("no message by id" + id,"Message",404)
        );

        // delete attribute
        if (!msg.getAttachment().isEmpty()){
            binaryContentRepository.deleteAll(msg.getAttachment());
        }

        JPAMessageRepository.delete(msg);
    }
}
