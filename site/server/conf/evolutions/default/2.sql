# --- !Ups

CREATE TABLE "userProgress" (
    id bigserial primary key,
    userId bigint not null,
    libraryName varchar(255) not null,
    sectionName varchar(255) not null,
    method varchar(255) not null,
    version int not null,
    exerciseType varchar(255) not null,
    args varchar(255),
    succeeded boolean not null default false,
FOREIGN KEY (userId) REFERENCES users (id));

# --- !Downs

DROP TABLE "userProgress";
