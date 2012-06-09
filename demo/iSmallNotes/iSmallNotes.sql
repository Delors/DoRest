/*
 Navicat PostgreSQL Data Transfer

 Source Server         : Localhost - SmallNotes
 Source Server Version : 90004
 Source Host           : localhost
 Source Database       : SmallNotes
 Source Schema         : public

 Target Server Version : 90004
 File Encoding         : utf-8

 Date: 07/04/2011 23:17:23 PM
*/

-- ----------------------------
--  Sequence structure for "TAGS_id_seq"
-- ----------------------------
DROP SEQUENCE IF EXISTS "TAGS_id_seq";
CREATE SEQUENCE "TAGS_id_seq" INCREMENT 1 START 54 MAXVALUE 9223372036854775807 MINVALUE 1 CACHE 1;
ALTER TABLE "TAGS_id_seq" OWNER TO "SmallNotesAdmin";

-- ----------------------------
--  Sequence structure for "notes_id_seq"
-- ----------------------------
DROP SEQUENCE IF EXISTS "notes_id_seq";
CREATE SEQUENCE "notes_id_seq" INCREMENT 1 START 20 MAXVALUE 9223372036854775807 MINVALUE 1 CACHE 1;
ALTER TABLE "notes_id_seq" OWNER TO "SmallNotesAdmin";

-- ----------------------------
--  Sequence structure for "users_id_seq"
-- ----------------------------
DROP SEQUENCE IF EXISTS "users_id_seq";
CREATE SEQUENCE "users_id_seq" INCREMENT 1 START 2 MAXVALUE 9223372036854775807 MINVALUE 1 CACHE 1;
ALTER TABLE "users_id_seq" OWNER TO "SmallNotesAdmin";

-- ----------------------------
--  Table structure for "USERS"
-- ----------------------------
DROP TABLE IF EXISTS "USERS";
CREATE TABLE "USERS" (
	"id" int8 NOT NULL DEFAULT nextval('users_id_seq'::regclass),
	"email" varchar(2048) NOT NULL,
	"pwd" varchar(64) NOT NULL DEFAULT '46210dddc66714c3d8d226711510cf8421774214016c508c72a833a05370f6b5'::bpchar,
	"name" varchar(1024) NOT NULL,
	"family_name" varchar(1024) NOT NULL
)
WITH (OIDS=FALSE);
ALTER TABLE "USERS" OWNER TO "SmallNotesAdmin";

-- ----------------------------
--  Records of "USERS"
-- ----------------------------
BEGIN;
INSERT INTO "USERS" VALUES ('2', 'alice@familie-eichberg.de', 'Alice''sSmallNotes', 'Alice', 'Eichberg');
INSERT INTO "USERS" VALUES ('1', 'mail@michael-eichberg.de', 'MySmallNotes', 'Michael', 'Eichberg');
COMMIT;

-- ----------------------------
--  Table structure for "TAGS"
-- ----------------------------
DROP TABLE IF EXISTS "TAGS";
CREATE TABLE "TAGS" (
	"id" int8 NOT NULL DEFAULT nextval('"TAGS_id_seq"'::regclass),
	"tag" varchar(128) NOT NULL,
	"u_id" int8 NOT NULL
)
WITH (OIDS=FALSE);
ALTER TABLE "TAGS" OWNER TO "SmallNotesAdmin";

-- ----------------------------
--  Records of "TAGS"
-- ----------------------------
BEGIN;
INSERT INTO "TAGS" VALUES ('1', 'My Movies', '1');
INSERT INTO "TAGS" VALUES ('3', 'Alice''s and Michael''s Movies', '1');
INSERT INTO "TAGS" VALUES ('3', 'Michael''s and Alice''s Movies', '2');
INSERT INTO "TAGS" VALUES ('40', 'Haus - Finanzierung', '1');
INSERT INTO "TAGS" VALUES ('42', 'Haus - Bau', '1');
INSERT INTO "TAGS" VALUES ('43', 'Michael''s Movies', '1');
INSERT INTO "TAGS" VALUES ('44', 'Alice''s Movies', '1');
INSERT INTO "TAGS" VALUES ('45', 'Urlaub', '1');
INSERT INTO "TAGS" VALUES ('46', 'asfasdfsadfsdf', '1');
COMMIT;

-- ----------------------------
--  Table structure for "NOTES_TAGS"
-- ----------------------------
DROP TABLE IF EXISTS "NOTES_TAGS";
CREATE TABLE "NOTES_TAGS" (
	"n_id" int8 NOT NULL,
	"t_id" int8 NOT NULL
)
WITH (OIDS=FALSE);
ALTER TABLE "NOTES_TAGS" OWNER TO "SmallNotesAdmin";

-- ----------------------------
--  Records of "NOTES_TAGS"
-- ----------------------------
BEGIN;
INSERT INTO "NOTES_TAGS" VALUES ('1', '1');
INSERT INTO "NOTES_TAGS" VALUES ('2', '1');
INSERT INTO "NOTES_TAGS" VALUES ('3', '1');
INSERT INTO "NOTES_TAGS" VALUES ('9', '42');
INSERT INTO "NOTES_TAGS" VALUES ('10', '43');
INSERT INTO "NOTES_TAGS" VALUES ('11', '43');
INSERT INTO "NOTES_TAGS" VALUES ('12', '43');
INSERT INTO "NOTES_TAGS" VALUES ('13', '43');
INSERT INTO "NOTES_TAGS" VALUES ('14', '45');
INSERT INTO "NOTES_TAGS" VALUES ('15', '41');
INSERT INTO "NOTES_TAGS" VALUES ('16', '43');
INSERT INTO "NOTES_TAGS" VALUES ('17', '46');
INSERT INTO "NOTES_TAGS" VALUES ('18', '46');
INSERT INTO "NOTES_TAGS" VALUES ('19', '43');
INSERT INTO "NOTES_TAGS" VALUES ('20', '1');
COMMIT;

-- ----------------------------
--  Table structure for "NOTES"
-- ----------------------------
DROP TABLE IF EXISTS "NOTES";
CREATE TABLE "NOTES" (
	"id" int8 NOT NULL DEFAULT nextval('notes_id_seq'::regclass),
	"note" varchar(4096) NOT NULL
)
WITH (OIDS=FALSE);
ALTER TABLE "NOTES" OWNER TO "SmallNotesAdmin";

-- ----------------------------
--  Records of "NOTES"
-- ----------------------------
BEGIN;
INSERT INTO "NOTES" VALUES ('1', 'Sucker Punch');
INSERT INTO "NOTES" VALUES ('2', 'Monsters');
INSERT INTO "NOTES" VALUES ('3', 'Skyline');
INSERT INTO "NOTES" VALUES ('9', 'Wohnzimmer zum Süden');
INSERT INTO "NOTES" VALUES ('10', 'Priest');
INSERT INTO "NOTES" VALUES ('11', 'Priest');
INSERT INTO "NOTES" VALUES ('12', 'Conan der Barbar');
INSERT INTO "NOTES" VALUES ('13', 'Skyline');
INSERT INTO "NOTES" VALUES ('14', 'Besuch von Padua');
INSERT INTO "NOTES" VALUES ('15', 'Blubb....');
INSERT INTO "NOTES" VALUES ('16', 'Sucker Punch');
INSERT INTO "NOTES" VALUES ('17', 'Dies ist ein Test... der jedoch nicht 100% erfolgreich ist..., da dass Feld zu klein ist...');
INSERT INTO "NOTES" VALUES ('18', 'etwas mehr Text, damit wir testen können was passiert, wenn das Feld übersteht... ich bin schon gespannt wie ein "Flitzebogen"....
');
INSERT INTO "NOTES" VALUES ('19', 'Thor');
INSERT INTO "NOTES" VALUES ('20', 'Priest');
COMMIT;

-- ----------------------------
--  Function structure for add_note(varchar, int8, varchar)
-- ----------------------------
DROP FUNCTION IF EXISTS "add_note"(varchar, int8, varchar);
CREATE FUNCTION "add_note"(IN user_email varchar, IN tag_id int8, IN note varchar, OUT note_id int8) RETURNS "int8" 
	AS $BODY$
BEGIN
	select nextval('notes_id_seq') into note_id;
	insert into "NOTES"(id,note) values (note_id,note);
	insert into "NOTES_TAGS"(n_id,t_id) values(note_id,tag_id);
END
$BODY$
	LANGUAGE plpgsql
	COST 100
	CALLED ON NULL INPUT
	SECURITY DEFINER
	VOLATILE;
ALTER FUNCTION "add_note"(IN user_email varchar, IN tag_id int8, IN note varchar, OUT note_id int8) OWNER TO "SmallNotesAdmin";
COMMENT ON FUNCTION "add_note"(IN user_email varchar, IN tag_id int8, IN note varchar, OUT note_id int8) IS 'Adds a note to the database and associates the note with the given tag.

This method does not performan authentication and it is the responsibility of the caller to make sure that the tag id is (still) valid. I.e., that this function is called in a transaction.
';


-- ----------------------------
--  Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "TAGS_id_seq" OWNED BY "TAGS"."id";
ALTER SEQUENCE "notes_id_seq" OWNED BY "NOTES"."id";
ALTER SEQUENCE "users_id_seq" OWNED BY "USERS"."id";
-- ----------------------------
--  Primary key structure for table "USERS"
-- ----------------------------
ALTER TABLE "USERS" ADD CONSTRAINT "users_pkey" PRIMARY KEY ("id") NOT DEFERRABLE INITIALLY IMMEDIATE;

-- ----------------------------
--  Indexes structure for table "USERS"
-- ----------------------------
CREATE UNIQUE INDEX "EMAIL_IDX" ON "USERS" USING btree(email ASC NULLS LAST);
CREATE UNIQUE INDEX "USERS_id_key" ON "USERS" USING btree("id" ASC NULLS LAST);

-- ----------------------------
--  Primary key structure for table "TAGS"
-- ----------------------------
ALTER TABLE "TAGS" ADD CONSTRAINT "TAGS_pkey" PRIMARY KEY ("id", "u_id") NOT DEFERRABLE INITIALLY IMMEDIATE;

-- ----------------------------
--  Indexes structure for table "TAGS"
-- ----------------------------
CREATE INDEX "TAGS_id_key" ON "TAGS" USING btree("id" ASC NULLS LAST);
CREATE INDEX "TAGS_user_id_index" ON "TAGS" USING btree(u_id ASC NULLS LAST);
ALTER TABLE "TAGS" CLUSTER ON "TAGS_user_id_index";

-- ----------------------------
--  Primary key structure for table "NOTES_TAGS"
-- ----------------------------
ALTER TABLE "NOTES_TAGS" ADD CONSTRAINT "NOTES_TAGS_pkey" PRIMARY KEY ("n_id", "t_id") NOT DEFERRABLE INITIALLY IMMEDIATE;

-- ----------------------------
--  Indexes structure for table "NOTES_TAGS"
-- ----------------------------
CREATE INDEX "tagToNoteIndex" ON "NOTES_TAGS" USING btree(t_id ASC NULLS LAST);
ALTER TABLE "NOTES_TAGS" CLUSTER ON "tagToNoteIndex";

-- ----------------------------
--  Primary key structure for table "NOTES"
-- ----------------------------
ALTER TABLE "NOTES" ADD CONSTRAINT "NOTES_pkey" PRIMARY KEY ("id") NOT DEFERRABLE INITIALLY IMMEDIATE;

-- ----------------------------
--  Indexes structure for table "NOTES"
-- ----------------------------
CREATE UNIQUE INDEX "NOTES_id_key" ON "NOTES" USING btree("id" ASC NULLS LAST);

