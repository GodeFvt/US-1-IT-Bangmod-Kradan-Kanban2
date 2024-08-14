SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema taskboard
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `taskboard` ;
CREATE SCHEMA IF NOT EXISTS `taskboard` DEFAULT CHARACTER SET utf8mb3 ;
USE `taskboard` ;

-- -----------------------------------------------------
-- Table `taskboard`.`taskstatus`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `taskboard`.`taskstatus` ;

CREATE TABLE IF NOT EXISTS `taskboard`.`taskstatus` (
  `statusId` INT NOT NULL AUTO_INCREMENT,
  `statusName` VARCHAR(50) NOT NULL,
  `statusDescription` VARCHAR(200) NULL DEFAULT NULL,
  `statusColor` VARCHAR(10) NULL DEFAULT '#828282',
  PRIMARY KEY (`statusId`),
  UNIQUE INDEX `idtaskstatus_UNIQUE` (`statusId` ASC) VISIBLE,
  UNIQUE INDEX `statusName_UNIQUE` (`statusName` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 1
DEFAULT CHARACTER SET = utf8mb3;

-- -----------------------------------------------------
-- Table `taskboard`.`tasklists`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `taskboard`.`tasklists` ;

CREATE TABLE IF NOT EXISTS `taskboard`.`tasklists` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(100) NOT NULL,
  `description` VARCHAR(500) NULL DEFAULT NULL,
  `assignees` VARCHAR(30) NULL DEFAULT NULL,
  `statusId` INT NOT NULL DEFAULT 1,
  `createdOn` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedOn` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `fk_tasklists_taskstatus_idx` (`statusId` ASC) VISIBLE,
  CONSTRAINT `fk_tasklists_taskstatus`
    FOREIGN KEY (`statusId`)
    REFERENCES `taskboard`.`taskstatus` (`statusId`))
ENGINE = InnoDB
AUTO_INCREMENT = 1
DEFAULT CHARACTER SET = utf8mb3;

-- -----------------------------------------------------
-- Table `taskboard`.`tasklimit`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `taskboard`.`tasklimit` ;

CREATE TABLE IF NOT EXISTS `taskboard`.`tasklimit` (
  `taskLimitId` INT NOT NULL AUTO_INCREMENT,
  `maximumTask` INT NULL DEFAULT 10,
  `isLimit` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`taskLimitId`),
  UNIQUE INDEX `idtaskstatus_UNIQUE` (`taskLimitId` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 1
DEFAULT CHARACTER SET = utf8mb3;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

COMMIT;