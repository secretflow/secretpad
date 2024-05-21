# 部署要求


## 前言
Secretpad 对操作系统、Docker 版本、处理器型号等有一些要求，部署 Secretpad 请确保您的环境符合以下要求。

## Docker 版本要求

我们推荐使用 Docker 20.10 或更高版本。Docker 的安装请参考[官方文档](https://docs.docker.com/engine/install/)，Docker 部署包下载参考[Docker软件包](https://download.docker.com/linux/centos/7/x86_64/stable/Packages/)。

## 处理器型号要求

secretpad 支持 linux/amd64 架构和 linux/arm64 架构。

## 操作系统要求
支持的操作系统包括：
* MacOS
* CentOS 7
* CentOS 8
* Ubuntu 16.04 及以上版本
* Windows (通过[WSL2 上的 Ubuntu](https://docs.microsoft.com/en-us/windows/wsl/install-win10))

## 资源要求

目前 SecretPad 不支持单独部署，在部署 SecretPad 时会依赖 Kuscia 镜像。

| 组件                  | CPU | 内存  | 磁盘空间 |
|:--------------------|:----|:----|:-----|
| SecretPad（包含kuscia） | 8核  | 16G | 200G |

## 网络要求
如果节点之间的入口网络存在网关时，为了确保节点之间通信正常，需要网关符合一些要求，详情请参考[网络要求](https://www.secretflow.org.cn/zh-CN/docs/kuscia/v0.7.0b0/deployment/networkrequirements)