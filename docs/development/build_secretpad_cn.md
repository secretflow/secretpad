# 构建命令

## 开发环境搭建

| 名称      | 推荐版本                                       | 下载地址                                                                | 说明 |
|---------|--------------------------------------------|---------------------------------------------------------------------|----|
| JDK     | OpenJDK17U-jdk_x64_linux_hotspot_17.0.11_9 | [下载JDK](https://www.oracle.com/java/technologies/downloads/#java17) |    |
| Maven   | apache-maven-3.8.8                         | [下载Maven](https://maven.apache.org/download.cgi)                    |    |
Docker|   要求最低版本 20.10      |                          [下载Docker Desktop](https://docs.docker.com/desktop/)                   |                                                                     |    |
| SqlLite | 3.43.2                                     | [下载sqlite studio](https://sqlitestudio.pl/)                         |    |

## 构建 SecretPad

SecrtPad 提供了 Makefile 来构建项目，你可以通过`make help`命令查看命令帮助，其中 Development 部分提供了构建能力：

```shell
Usage:
  make <target>

General
  help              Display this help.

Development
  test              Run tests.
  build             Build scretpad binary.
  image             Build docker image with the manager.
  docs              Build docs.
  pack              Build pack all in one with tar.gz.
```

### 测试

在 SecretPad 项目根目录下：

执行`make test`命令，该命令将会执行项目中所有的测试

### 构建可执行 JAR 文件

在 SecretPad 项目根目录下：

执行`make build`命令，该命令将会构建出 SecretPad 的可执行JAR，构建产物会生成在 ./target/ 目录下。

### 构建 SecretPad Image

在 SecretPad 项目根目录下：

执行`make image`命令，该命令将会使用 Docker 命令构建出 SecretPad 镜像。目前 SecretPad 支持构建 linux/amd64 和 linux/arm64 的 Anolis 镜像。

### 编译文档

在 SecretPad 项目根目录下：

执行`make docs`命令，该命令会生成 SecretPad 文档，生成的文档会放在 `docs/_build/html` 目录，用浏览器打开 `docs/_build/html/index.html` 就可以查看文档。

该命令依赖于 python 环境，并且已经安装了 pip 工具；编译文档前请提前安装，否则会执行错误。

### 构建 allinone-package

在 SecretPad 项目根目录下：

执行`make pack platform="linux/amd64"`命令

>platform参数是可选的，参数值有 "linux/amd64"、"linux/arm64"，如果没有指定platform，默认使用 platform="linux/amd64"。


该命令会生成 secretflow-allinone-package-{VERSION_TAG}-{MVP_TAR_SUFFIX}.tar.gz
包含 Kuscia 镜像、SecretFlow 镜像、SecretPad 镜像、一键安装脚本、一键卸载脚本。

该命令执行结果依赖 环境变量配置，不配置默认使用最新的镜像

```shell
KUSCIA_IMAGE=""
SecretPad_IMAGE=""
SECRETFLOW_IMAGE=""
SECRETFLOW_SERVING_IMAGE=""
TEE_APP_IMAGE=""
TEE_DM_IMAGE=""
CAPSULE_MANAGER_SIM_IMAGE=""

# 默认
KUSCIA_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/kuscia:latest
SecretPad_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/SecretPad:latest
SECRETFLOW_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/secretflow-lite-anolis8:latest
SECRETFLOW_SERVING_IMAGE:=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/serving-anolis8:latest
TEE_APP_IMAGE:=secretflow/teeapps-sim-ubuntu20.04:latest
TEE_DM_IMAGE:=secretflow/sf-tee-dm-sim:latest
CAPSULE_MANAGER_SIM_IMAGE:=secretflow/capsule-manager-sim-ubuntu20.04:latest
```
