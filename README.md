## postgres数据库实时日志采集

## 	原理
基于postgresql 逻辑复制功能 通过将wal日志解析成sql 发送至kafka ，最终在consumer端进行wal 日志的sql 转换 实现pg实例到adb 实例的数据实时同步 

## 架构
 描述：利用pg  replicationStream 轮询采集某个slot （逻辑复制槽）中的wal 日志  通过 Lexer 解析工具将DML语言实时发送至kafka
使用自定义分区将相同id 记录的数据写入同一个分区中保证了数据的先后顺序。保证最大吞吐的同时也保证了数据的一致性，最终达到数据的实时性

## 配置文件config.properties说明

- dingTalk_token:预警钉钉 token
- decoding :postgres slot 编码默认使用test_decoding
- pg_url : postgres jdbc 连接串 
- pg_user :postgres 高级账号
- pg_password : postgres 密码
- target_url : 目标数据库jdbc 连接串
- target_user :目标数据库只读用户
- target_password :目标数据库密码
- slot_name : postgres 数据库 slot 名称 
- environment : 生产环境使用  prod
- lsn_file : 保存postgres  lsn 文件路径
- kafka_host : cdc 采集到postgres 日志发送到 kafka 的 broker 地址
- kafka_topic :  发送到 Kafka 的 topic 名称
- jks_path : cdc 与 kafka 部署公网连接时验证 jks

## 启动类  Bootstrap