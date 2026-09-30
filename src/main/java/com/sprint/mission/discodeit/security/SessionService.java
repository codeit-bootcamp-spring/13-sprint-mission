package com.sprint.mission.discodeit.security;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionService {
    private final SessionRegistry sessionRegistry;

    public boolean userOnline(String username){
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (
                    principal instanceof DiscodeitUserDetails details
                            && details.getUsername().equals(username)
            ){
                return true;
            }
        }
        return false;
    }

}
