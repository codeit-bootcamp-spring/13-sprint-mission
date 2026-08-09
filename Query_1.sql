INSERT INTO users (id, created_at, updated_at, username, email, password)
VALUES (gen_random_uuid(), now(), now(), 'test1', 'test1@test.com', 'test1234abcd'),
       (gen_random_uuid(), now(), now(), 'test2', 'test2@test.com', 'test1234abcd'),
       (gen_random_uuid(), now(), now(), 'test3', 'test3@test.com', 'test1234abcd');

-- 채널 먼저
INSERT INTO channels (id, created_at, type)
VALUES (gen_random_uuid(), now(), 'PUBLIC');

SELECT id
FROM channels;

INSERT INTO messages (id, created_at, content, channel_id, author_id)
VALUES (gen_random_uuid(), now(), '테스트 메시지1', 'e1592050-e587-4bb4-a5ac-b092973a932c',
        '75089772-4258-420c-8ed4-3c8269c00f2a'),
       (gen_random_uuid(), now(), '테스트 메시지2', 'e1592050-e587-4bb4-a5ac-b092973a932c',
        '406e0e9a-bff0-490c-a410-819cb291233f'),
       (gen_random_uuid(), now(), '테스트 메시지3', 'e1592050-e587-4bb4-a5ac-b092973a932c',
        'cb62393a-c60f-4f56-9e7f-652b20003f69');