# --- !Ups

CREATE INDEX "fetchUserProgress_idx" ON "userProgress" ("userid", "libraryname", "sectionname");
CREATE UNIQUE INDEX "userLogin_idx" ON "users" ("login");

# --- !Downs

DROP INDEX "fetchUserProgress_idx";
DROP INDEX "userLogin_idx";
