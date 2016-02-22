# --- !Ups

CREATE TABLE "users" (
    id bigserial primary key,
    login varchar(255) UNIQUE NOT NULL,
    name varchar(255),
    githubId varchar(255) UNIQUE NOT NULL,
    pictureUrl varchar(255) NOT NULL,
    githubUrl varchar(255) NOT NULL,
    email varchar(255)
);

CREATE TABLE "userProgress" (
    id bigserial primary key,
    userId bigint not null,
    libraryName varchar(255) not null,
    sectionName varchar(255) not null,
    method varchar(255) not null,
    version int not null,
    exerciseType varchar(255) not null,
    args text[],
    succeeded boolean not null default false,
FOREIGN KEY (userId) REFERENCES users (id));

# --- !Downs

DROP TABLE "userProgress";
DROP TABLE "users";