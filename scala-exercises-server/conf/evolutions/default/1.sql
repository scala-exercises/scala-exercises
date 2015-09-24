# --- !Ups

CREATE TABLE users (
    id serial primary key,
    login text,
    name text,
    github_id text,
    picture_url text,
    github_url text,
    email text
);

INSERT INTO users(login, name, github_id, picture_url, github_url, email) VALUES(
    'test_user',
    'Test User',
    '40239432',
    'https://randomuser.me/api/portraits/med/men/23.jpg',
    'https://github.com/rafaparadela',
    'testuser@example.com');


# --- !Downs

DROP TABLE users;
