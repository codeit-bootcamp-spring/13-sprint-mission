package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFMessageService implements MessageService {
    private final List<Message> data = new ArrayList<Message>();
    //메세지 객체를저장하는 리스트, 재할당 방지 final
    private final UserService userService;
    //추후 메인 클레스에서 사용
    private final ChannelService channelService;
    //추후 메인 클레스에서 사용
    public JCFMessageService(UserService userService, ChannelService channelService) {
        //호출: main에서 new JCFMessageService(UserService userService, ChannelService channelServic);로 호출
        // 각각 넘겨받은 구현체 주소를 필드 참조 변수에 할당
        this.userService = userService;
        this.channelService = channelService;
    }

    @Override
    public void create(Message message) {
        //main에서 MessageService.cerate(Message message);로 호출
        //메세지객체 주소가 들어있는 참조변수를 리스트에 저장한다.
        //호출 된 곳으로 복귀.
        data.add(message);
    }

    @Override
    public Message read(UUID id) {
        //호출 경로1: main클레스에서 MessageService.read(UUID id)로 호출
        //호출 경로 2: update 또는 delete에서 호출.
        //리스트 안에 메세지 객체의 주소 값을 반환 한다.
        //반환 후 호출했던 곳으로 복귀
        for (Message message : data) {
            if (message.getId().equals(id)) {
                return message;
            }
        }
        return null;
    }

    @Override
    public List<Message> readAll() {
        //main클레스에서 MessageService.readALL()로 호출
        //리스트의 모든 값을 반환 후 호출한 곳으로 복귀
        return data;
    }

    @Override
    public void update(UUID id, String message) {
        //main 클레스에서 MessageService.update(UUID id, String message)로 호출
        Message foundMessage = read(id);
        if (foundMessage != null) {
            //updateMessage(message);에 매개변수를 할당하고 메서드 종료
            //호출 한 곳으로 복귀.(updateMessage(message);로 이동후 모든 과정이 끝나면 거꾸로 돌아감.)
            foundMessage.updateMessage(message);
        }

    }

    @Override
    public void delete(UUID id) {
        //main에서 MessageService.delete(UUID id)로 호츌
        //동일 아디를 갖는 객체를 삭제하고 메서드 종료 후 호출 위치로 복귀.
        data.remove(read(id));

    }
}
