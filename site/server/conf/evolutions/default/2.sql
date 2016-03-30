# --- !Ups

ALTER TABLE "userProgress" ADD CONSTRAINT one_evaluation_per_method UNIQUE(userId, libraryName, sectionName, method);

# --- !Downs

ALTER TABLE "userProgress" DROP CONSTRAINT one_evaluation_per_method;
