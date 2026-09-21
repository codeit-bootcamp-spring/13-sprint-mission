package com.sprint.mission.discodeit.exception.jwt;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;

public class TokenRenewalFailedException extends DiscodeitException {
    public TokenRenewalFailedException() {
        super(ErrorCode.TOKEN_RENEWAL_FAILED);
    }
}
