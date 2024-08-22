create schema if not exists red;
create schema if not exists black;

create table if not exists red.todo
(
    id   serial primary key not null,
    text text               not null
);

create table if not exists red.app_user
(
    id       serial primary key not null,
    username text unique        not null,
    password text               not null
);

create table if not exists black.todo
(
    id   serial primary key not null,
    text text               not null
);

create table if not exists black.app_user
(
    id       serial primary key not null,
    username text unique        not null,
    password text               not null
);