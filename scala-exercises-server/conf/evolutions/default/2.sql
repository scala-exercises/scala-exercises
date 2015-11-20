# --- !Ups

CREATE TABLE followups (
    id serial primary key,
    login text,
    section text,
    category text,
    status text
);

INSERT INTO followups(login, section, category, status) VALUES('test_user', 'stdlib', 'extractors', 'completed');


# --- !Downs

DROP TABLE followups;
