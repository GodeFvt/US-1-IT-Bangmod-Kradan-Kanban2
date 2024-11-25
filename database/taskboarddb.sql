SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `taskboard` ;
CREATE SCHEMA IF NOT EXISTS `taskboard` DEFAULT CHARACTER SET utf8mb3 ;
USE `taskboard` ;

-- -----------------------------------------------------
-- Table `taskboard`.`users`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `taskboard`.`users` (
  `oid` VARCHAR(40) NOT NULL,
  `username` VARCHAR(50) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `email` VARCHAR(150) NOT NULL,
  PRIMARY KEY (`oid`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `taskboard`.`boards`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `taskboard`.`boards` (
  `boardId` VARCHAR(10) NOT NULL,
  `boardName` VARCHAR(200) NOT NULL,
  `description` VARCHAR(500) NULL DEFAULT NULL,
  `isCustomStatus` TINYINT NOT NULL DEFAULT '0',
  `oid` VARCHAR(40) NOT NULL,
  `visibility` ENUM('PUBLIC','PRIVATE') NOT NULL DEFAULT 'PRIVATE',
  `createdOn` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedOn` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`boardId`),
  INDEX `fk_board_user_idx` (`oid` ASC) VISIBLE,
  CONSTRAINT `fk_board_user`
    FOREIGN KEY (`oid`)
    REFERENCES `taskboard`.`users` (`oid`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `taskboard`.`boards_has_users`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `taskboard`.`collaboration` (
  `boardId` VARCHAR(10) NOT NULL,
  `oid` VARCHAR(40) NOT NULL,
  `access` ENUM('READ', 'WRITE') NOT NULL DEFAULT 'READ',
  `isPending` TINYINT(1) NOT NULL DEFAULT '1',
  `addedOn` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`boardId`, `oid`),
  INDEX `fk_boards_has_users_users1_idx` (`oid` ASC) VISIBLE,
  INDEX `fk_boards_has_users_boards1_idx` (`boardId` ASC) VISIBLE,
  CONSTRAINT `fk_boards_has_users_boards1`
    FOREIGN KEY (`boardId`)
    REFERENCES `taskboard`.`boards` (`boardId`),
  CONSTRAINT `fk_boards_has_users_users1`
    FOREIGN KEY (`oid`)
    REFERENCES `taskboard`.`users` (`oid`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `taskboard`.`tasklimits`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `taskboard`.`tasklimits` (
  `taskLimitId` INT NOT NULL AUTO_INCREMENT,
  `maximumTask` INT NULL DEFAULT '10',
  `isLimit` TINYINT(1) NOT NULL DEFAULT '0',
  `boardId` VARCHAR(10) NOT NULL,
  PRIMARY KEY (`taskLimitId`),
  UNIQUE INDEX `idtaskstatus_UNIQUE` (`taskLimitId` ASC) VISIBLE,
  INDEX `fk_tasklimit_board1_idx` (`boardId` ASC) VISIBLE,
  CONSTRAINT `fk_tasklimit_board1`
    FOREIGN KEY (`boardId`)
    REFERENCES `taskboard`.`boards` (`boardId`))
ENGINE = InnoDB
AUTO_INCREMENT = 1
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `taskboard`.`taskstatus`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `taskboard`.`taskstatus` (
  `statusId` INT NOT NULL AUTO_INCREMENT,
  `statusName` VARCHAR(50) NOT NULL,
  `statusDescription` VARCHAR(200) NULL DEFAULT NULL,
  `statusColor` VARCHAR(10) NULL DEFAULT '#828282',
  `boardId` VARCHAR(10) NULL DEFAULT NULL,
  PRIMARY KEY (`statusId`),
  UNIQUE INDEX `idtaskstatus_UNIQUE` (`statusId` ASC) VISIBLE,
  INDEX `fk_taskstatus_boards1_idx` (`boardId` ASC) VISIBLE,
  CONSTRAINT `fk_taskstatus_boards1`
    FOREIGN KEY (`boardId`)
    REFERENCES `taskboard`.`boards` (`boardId`))
ENGINE = InnoDB
AUTO_INCREMENT = 1
DEFAULT CHARACTER SET = utf8mb3;

-- -----------------------------------------------------
-- Table `taskboard`.`taskattachment`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `taskboard`.`taskattachment` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `filename` VARCHAR(255) NULL,
  `storedName` VARCHAR(255) NULL,
  `contentType` VARCHAR(255) NULL,
  `fileData` LONG NULL,
  `uploadedAt` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  `taskId` INT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `storedName_UNIQUE` (`storedName` ASC) VISIBLE,
  INDEX `fk_taskattachment_tasklists_idx` (`taskId` ASC) VISIBLE,
  CONSTRAINT `fk_taskattachment_tasklists`
    FOREIGN KEY (`taskId`)
    REFERENCES `taskboard`.`tasklists` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 1
DEFAULT CHARACTER SET = utf8mb3;

-- -----------------------------------------------------
-- Table `taskboard`.`tasklists`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `taskboard`.`tasklists` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(100) NOT NULL,
  `description` VARCHAR(500) NULL DEFAULT NULL,
  `assignees` VARCHAR(30) NULL DEFAULT NULL,
  `statusId` INT NOT NULL DEFAULT '1',
  `createdOn` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedOn` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `boardId` VARCHAR(10) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_tasklists_board1_idx` (`boardId` ASC) VISIBLE,
  INDEX `fk_tasklists_taskstatus` (`statusId` ASC) VISIBLE,
  CONSTRAINT `fk_tasklists_board1`
    FOREIGN KEY (`boardId`)
    REFERENCES `taskboard`.`boards` (`boardId`),
  CONSTRAINT `fk_tasklists_taskstatus`
    FOREIGN KEY (`statusId`)
    REFERENCES `taskboard`.`taskstatus` (`statusId`))
ENGINE = InnoDB
AUTO_INCREMENT = 1
DEFAULT CHARACTER SET = utf8mb3;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;