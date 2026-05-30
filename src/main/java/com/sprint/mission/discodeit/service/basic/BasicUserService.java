package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;

import java.util.List;
import java.util.UUID;

public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    //의존성 주입을 JCFUserRepository가 아닌 인터페이스 이름으로 하는 이우는
    // 추후 메인에서 객체 생성시

    public BasicUserService (UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public void create(User user) {
        userRepository.save(user);// 나중에 메인에서 BasicUserService객체 생성시
        // JCFUserRepository를 인수로 사용하여 생성한다.-> 서비스로직 이니까 JCF
        //때문에 JCFUserRepository클레스에 정의한save를 사용함.

    }

    @Override
    public User read(UUID id) {
        return userRepository.findById(id);//동일한 이유로
        //CFUserRepository클레스에 정의한 findById를 사용함.
    }

    @Override
    public List<User> readAll() {
        return userRepository.findAll();
    }

    @Override
    public void update(UUID id, String name, String email, String password) {

        User foundUser = read(id);//->userRepository.findById()로 User객체 반환
                                 // User타입 변수에 할당.
        if (foundUser != null){//-> 할당 값이 null이 아니먄
            foundUser.update(name, email, password);//매개 변수로 들어온 값을 객체의 필드에 할당
        }
        //객체 생성시 인수로 받는 JCF클레스에는 update가 없다 그래서 update를 사용 하려면
        //id로 갹채룰 찾아서 객체의 클레스에 있는 update를 호출하여 사용한다.
    } //{File을 인수로 객체를 생성하면 File에서 이전에 저징된 동일 ID의 데이터를 삭제하고
     // 업데이트된 객체를 저장해야하기 떄문에 save가 필요핟.

    //{메인에서 객체 생성시 인수에 JCF를 넣었을때}
    //repository의 필드에 create메서드의 save로 repository에 객체를 저장하지만,
    // update로 팔드를 바꾼 객체의 주소를 repository의 리스트안에서 공유 참고하고 있기 떄문에 update이후 다시 save하지 않는디.



    @Override
    public void delete(UUID id) {
        userRepository.delete(id);
    }
}
