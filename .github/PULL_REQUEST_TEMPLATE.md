## 요구사항

### 기본
- [x] 도메인 모델(User, Channel, Message) 구현
- [x] CRUD 기능이 포함된 Service 인터페이스 및 구현체 작성
- [x] JCF 기반 데이터 저장 구현
- [x] File IO 및 객체 직렬화를 활용한 데이터 영속화 구현
- [x] Repository 인터페이스 및 구현체 작성
- [x] 기능 테스트 수행 


### 심화
- [x] Message 생성 시 연관 도메인(User, Channel) 검증 로직 추가 
- [ ] 관심사 분리를 통한 레이어 간 의존성 주입

## 주요 변경사항
- User, Channel, Message 엔티티 구현
- JCF 기반 Service 구현
- File IO 기반 Service 구현
- Repository 계층 추가 및 저장 로직 분리
- Message 생성 시 User, Channel 존재 여부 검증 로직 추가

## 멘토에게
- 셀프 코드 리뷰를 통해 피드백을 확인한 뒤 추가 질문을 이어가겠습니다.
