-- 
-- Create tables for nopassword
--

-- Start from scratch

DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS tokens;

--  Create the tables

CREATE TABLE users
(
	id SERIAL PRIMARY KEY,
	valid BOOLEAN DEFAULT TRUE,
	address TEXT,
	name TEXT,
	count INT DEFAULT 0,
	contact BOOLEAN DEFAULT FALSE
);

CREATE UNIQUE INDEX ON users(name) WHERE valid;

CREATE TABLE tokens
(
	token BIGINT PRIMARY KEY,
	user_id INT REFERENCES users(id)
);
