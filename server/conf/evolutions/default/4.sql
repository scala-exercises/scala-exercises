# --- !Ups

CREATE INDEX "libraryname_idx" ON "userProgress" ("libraryname");
CREATE INDEX "sectionname_idx" ON "userProgress" ("sectionname");

# --- !Downs

DROP INDEX "libraryname_idx";
DROP INDEX "sectionname_idx";
