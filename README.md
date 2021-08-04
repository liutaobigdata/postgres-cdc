## postgres数据库实时日志采集

## 	原理
基于postgresql 逻辑复制功能 通过将wal日志解析成sql 发送至kafka ，最终在consumer端进行wal 日志的sql 转换 实现pg实例到不同异构数据源的一个实时同步 

## 架构图
 [Architecture](./src/main/resources/architecture.png)
 

## 配置文件config.properties说明

- dingTalk_token:预警钉钉 token
- decoding :postgres slot 编码默认使用test_decoding
- pg_url : postgres jdbc 连接串 
- pg_user :postgres 高级账号
- pg_password : postgres 密码
- slot_name : postgres 数据库 slot 名称 
- environment : 生产环境使用  prod
- lsn_file : 保存postgres  lsn 文件路径
- kafka_host : cdc 采集到postgres 日志发送到 kafka 的 broker 地址
- kafka_topic :  发送到 Kafka 的 topic 名称
- jks_path : cdc 与 kafka 部署公网连接时验证 jks 默认都是和数据库在同一个内部网段中

## 常驻后台启动

 ``` shell
   
   nohup   java -Xms8g -Xmx8g  -jar -Dfile.encoding=utf-8  postgresql-cdc.jar  -c  producer.properties -t  tables
  ```