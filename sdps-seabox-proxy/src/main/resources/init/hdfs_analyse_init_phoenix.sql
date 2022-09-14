CREATE SCHEMA IF NOT EXISTS "HDFS_NM";
USE "HDFS_NM";

CREATE TABLE IF NOT EXISTS HDFS_NM.TB_DIR_INFO (
                                                   CLUSTER VARCHAR NOT NULL,
                                                   PATH VARCHAR NOT NULL,
                                                   DAY_TIME VARCHAR NOT NULL,
                                                   TOTAL_FILE_NUM BIGINT,
                                                   TOTAL_FILE_SIZE BIGINT,
                                                   AVG_FILE_SIZE DOUBLE,
                                                   TOTAL_BLOCK_NUM BIGINT,
                                                   TOTAL_SMALL_FILE_NUM BIGINT,
                                                   TOTAL_EMPTY_FILE_NUM BIGINT,
                                                   PATH_INDEX INTEGER,
                                                   TYPE INTEGER,
                                                   TYPE_VALUE VARCHAR,
                                                   TENANT VARCHAR,
                                                   MODIFICATION_TIME VARCHAR,
                                                   ACCESS_TIME VARCHAR,
                                                   TEMPERATURE INTEGER,
                                                   CONSTRAINT PK PRIMARY KEY (CLUSTER, PATH, DAY_TIME)
    ) IMMUTABLE_ROWS=true, UPDATE_CACHE_FREQUENCY=NEVER, APPEND_ONLY_SCHEMA=true, VERSIONS=1, DATA_BLOCK_ENCODING='FAST_DIFF', SALT_BUCKETS=5, COMPRESSION='Snappy';

-- 参数说明:
-- 可变索引		immutable_rows
-- 更新缓存频率	update_cache_frequency
--      【允许用户声明服务器检查元数据更新的频率（例如添加或删除表列或更新表统计信息）默认：ALWAYS(永远),NEVER(从不),毫秒数值;
--      ALWAYS值会导致客户端每次执行一个引用表的语句（或每次提交一个UPSERT VALUES语句）一次就检查服务器。】
-- 只添加模式		append_only_schema
--      (从Phoenix 4.8开始可用)，当为true时声明只会添加列，而不会从表中删除列。
--      通过设置这个选项，当客户端已经在CREATE TABLE/VIEW IF NOT EXISTS语句中声明了所有列时，我们可以防止从客户端到服务器的RPC获取表元数据。
-- 保留版本数		versions
-- 数据块编码		data_block_encoding
--      使用压缩或编码是必须的。SNAPPY和FAST_DIFF都是很好的选择。
--      FAST_DIFF编码默认在所有Phoenix表上自动启用，并且通过允许更多的数据适合块缓存，几乎总是可以提高整体读取延迟和吞吐量。注意：FAST_DIFF编码会增加请求处理过程中产生的垃圾。
-- 盐桶			salt_buckets
-- 压缩格式		compression


-- Phoenix Salted Table
-- 盐表:防止hbase表rowkey设计为自增序列而引发热点region读和热点region写而采取的一种表设计方式
-- 通过在创建表的时候指定salt_buckets来实现pre-split(预分割)
-- 默认情况下，对salted table创建二级索引，二级索引表会随同源表切进行Salted切分，salt_buckets与源表保持一致。
-- 当然，在创建二级索引表的时候也可以自定义salt_buckets的数量，phoenix没有强制它的数量必须跟源表保持一致。
