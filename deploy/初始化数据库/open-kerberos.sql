#开启kerberosSQL
use sdps;
UPDATE sys_global_args set arg_value='true' where arg_type='kerberos' and arg_key='enable';
UPDATE sdps_cluster set kerberos=1;