# --- !Ups

CREATE TABLE "User" (
    id serial primary key,
    login varchar(255) unique not null,
    name varchar(255) not null,
    githubId varchar(255) not null,
    pictureUrl varchar(255) not null,
    githubUrl varchar(255) not null,
    email varchar(255) not null
);

# --- !Downs

DROP TABLE "User";
