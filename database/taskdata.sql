INSERT INTO `taskboard`.`taskstatus` (`statusId`,`statusName`, `statusDescription`,`statusColor`,`boardId`) VALUES
(1,'No Status','A status has not been assigned','#828282',null),
(2,'To Do','The task is included in the project','#e3f20d',null),
(3,'In Progress','The task is being worked on','#1483c8',null),
(4,'Done','The task has been completed','#008f1d',null);

COMMIT;

