# 打包MVP部署包


## 环境准备
[环境准备](build_secretpad_cn.md#开发环境搭建)
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
## MVP 构建

> 修改项目：scripts/pack/pack_allinone.sh 脚本中的镜像拉取路径

```shell
KUSCIA_IMAGE=""
SECRETPAD_IMAGE=""
SECRETFLOW_IMAGE=""
SECRETFLOW_SERVING_IMAGE=""
TEE_APP_IMAGE=""
TEE_DM_IMAGE=""
CAPSULE_MANAGER_SIM_IMAGE=""

# 默认
KUSCIA_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/kuscia:latest
SECRETPAD_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/secretpad:latest
SECRETFLOW_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/secretflow-lite-anolis8:latest
SECRETFLOW_SERVING_IMAGE:=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/serving-anolis8:latest
TEE_APP_IMAGE:=secretflow/teeapps-sim-ubuntu20.04:latest
TEE_DM_IMAGE:=secretflow/sf-tee-dm-sim:latest
CAPSULE_MANAGER_SIM_IMAGE:=secretflow/capsule-manager-sim-ubuntu20.04:latest
```


在 SecretPad 项目根目录下：

执行`make pack platform="linux/amd64"`命令

>platform参数是可选的，参数值有 "linux/amd64"、"linux/arm64"，如果没有指定platform，默认使用 platform="linux/amd64"。


该命令会生成 secretflow-allinone-package-{VERSION_TAG}-{MVP_TAR_SUFFIX}.tar.gz
包含 kuscia 镜像、SecretFlow 镜像、SecretPad 镜像、一键安装脚本、一键卸载脚本。

该命令执行结果依赖 环境变量配置，不配置默认使用最新的镜像
