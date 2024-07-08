# sqlite 迁移至 mysql 说明

## 版本约定

* 此文档约定版本为2024.3.25为止,docker hub的mysql官方latest版本

## 前置条件

* 以center模式或者p2p模式新安装的集群为例
* 安装完成后请勿做任何操作,包括登录.登录之后token表中的datetime字段和mysql不兼容,需要额外的手动修改类型

## dump sqlite数据库(以center模式举例,其他模式类同)

* 以root用户为例,进入到数据库文件所在位置 cd /root/kuscia/master/secretpad/kuscia-system/db
* sqlite3 secretpad.sqlite .dump > secretpad_dump.sql

## 转换secretpad_dump.sql格式为mysql的sql格式,需手动转换

* 将secretpad_dump.sql中的除了insert语句,其他全部删除,仅仅保留insert语句
* 删除sqlite_sequence表相关的insert语句,类似:INSERT INTO "sqlite_sequence" VALUES('inst',2);都要删除
* 替换所有的表名称,去掉insert语句中表名称上的双引号
* 如果本身已有feature_table,vote_invite,vote_request等表数据,需将sql语句中desc字段替换成description

## 创建mysql数据库

* 在准备好的mysql中创建好所有的表,schemamysql文件夹下create_table.sql为mysql的建表语句
* 建好表之后,将上述步骤准备好的sql语句插入到准备好的mysql中,完整的mysql sql语句示例见schemamysql文件夹下insert.sql

## 在已经启动的集群中找到secretpad的pod,替换配置

* 修改数据库驱动,在{user}-kuscia-master-secretpad pod中修改 application.yaml中将注释掉的springjpa以及数据库配置打开,替换掉上面的数据库配置

## 重启secretpad pod 迁移成功

* 重新启动{user}-kuscia-master-secretpad
