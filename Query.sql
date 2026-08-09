INSERT INTO users (id, created_at, updated_at, username, email, password)
VALUES (gen_random_uuid(), now(), now(), 'test1', 'test1@test.com', 'test1234abcd'),
       (gen_random_uuid(), now(), now(), 'test2', 'test2@test.com', 'test1234abcd'),
       (gen_random_uuid(), now(), now(), 'test3', 'test3@test.com', 'test1234abcd');