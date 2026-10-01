package com.sprint.mission.discodeit.security;


import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
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
                .orElseThrow(() -> new MessageNotFoundException(
                        "Authchecker - 메세지가 존재하지 않음. id : {}",
                        id
                ));

        return message.getAuthor().getUsername().equals(username);
    }

    @Transactional(readOnly = true)
    public boolean isSameUser(UUID id, String username){
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(
                "AuthChecker - 유저가 존재하지 않음. id : {}",
                id
        ));
        return user.getUsername().equals(username);
    }


}
