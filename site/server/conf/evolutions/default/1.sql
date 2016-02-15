# --- !Ups

CREATE TABLE "users" (
    id bigserial primary key,
    login varchar(255) UNIQUE NOT NULL,
    name varchar(255) NOT NULL,
    githubId varchar(255) UNIQUE NOT NULL,
    pictureUrl varchar(255) NOT NULL,
    githubUrl varchar(255) NOT NULL,
    email varchar(255) NOT NULL
);

# --- !Downs

DROP TABLE "users";
