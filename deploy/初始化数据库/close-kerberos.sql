#关闭kerberosSQL
use sdps;
UPDATE sys_global_args set arg_value='false' where arg_type='kerberos' and arg_key='enable';
UPDATE sdps_cluster set kerberos=0;
