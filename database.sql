CREATE SEQUENCE insult_id_seq;
                                                CREATE TABLE insult
                                                (
                                                  id integer PRIMARY KEY default nextval('insult_id_seq'),
                                                  text text NOT NULL
                                                );
                                                ALTER SEQUENCE insult_id_seq owned by insult.id;