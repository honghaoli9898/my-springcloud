-- 初始化数据库表结构

declare 
  num number;
begin 
  select count(1) into num from user_tables where table_name = upper('demo') ;
  if 
    num > 0 then execute immediate 'drop table demo' ;
  end if;
end;
/
create table demo
(
  ID	VARCHAR2(32) primary key not null,
  CODE   VARCHAR2(50),
  NAME   VARCHAR2(50),
  CREATE_USER  VARCHAR2(20),
  UPDATE_USER VARCHAR2(20),
  CREATE_TIME	DATE,
  UPDATE_TIME   DATE,
  IS_DELETED    CHAR(1)
);
comment on column demo.id is 'id';
comment on column demo.CODE is '编码';
comment on column demo.NAME is '名称';
comment on column demo.CREATE_USER is '创建人';
comment on column demo.CREATE_TIME is '创建时间';
comment on column demo.UPDATE_USER is '修改人';
comment on column demo.UPDATE_TIME is '修改时间';
comment on column demo.IS_DELETED is '逻辑删除';