package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;
import lombok.*;
import org.springframework.stereotype.*;

import java.util.*;
import java.util.stream.*;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserRepository userRepository;

    @Override
    public MessageResponse find(UUID id) {
       Message message = messageRepository.findById(id)
               .orElseThrow(()-> new NoSuchElementException("메세지 아이디를 찾을 수 없습니다"));

       return MessageResponse.from(message);
    }

    @Override
    public MessageResponse create(MessageRequest.Create request,
                                  List<CreateBinaryContentRequest> createBinaryContentRequests) {

        if (request == null) {
            throw new IllegalArgumentException("메시지 생성 요청은 필수입니다.");
        }

        Channel channel = channelRepository.find(request.channelId());
        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }

        User author = userRepository.find(request.authorId());
        if (author == null) {
            throw new IllegalArgumentException("존재하지 않는 작성자입니다.");
        }

        List<UUID> attachments = new ArrayList<>();
        if (createBinaryContentRequests != null && !createBinaryContentRequests.isEmpty()) {
            attachments = createBinaryContentRequests.stream()
                    .map(binaryRequest -> {
                        // 객체 생성
                        BinaryContent binaryContent = new BinaryContent(
                                binaryRequest.fileName(),
                                (long) binaryRequest.bytes().length,
                                binaryRequest.contentType(),
                                binaryRequest.bytes()
                        );
                        binaryContentRepository.create(binaryContent);
                        return binaryContent.getId();
                    })
                    .toList();
        }

        Message message = new Message(
                request.content(),
                request.channelId(),
                request.authorId(),
                attachments
        );

       messageRepository.create(message);

        return MessageResponse.from(message);
    }


    @Override
    public List<MessageResponse> findAllByChannelId(UUID channelId) {
        if (channelId == null) {
            throw new IllegalArgumentException("채널 ID는 필수입니다.");
        }

        if (channelRepository.find(channelId) == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }

        return messageRepository.findAllByChannelId(channelId)
                .stream()
                .map(MessageResponse::from)
                .toList();

    }
    @Override
    public MessageResponse update(UUID id, MessageRequest.Update request) {
      String newContent = request.content();
      Message message = messageRepository.findById(id)
              .orElseThrow(()-> new NoSuchElementException("메세지 아이디를 찾을 수 없습니다."));
      message.update(newContent);
      return MessageResponse.from(message);
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("메세지 ID는 필수입니다.");
        }
        if (!messageRepository.exists(id)) {
            throw new IllegalArgumentException("존재하지 않는 메세지 ID입니다.");
        }
        List<BinaryContent> attachments =
                binaryContentRepository.findAllByMessageId(id);

        attachments.forEach(
                attachment -> binaryContentRepository.delete(attachment.getId())
        );
        messageRepository.delete(id);
    }
}
