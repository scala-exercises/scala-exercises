# --- !Ups

CREATE TABLE "users" (
    id bigserial primary key,
    login text UNIQUE NOT NULL,
    name text NOT NULL,
    githubId text NOT NULL,
    pictureUrl text NOT NULL,
    githubUrl text NOT NULL,
    email text NOT NULL    
);

# --- !Downs

DROP TABLE "users";
