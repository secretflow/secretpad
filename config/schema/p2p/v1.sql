-- delete p2p mode not support sql
delete from main.node_route where route_id = 1 or route_id = 2;
delete from main.node where name = 'alice' or name = 'bob';
delete from main.inst where name = 'alice' or name = 'bob';

--  sqlLite3 optimize
PRAGMA synchronous = NORMAL;
PRAGMA journal_mode = WAL;