# postgres数据库实时日志采集

# 	原理
基于postgresql 逻辑复制功能 通过将wal日志解析成sql 发送至kafka ，最终在consumer端进行wal 日志的sql 转换 实现pg实例到不同异构数据源的一个实时同步 

# 架构图
 [Architecture](./src/main/resources/architecture.png)
 

# 配置文件config.properties说明

- dingTalk_token:预警钉钉 token
- decoding :postgres slot 编码默认使用test_decoding
- pg_url : postgres jdbc 连接串 
- pg_user :postgres 高级账号
- pg_password : postgres 密码
- slot_name : postgres 数据库 slot 名称 
- lsn_file : 保存postgres  lsn 文件路径
- kafka_host : cdc 采集到postgres 日志发送到 kafka 的 broker 地址
- kafka_topic :  发送到 Kafka 的 topic 名称
- jks_path : kafka 认证jks文件   使用kafka默认连接则不需要该参数

# 常驻后台启动

 ``` shell
   
   nohup java -Xms8g -Xmx8g  -jar -Dfile.encoding=utf-8  postgresql-cdc.jar  -c  producer.properties -t  tables >> /dev/null 2>&1 &
 ```


# pg数据库相关配置
 -  检查 [wal_level](https://www.postgresql.org/docs/14/sql-altersystem.html) 的值是否是 ：logic 如果不是需要改为 logic 注意修改后需重启实例
 - 检查是否有slot  查看命令：
  ```sql
      select  * from pg_replication_slots ; 

```


如果没有需要创建 创建命令：
   ```sql 
      select * from pg_create_logical_replication_slot('regression_slot', 'test_decoding');
  ```