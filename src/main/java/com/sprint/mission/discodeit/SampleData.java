package com.sprint.mission.discodeit;

import java.util.ArrayList;
import java.util.List;

public class SampleData {

    public static final List<String> names = new ArrayList<>();
    public static void loadSampleNames() {
        names.add("시나모롤");
        names.add("헬로키티");
        names.add("마이멜로디");
        names.add("쿠로미");
        names.add("폼폼푸린");
        // 창고 초기 데이터 추가 완료
    }

    public static final List<String> titles = new ArrayList<>();
    public static void loadChannelTitles() {
        titles.add("공지사항");
        titles.add("DM");
        titles.add("엄마님과의 대화");
        titles.add("가족 공지방");
        titles.add("음성채널");
        // 채널명 초기 정보 추가 완료
    }

    public static final List<String> messages = new ArrayList<>();
    public static void loadMessages() {
        messages.add("안녕히 주무세요");
        messages.add("이 편지는 영국에서 시작되어...");
        messages.add("자니...? 자...? 문득 네 생각이 나서 연락해봤어.");
        messages.add("나비보벳따우 나비보벱띠");
        messages.add("야 ㅁㅊㅁㅊ 이번 굿즈 신상 봤음??");
    }
}
