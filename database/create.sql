CREATE DATABASE nopassword;

\c nopassword;

CREATE TABLE users
(
	id SERIAL PRIMARY KEY,
	valid BOOLEAN DEFAULT TRUE,
	address TEXT,
	count INT DEFAULT 0,
	contact BOOLEAN DEFAULT FALSE
);

CREATE TABLE names
(
	id SERIAL PRIMARY KEY,
	user_id INT UNIQUE REFERENCES users(id),
	name TEXT UNIQUE
);

