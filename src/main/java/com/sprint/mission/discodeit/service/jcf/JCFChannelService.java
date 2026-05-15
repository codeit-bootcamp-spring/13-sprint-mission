package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFChannelService implements ChannelService {

    private final List<Channel> data = new ArrayList<Channel>();
    private final UserService userService;//여기서 변수를 만들고
    public JCFChannelService(UserService userService) {
        // 메인에서 JCFChannelService 객체 생성시 인자로 userSeervice 할당->
        //-> 객체 생성시 인수에 넣은 userService를 매개변수에 할당
        //this.userService = userService;으로 UserService userService;에 최종 할당.
        this.userService = userService;
    }

    @Override
    public void create(Channel channel) {
        //메인클레스에서 channelService.create(channel);로 호출
        //새로 생성된 channel객체를 매개변수로 할당.
        //data.add(channel);에서 매개변수인 channel객체의 주소를 ArrayList에 추가.
        //void는 반환값이 없으니까 메서드 종료시 실행권이 메인(호출한 곳)으로 복귀
        data.add(channel);
    }

    @Override
    public Channel read(UUID id) {
        //호출: Main에서 channelService.read(channel.getId())로 호출
        // 또는 update(), delete() 내부에서 호출
        for (Channel channel : data) {
            if (channel.getId().equals(id)) {
                //일치하는 channel객체 반환 없으면 null -> 호출한 곳으로 복귀.
                return channel;
            }
        }
        return null;

    }

    @Override
    public List<Channel> readAll() {
        //메인에서 channelService.readALL()로 호출
        //data리스트 전체를 반환하고 호출된 곳으로 복귀.
        return data;
    }

    @Override
    public void update(UUID id, String chName, String description) {
        //메인에서 channelService.update(UUID id, String chName, String description))호출
        //매개변수를 전부 할당 받는다.
        Channel foundChannel = read(id);
        //변수 read(id)호출 read메서드에 UUID id만 할당->반환 받은 객체를 좌항 변수에 할당
        if (foundChannel != null) {
            foundChannel.update(chName, description);
            //null이 아니면 channel class의 update메서드 호출 및 실행권 이동.
            // 필드 수정 후 실행권 복귀
        }
        //더이상 할일이으니까 호출한 곳main으로 복귀.

    }

    @Override
    public void delete(UUID id) {
        //호출:메인에서 channelService.delet(UUID id)로 호출한다.
        data.remove(read(id));
        //read(id)를 호출해서 객체를 반환 받는다.
        //ArrayList에 해당 객체를 삭제
        //메서드 종료 -> 실행권이 메서드를 호출한 곳(main)으로 복귀

    }


}
