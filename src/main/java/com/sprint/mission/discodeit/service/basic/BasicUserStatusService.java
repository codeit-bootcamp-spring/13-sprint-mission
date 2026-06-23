package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

//userStatus(사용자 상태) 관련 비즈니스 로직을 처리하는 service 구현체
@RequiredArgsConstructor
@Service
public class BasicUserStatusService implements UserStatusService {
    private final UserStatusRepository userStatusRepository; //사용자 상태 저장소
    private final UserRepository userRepository; //사용자 저장소

    @Override //사용자 상태 생성
    public UserStatus create(UserStatusCreateRequest request){
        //요청 객체에서 사용자 ID 조회
        UUID userId = request.getUserId();

        //사용자 상태 생성 전 사용자 존재 여부 확인
        if (!userStatusRepository.existsById(userId)) {
            throw new NoSuchElementException("User with id " + userId + " does not exist");
        }
        //이미 해당 사용자의 상태 정보가 존재하는지 확인
        if (userStatusRepository.findByUserId(userId).isPresent()) {
            throw new NoSuchElementException("UserStatus with id " + userId + " already exists");
        }
        Instant lastActiveAt = request.getLastActiveAt(); //마지막 활동 시간 조회
        UserStatus userStatus = new UserStatus(userId, lastActiveAt); //userStatus 엔티티 생성
        return userStatusRepository.save(userStatus); //저장 후 반환
    }
    @Override //userStatus ID로 사용자 상태 조회
    public UserStatus find(UUID userStatusId) {
        return userStatusRepository.findById(userStatusId)
                //존재하지 않으면 예외 발생
                .orElseThrow(() -> new NoSuchElementException("UserStatus with id " + userStatusId + " does not exist"));
    }
    @Override //전체 사용자 상태 조회
    public List<UserStatus> findAll() {
        return userStatusRepository.findAll().stream().toList();
    }
    @Override //userStatus ID 기준 상태 정보 수정
    public UserStatus update(UUID userStatusId, UserStatusUpdateRequest request){
        Instant newLastActiveAt = request.getNewLastActiveAt(); //요청 객체에서 새 마지막 활동 시간 조회

        //수정 대상 userStatus 조회
        UserStatus userStatus = userStatusRepository.findById(userStatusId)
                .orElseThrow(() -> new NoSuchElementException("UserStatus with id " + userStatusId + " does not exist"));

        return userStatusRepository.save(userStatus); //수정된 객체 저장 후 반환
    }
    @Override //사용자 ID 기준 상태 정보 수정
    public UserStatus updateByUserId(UUID userId, UserStatusUpdateRequest request){
        Instant newLastActiveAt = request.getNewLastActiveAt(); //변경할 마지막 활동 시간 조회

        UserStatus userStatus = userStatusRepository.findByUserId(userId) //사용자 ID로 상태 정보 조회
                .orElseThrow(() -> new NoSuchElementException("UserStatus with userId " + userId + " not found"));
        userStatus.update(newLastActiveAt); //마지막 활동 시간 변경
        return userStatusRepository.save(userStatus); //변경 내용 저장 후 반환
    }
    @Override //사용자 상태 삭제
    public void delete(UUID userStatusId) {
        //삭제 대상 존재 여부 확인
        if (!userStatusRepository.existsById(userStatusId)) {
            throw new NoSuchElementException("UserStatus with id " + userStatusId + " does not exist");
        }
        //사용자 상태 삭제
        userStatusRepository.deleteById(userStatusId);
    }
}
