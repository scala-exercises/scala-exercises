# --- !Ups

DROP TABLE users;
CREATE TABLE users (
    id integer primary key auto_increment,
    login varchar(255) unique not null,
    name varchar(255) not null,
    github_id varchar(255) not null,
    picture_url varchar(255) not null,
    github_url varchar(255) not null,
    email varchar(255) not null
);

# --- !Downs

DROP TABLE users;
