package com.sprint.mission.discodeit.exception.jwt;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class InvalidJwtException extends JwtException {
    public InvalidJwtException() {
        super(ErrorCode.INVALID_JWT);
    }
}
