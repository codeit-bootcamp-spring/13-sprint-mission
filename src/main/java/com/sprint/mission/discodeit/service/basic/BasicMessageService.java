package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.mapper.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;
import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
    @Transactional
    public MessageResponse create(CreateMessageRequest.Create request,
                                  List<CreateBinaryContentRequest> createBinaryContentRequests) {

        if (request == null) {
            throw new IllegalArgumentException("메시지 생성 요청은 필수입니다.");
        }

        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널입니다."));

        User author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 작성자입니다."));

        List<BinaryContent> attachments = new ArrayList<>();
        if (createBinaryContentRequests != null && !createBinaryContentRequests.isEmpty()) {
            attachments = createBinaryContentRequests.stream()
                    .map(binaryRequest -> new BinaryContent(
                            binaryRequest.fileName(),
                            (long) binaryRequest.bytes().length,
                            binaryRequest.contentType(),
                            binaryRequest.fileName()
                    ))
                    .toList();
        }

        Message message = new Message(
                request.content(),
                channel,
                author,
                attachments
        );

       messageRepository.save(message);

        return MessageResponse.from(message);
    }


    @Override
    public List<MessageResponse> findAllByChannelId(UUID channelId) {
        if (channelId == null) {
            throw new IllegalArgumentException("채널 ID는 필수입니다.");
        }

        if (channelRepository.existsById(channelId)) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }

        return messageRepository.findByChannelId(channelId)
                .stream()
                .map(MessageResponse::from)
                .toList();

    }
    @Override
    @Transactional
    public MessageResponse update(UUID id, CreateMessageRequest.Update request) {
      String newContent = request.content();
      Message message = messageRepository.findById(id)
              .orElseThrow(()-> new NoSuchElementException("메세지 아이디를 찾을 수 없습니다."));
      message.update(newContent);
      return MessageResponse.from(message);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("메세지 ID는 필수입니다.");
        }
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메세지 ID입니다."));

        messageRepository.delete(message);
    }

    @Override
    public PageResponse<MessageResponse> getMessages(UUID channelId, int page) {
        Pageable pageable = PageRequest.of(page, 50, Sort.by(Sort.Direction.DESC, "createdAt"));

        Slice<Message> messageSlice = messageRepository.findByChannelId(channelId, pageable);

        Slice<MessageResponse> responseSlice = messageSlice.map(MessageResponse::from);

        return PageResponseMapper.fromSlice(responseSlice);

    }
}
