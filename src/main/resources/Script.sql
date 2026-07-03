
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- binary_contents 테이블 생성
CREATE TABLE binary_contents(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    file_name VARCHAR(255) NOT NULL,
    size bigint NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    bytes bytea NOT NULL
);


-- 유저 테이블 생성
-- binary_contents(id)를 참조, 연관 엔티티 삭제 시 NULL로 변경
CREATE TABLE users(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(60) NOT NULL,
    profile_id UUID UNIQUE references binary_contents(id) ON DELETE SET NULL
);

-- 채널 테이블 생성
CREATE TABLE channels(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz,
    name VARCHAR(100),
    description VARCHAR(500),
    type VARCHAR(10) NOT NULL CHECK ( type IN ('PUBLIC', 'PRIVATE'))
);

-- userStatus 테이블 생성
-- 유저 테이블 id를 참조, 연관 엔티티 삭제 시 같이 삭제
CREATE TABLE user_statuses(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz,
    user_id UUID UNIQUE NOT NULL references users(id) ON DELETE CASCADE,
    last_active_at timestamptz NOT NULL
);

-- read_statuses 테이블 생성
-- 유저 테이블 유저 아이디 참조, 연관 엔티티 삭제 시 같이 삭제
-- 채널 테이블 채널 아이디 참조, 연관 엔티티 삭제 시 같이 삭제
CREATE TABLE read_statuses(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz,
    user_id UUID NOT NULL references users(id) ON DELETE CASCADE,
    channel_id UUID NOT NULL references channels(id) ON  DELETE CASCADE,
    last_read_at timestamptz NOT NULL,
    CONSTRAINT uk_read_statuses_user_channel UNIQUE(user_id, channel_id)
);

-- 메시지 테이블 생성
CREATE TABLE messages(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz,
    content text,
    channel_id UUID NOT NULL references channels(id) ON DELETE CASCADE,
    author_id UUID references users(id) ON DELETE SET NULL
);

-- message_attachments 테이블 생성
-- messages(id), binary_contents(id) 복합키 생성
CREATE TABLE message_attachments(
    message_id UUID NOT NULL references messages(id) ON DELETE CASCADE,
    attachment_id UUID NOT NULL references binary_contents(id) ON DELETE CASCADE,
    PRIMARY KEY (message_id, attachment_id)
);





