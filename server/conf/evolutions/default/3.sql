# --- !Ups

ALTER TABLE "userProgress" ADD COLUMN "updatedAt" timestamp DEFAULT current_timestamp; 

# --- !Downs

ALTER TABLE "userProgress" DROP COLUMN "updatedAt";


