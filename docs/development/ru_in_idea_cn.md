# secretpad本地调试

## 环境准备

### 后端 Secretpad (Java)

| 名称      | 版本    | 下载地址                                                                | 说明              |
|---------|-------|---------------------------------------------------------------------|-----------------|
| JDK     | 17    | [下载JDK](https://www.oracle.com/java/technologies/downloads/#java17) |                 |
| Maven   | 3.8.+ | [下载Maven](https://maven.apache.org/download.cgi)                    |                 |
| SqlLite | 3.+   | [下载sqlite studio](https://sqlitestudio.pl/)                         |                 |

### 前端 Secretpad-frontend (React)

> 如果开发调试过程中，不使用到前端项目，则无需关注

| 名称   | 版本     | 下载地址                               | 说明               |
|------|--------|------------------------------------|------------------|
| Node | 14     | [下载Node](https://nodejs.p2hp.com/) | 可安装nvm node版本管理器 |
| pnpm | latest | npm install -g pnpm                |                  |

## 代码准备

### 后端 Secretpad (Java)

```text
https://github.com/secretflow/secretpad 
https://gitee.com/secretflow/secretpad  
```

### 前端 Secretpad-frontend (React)

```text
https://github.com/secretflow/secretpad-frontend 
https://gitee.com/secretflow/secretpad-frontend 
```

## 代码编译

### 后端 Secretpad (Java)

> 在项目拉取完成后，打开开发工具（如：eclipse、idea）刷新maven。导入依赖。
> 项目中有自身构建依赖，需install到本地。

```shell
mvn clean install -Dmaven.test.skip=true
```

### 前端 Secretpad-frontend (React)

> 在项目拉取完成后，打开开发工具（如：webstorm、idea,vsCode）

```shell
pnpm install
```

## 本地调试

### 后端 Secretpad (Java)

> 目前secretpad本地调试需依赖kuscia，简单利用mvp操作，步骤如下

#### Mvp准备

下载地址：[下载MVP](https://secretflow-public.oss-cn-hangzhou.aliyuncs.com/mvp-packages/secretflow-allinone-package-latest.tar.gz)

#### 安装MVP

下载后得到tar压缩包为：secretflow-allinone-package-latest.tar.gz

---

```shell
 tar -zxvf secretflow-allinone-package-latest.tar.gz
 cd secretflow-allinone-package-latest
 ./install 
```

> **安装结束后出现如下内容则为安装完成 妥善保存用户名密码和安装路径：**

:::success
web server started successfully
Please visit the website [http://localhost:8088](http://localhost:8088) (or http://{the IPAddress of this machine}:8088)
to experience the Kuscia web's functions .
The login name:'admin' ,The login password:'12#$qwER' . （用户名+密码）
The demo data would be stored in the path: /Users/lucky/kuscia .（后面用到这个路径）
:::

---

#### secretpad本地启动准备-stop mvp中的进程

> 查看进程

```shell
 docker ps
 ----------------------
 CONTAINER ID   IMAGE                                                                                                            COMMAND                   CREATED              STATUS              PORTS                                                                       NAMES
c406f88100b4   secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/secretpad:v0.2.0b0-6-g4eae988-20230911104908-4eae98   "java -jar -Dsun.net…"   About a minute ago   Up About a minute   80/tcp, 0.0.0.0:8088->8080/tcp                                              lucky-kuscia-secretpad
6da080bc5afd   secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/kuscia:0.3.0b0                                        "bin/entrypoint.sh t…"   5 minutes ago        Up 5 minutes        0.0.0.0:38080->1080/tcp                                                     lucky-kuscia-lite-bob
f2e458ffc357   secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/kuscia:0.3.0b0                                        "bin/entrypoint.sh t…"   6 minutes ago        Up 6 minutes        0.0.0.0:28080->1080/tcp                                                     lucky-kuscia-lite-alice
2baf8096ed5a   secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/kuscia:0.3.0b0                                        "tini -- scripts/dep…"   7 minutes ago        Up 7 minutes        0.0.0.0:18080->1080/tcp, 0.0.0.0:18082->8082/tcp, 0.0.0.0:18083->8083/tcp   lucky-kuscia-master
```

---

> **结束secretpad进程**
> **找到secretpad名NAMES为 lucky-kuscia-secretpad （不同安装的name不同）**

```shell
docker stop lucky-kuscia-secretpad
```

#### secretpad本地启动准备-证书相关配置

> **证书相关配置 参考图文描述**

:::tips
安装的目录，即上述安装完成后的目录 ，示例是：/Users/lucky/kuscia
复制到项目目录，举例是：/secretpad
:::

---

```shell

cp -R /Users/lucky/kuscia/secretpad/config/certs  /secretpad/config/certs 
cp -R /Users/lucky/kuscia/secretpad/config/certs  /secretpad/config/certs/alice
cp -R /Users/lucky/kuscia/secretpad/config/certs  /secretpad/config/certs/bob

cp /Users/lucky/kuscia/secretpad/config/server.jks /secretpad/config/server.jks

```

> 目录举例

![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/122656402/1703493127609-2c39f495-ec7e-4a7a-9c82-b65e95094048.png#clientId=u0f93730d-0192-4&from=paste&height=343&id=u1120c4c2&originHeight=686&originWidth=410&originalType=binary&ratio=2&rotation=0&showTitle=false&size=96136&status=done&style=none&taskId=uef04c702-285a-4793-9ebc-163167278f2&title=&width=205)

#### secretpad本地启动准备-数据库配置（示例：sqlite）

:::success
项目config目录同级新建db文件夹 即：/secretpad/db
db目录下新建：secretpad.sqlite文件
:::

---

```shell
mkdir -p /secretpad/db
touch /secretpad/db/secretpad.sqlite
```

---

```shell
cd /secretpad/scripts
./update-sql.sh
```

#### secretpad本地启动准备-默认用户初始化

:::success
使用SQLiteStudio工具连接数据库 查看user_accounts表是否已有默认用户数据；如没有则进行创建，**否则启动项目会报错**
**如下sql，也可自己设置sql中密码hash：加密规则为sha256**
**默认用户：admin**
**默认密码：12#$qwER**
**如果为p2p启动需要设置用户owner_type字段值为：P2P**
:::

```shell
INSERT INTO user_accounts (owner_id,
                           owner_type,
                           gmt_modified,
                           gmt_create,
                           is_deleted,
                           password_hash,
                           name,
                           id)
VALUES ('kuscia-system',
        'CENTER',
        '2023-11-09 08:01:45',
        '2023-11-09 08:01:45',
        0,
        'b524e25cf0067afea0fba7329d76a06950ab24175269383f1611a8a9dfb0d322',
        'admin',
        1);
```

#### secretpad本地启动准备-配置启动参数

> 参数说明：spring.profiles.active为项目命名空间

```
-Dspring.profiles.active=dev 
```

#### secretpad本地启动

```text
SecretPadApplication
```

#### 测试是否成功启动

> 项目成功启动后；可打开链接测试：[http://localhost:8080/login](http://localhost:8080/login) （端口可能不同，根据启动情况）

### 前端 Secretpad-frontend (React)

#### 本地调试启动

> 前后端分离调试 本地开发测试 手动在platform目录下增加.env文件 文件内容如下

```text
PROXY_URL = http://localhost:8080
```

> 项目启动

```shell
pnpm run dev
```

> 启动后访问测试：如：http://localhost:8000（端口可能不同，根据控制台打印端口）

#### 编译后集成启动

> 前后端合并启动调试

```shell
pnpm run build
```

> 编译完成后将编译文件放入secretpad后端目录中
> 目录说明：/secretpad/secretpad-web/src/main/resources/static

> 启动后访问测试：如：http://localhost:8080（端口可能不同，此处为secretpad程序端口）

## 编译构建

### 后端 Secretpad (Java) 镜像构建

> 修改项目中镜像：scripts/build_image.sh脚本中镜像仓库地址；即:remote_image的值。

```shell
make image
```

### MVP构建

> 修改项目：scripts/pack/pack_allinone.sh脚本中的镜像拉取路径

```
KUSCIA_IMAGE="" //kuscia镜像地址
SECRETPAD_IMAGE=""  //secretpad镜像地址
SECRETFLOW_IMAGE=""  //secretflow镜像地址
```

---

```
sh scripts/pack/pack_allinone.sh
```

## Secretpad常见问题

### 项目编译中 依赖错误或import找不到（代码飘红）

```markdown
mvn clean install -Dmaven.test.skip=true
```

### 启动失败

排查控制台日志

- kuscia地址是否正确
- 是否设置了用户信息

### 分机部署（secretpad和kuscia不在同一台机器安装）

- 安装mvp包 ./install时 使用：-P notls 参数
