package com.sprint.mission.discodeit.entity;

// 사용자 별 마지막으로 확인된 접속 시간을 표현하는 도메인 모델
// 사용자의 온라인 상태를 확인하기 위해 활용
// 마지막 접속 시간을 기준으로 현재 로그인한 유저로 판단할 수 있는 메소드를 정의하세요.
// 마지막 접속 시간이 현재 시간으로부터 5분 이내이면 현재 접속 중인 유저로 간주합니다.

import java.time.*;
import java.util.*;

public class ReadStatus extends BaseEntity {

    UUID id;
    UUID userId;
    UUID channelId;

}
