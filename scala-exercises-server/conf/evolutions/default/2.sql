# --- !Ups

CREATE TABLE followups (
    id serial primary key,
    login text,
    section text,
    category text,
    status text
);

INSERT INTO followups(login, section, category, status) VALUES('test_user', 'stdlib', 'extractors', 'completed');
INSERT INTO followups(login, section, category, status) VALUES('test_user', 'stdlib', 'pattern matching', 'completed');
INSERT INTO followups(login, section, category, status) VALUES('test_user', 'play', 'controllers', 'completed');
INSERT INTO followups(login, section, category, status) VALUES('test_user', 'akka', 'actors', 'completed');
INSERT INTO followups(login, section, category, status) VALUES('rafaparadela', 'example', 'extractors', 'completed');
INSERT INTO followups(login, section, category, status) VALUES('raulraja', 'example', 'extractors', 'completed');


# --- !Downs

DROP TABLE followups;
