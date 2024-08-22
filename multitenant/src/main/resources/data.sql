set search_path to "black";
truncate table todo;
insert into todo(text) values('black one');
insert into todo(text) values('black two');

set search_path to "red";
truncate table todo;
insert into todo(text) values('red one');
insert into todo(text) values('red two');