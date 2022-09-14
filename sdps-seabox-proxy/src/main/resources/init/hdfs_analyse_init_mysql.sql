CREATE DATABASE IF NOT EXISTS sdps;

USE sdps;

CREATE TABLE IF NOT EXISTS `sdps_hdfs_file_stats` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `cluster_id` BIGINT(20) NOT NULL
    `cluster` VARCHAR(255) DEFAULT NULL,
    `day_time` VARCHAR(255) DEFAULT NULL,
    `type` VARCHAR(255) DEFAULT NULL,
    `type_key` VARCHAR(255) DEFAULT NULL,
    `type_value_num` BIGINT(20) DEFAULT NULL,
    `type_value_size` BIGINT(20) DEFAULT NULL,
    `create_time` DATETIME DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE (`cluster`, `day_time`, `type`, `type_key`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `sdps_hdfs_db_table` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `cluster_id` BIGINT(20) NOT NULL,
    `cluster` VARCHAR(255) DEFAULT NULL,
    `db_name` VARCHAR(255) DEFAULT NULL,
    `table_name` VARCHAR(255) DEFAULT NULL,
    `type` VARCHAR(255) DEFAULT NULL,
    `category` VARCHAR(255) DEFAULT NULL,
    `path` VARCHAR(255) DEFAULT NULL,
    `create_time` DATETIME DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

