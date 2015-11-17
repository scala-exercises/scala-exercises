# --- !Ups

CREATE TABLE followups (
    id serial primary key,
    text text
);

INSERT INTO followups(text) VALUES('prueba');


# --- !Downs

DROP TABLE followups;
