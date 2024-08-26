set search_path to "blue";
truncate table todo;
insert into todo(text) values('blue one');
insert into todo(text) values('blue two');
truncate table app_user;
insert into app_user(username, password) values('blue-user', '{bcrypt}$2y$10$gZ9FExFNPFxjTHgeVdaxEulkFkGS4QQHGROAKH4y51VY1YHmocr7e');

set search_path to "red";
truncate table todo;
insert into todo(text) values('red one');
insert into todo(text) values('red two');
truncate table app_user;
insert into app_user(username, password) values('red-user', '{bcrypt}$2y$10$gZ9FExFNPFxjTHgeVdaxEulkFkGS4QQHGROAKH4y51VY1YHmocr7e');