# --- !Ups

CREATE TABLE "users" (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255),
    githubId VARCHAR(255) UNIQUE NOT NULL,
    pictureUrl VARCHAR(255) NOT NULL,
    githubUrl VARCHAR(255) NOT NULL,
    email VARCHAR(255)
);

CREATE TABLE "userProgress" (
    id BIGSERIAL PRIMARY KEY,
    userId BIGINT NOT NULL,
    libraryName VARCHAR(255) NOT NULL,
    sectionName VARCHAR(255) NOT NULL,
    method VARCHAR(255) NOT NULL,
    version int NOT NULL,
    exerciseType VARCHAR(255) NOT NULL,
    args text[] NOT NULL,
    succeeded BOOLEAN NOT NULL DEFAULT false,
FOREIGN KEY (userId) REFERENCES users (id));

# --- !Downs

DROP TABLE "userProgress";
DROP TABLE "users";