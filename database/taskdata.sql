INSERT INTO `taskboard`.`taskstatus` (`statusId`,`statusName`, `statusDescription`,`statusColor`) VALUES
(1,'No Status','A status has not been assigned','#828282'),
(2,'To Do','The task is included in the project','#e3f20d'),
(3,'In Progress','The task is being worked on','#1483c8'),
(4,'Reviewing','The task is being reviewed','#e8814a'),
(5,'Testing','The task is being tested','#913de6'),
(6,'Waiting','The task is waiting for a resource','#fa0000'),
(7,'Done','The task has been completed','#008f1d');

INSERT INTO `taskboard`.`tasklists` (`id`,`title`, `description`,`assignees`, `statusId`,`createdOn`,`updatedOn`) VALUES
(1,'NS01',null,null, 1,'2024-04-22 09:00:00','2024-05-14 09:00:00'),
(2,'TD01', null, null, 2,'2024-04-22 09:05:00','2024-05-14 09:10:00'),
(3,'IP01', null,null, 3,'2024-04-22 09:10:00','2024-05-14 09:20:00'),
(4,'TD02', null,null, 2,'2024-04-22 09:15:00','2024-05-14 09:30:00'),
(5,'DO01', null,null, 7,'2024-04-22 09:10:00','2024-05-14 09:40:00'),
(6,'IP02', null,null, 3,'2024-04-22 09:10:00','2024-05-14 09:50:00');

INSERT INTO `taskboard`.`tasklimit` (`maximumTask`) VALUES ('10');

COMMIT;

