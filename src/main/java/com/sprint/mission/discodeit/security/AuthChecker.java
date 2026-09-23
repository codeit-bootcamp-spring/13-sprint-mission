package com.sprint.mission.discodeit.security;


import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component("AuthChecker")
@RequiredArgsConstructor
public class AuthChecker {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;


    @Transactional(readOnly = true)
    public boolean messageOwner(UUID id, String username){
        Message message = messageRepository.findById(id)
                .orElseThrow(RuntimeException::new);

        return message.getAuthor().getUsername().equals(username);
    }

    @Transactional(readOnly = true)
    public boolean isSameUser(UUID id, String username){
        User user = userRepository.findById(id).orElseThrow(RuntimeException::new);
        return user.getUsername().equals(username);
    }


}
