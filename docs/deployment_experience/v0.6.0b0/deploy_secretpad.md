# Secretpad部署文档

## 前言

本教程帮助你在多台机器上部署[中心化组网模式](https://www.secretflow.org.cn/zh-CN/docs/kuscia/v0.6.0b0/reference/architecture_cn#centralized)、[点对点组网模式](https://www.secretflow.org.cn/zh-CN/docs/kuscia/v0.6.0b0/reference/architecture_cn#peer-to-peer)
来部署Secretpad集群。

## 前置准备

在部署Secretpad之前，请确保环境准备齐全，包括所有必要的软件、资源、操作系统版本和网络环境等满足要求，以确保部署过程顺畅进行，详情参考[部署要求](deploy_check.md)

## 一. 部署方式

部署有两种方式，可以通过下载**镜像压缩包**部署，也可以通过直接下载**install脚本**的方式部署。

### 1.1 使用镜像包部署

[点此下载安装包](https://secretflow-public.oss-cn-hangzhou.aliyuncs.com/mvp-packages/secretflow-allinone-package-latest.tar.gz)

解压部署包```tar -xzvf secretflow-allinone-package-latest.tar.gz```

cd到解压包中，部署的镜像在image目录下。

### 1.2 使用脚本部署

[点击下载install脚本](../../../scripts/install.sh)

在install脚本中配置所需的镜像。

```
export SECRETPAD_IMAGE=""
export KUSCIA_IMAGE=""
export SECRETFLOW_IMAGE=""
export SECRETFLOW_SERVING_IMAGE=""
export TEE_APP_IMAGE=""
export TEE_DM_IMAGE=""
export CAPSULE_MANAGER_SIM_IMAGE=""
 ```

通过上述两种方式都可以拿到install脚本，下面是通过install脚本部署说明。

## 二. 中心化模式部署

[中心化平台安装指引](https://www.secretflow.org.cn/zh-CN/docs/secretpad/latest/zgnd8oqo5chsqhzm)

**安装master节点**

```shell
# 安装master节点后 总共会生成五个容器，一个kuscia master节点，三个kuscia内置节点（alice、bob、tee），一个平台容器
bash install.sh master

# 查看docker容器
docker ps

# 一个master节点
root-kuscia-master
# 两个内置lite节点
root-kuscia-lite-alice
root-kuscia-lite-bob
# 一个内置tee节点
root-kuscia-lite-tee
# 一个平台容器
root-kuscia-master-secretpad
```

**注册lite节点**

[点击此处注册节点教程](../node_installation_guidelines.md#第-1-步节点注册-获取节点id节点令牌)

**安装lite节点**

```shell
# 安装lite节点后 总共会生成一个容器，一个kuscia lite节点
bash install.sh lite -n alice -m 'http://root-kuscia-master:1080' -t xdeploy-tokenx -p 10080  -k 41802 -g 41803 -s 8180 -q 13181 -P notls
```

## 三. 点对点模式部署

[点对点平台安装指引](https://www.secretflow.org.cn/zh-CN/docs/secretpad/latest/dbd670doqfio1puq)

```shell
# 安装autonomy节点后 总共会生成一个容器，一个kuscia autonomy节点
bash install.sh autonomy -n alice -s 8080 -g 40803 -k 40802 -p 10080 -q 13081 -P mtls
# 部署两个autonomy节点之后就可以进行通信了
```

[点对点添加合作节点教程](https://www.secretflow.org.cn/zh-CN/docs/secretpad/latest/kvu445094gvtkp3f)

## 四. 参数说明

### 配置项详解

- `n`:节点名称，平台页面的计算节点ID
- `m`:master节点地址
    - `协议`:与—P参数对应关系
        - notls->http
        - (tls、mtls)->https
    - `ip`:master节点的ip地址
    - `port`:master节点的gateway端口号
- `t`:节点token，平台页面中的节点部署令牌
- `d`:项目的安装目录(默认安装目录是：$HOME/kuscia)
- `p`:参数传递的是 lite/autonomy 容器 kuscia-gateway 映射到主机的端口，保证和主机上现有的端口不冲突即可
- `k`:参数传递的是 lite/autonomy 容器 Kuscia-api 映射到主机的 HTTP/HTTPS 端口，保证和主机上现有的端口不冲突即可
- `g`:参数传递的是 lite/autonomy 容器 Kuscia-grpc 映射到主机的 HTTP/HTTPS 端口，保证和主机上现有的端口不冲突即可
- `s`:secretpad平台端口，保证和主机上现有的端口不冲突即可
- `q`:参数传递的是 lite/autonomy 容器 kuccia 映射到主机的 env 端口，保证和主机上现有的端口不冲突即可
- `P`:KusciaAPI 以及节点对外网关使用的通信协议，有三种安全模式可供选择：notls/tls/mtls（非必填，只允许小写，默认：tls)，与
  Kuscia
  部署配置相同 [protocol参考链接](https://www.secretflow.org.cn/zh-CN/docs/kuscia/v0.6.0b0/deployment/kuscia_config_cn#id3)
    - `notls`: 此模式下，通信通过未加密的 HTTP 传输，比较安全的内部网络环境或者 Kuscia 已经存在外部网关的情况可以使用该模式【直接部署在公网有安全风险】。
    - `tls`: (默认)在此模式下，通信通过 TLS 协议进行加密，即使用 HTTPS 进行安全传输，不需要手动配置证书。
    - `mtls`: 这种模式也使用 HTTPS 进行通信，但它支持双向 TLS 验证，需要手动交换证书以建立安全连接。