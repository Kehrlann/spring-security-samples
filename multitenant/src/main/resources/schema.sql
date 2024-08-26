create schema if not exists red;
create schema if not exists blue;

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

create table if not exists blue.todo
(
    id   serial primary key not null,
    text text               not null
);

create table if not exists blue.app_user
(
    id       serial primary key not null,
    username text unique        not null,
    password text               not null
);