-- 初始化数据库表结构
SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS demo;

CREATE TABLE `demo` (
  `ID` varchar(32) NOT NULL COMMENT 'id',
  `CODE` varchar(50) NOT NULL COMMENT '代码',
  `NAME` varchar(50) NOT NULL COMMENT '名称',
  `CREATE_USER` varchar(20) DEFAULT NULL COMMENT '创建人',
  `UPDATE_USER` varchar(20) DEFAULT NULL COMMENT '修改人',
  `CREATE_TIME` datetime DEFAULT NULL COMMENT '创建时间',
  `UPDATE_TIME` datetime DEFAULT NULL COMMENT '修改时间',
  `IS_DELETED` char(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;