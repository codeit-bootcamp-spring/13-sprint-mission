package com.sprint.mission.discodeit.entity;

import java.util.*;

// 사용자가 채널 별 마지막으로 메시지를 읽은 시간을 표현하는 도메인 모델
// 사용자별 각 채널에 읽지 않은 메시지를 확인하기 위해 활용
public class UserStatus extends BaseEntity {

    UUID id;
    UUID userId;

}
