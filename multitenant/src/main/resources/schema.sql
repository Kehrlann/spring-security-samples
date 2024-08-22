create schema if not exists red;
create schema if not exists black;

create table if not exists red.todo
(
    id   SERIAL PRIMARY KEY NOT NULL,
    text TEXT               NOT NULL
);


create table if not exists black.todo
(
    id   SERIAL PRIMARY KEY NOT NULL,
    text TEXT               NOT NULL
);