# --- !Ups

CREATE TABLE "UserProgress" (
    id serial primary key,
    userId bigint not null,
    libraryName varchar(255) not null,
    sectionName varchar(255) not null,
    method varchar(255) not null,
    args varchar(255),
    succeeded boolean,
FOREIGN KEY (userId) REFERENCES "User" (id));

# --- !Downs

DROP TABLE "UserProgress";