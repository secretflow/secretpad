![img.png](./docs/imgs/logo.png)

# SecretPad

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
<p align="center">
<a href="./README.zh-CN.md">简体中文</a>｜<a href="./README.md">English</a>
</p>

[SecretPad](https://www.secretflow.org.cn/zh-CN/docs/secretpad/)
是一个基于 [Kuscia](https://www.secretflow.org.cn/zh-CN/docs/kuscia) 的隐私计算的 web 框架，能够方便的使用基于保护隐私的数据智能和机器学习的能力。通过
SecretPad：

* 你可以快速创建节点、注册数据、创建项目、进行合作节点间的授权
* 你可以使用丰富的数据预处理，数据分析、数据建模能力，满足多样化的业务场景。
* 你可以使用模型训练、模型预测的能力。

## Secretpad-frontend

* [secretpad-frontend](https://github.com/secretflow/secretpad-frontend)
  是Secretpad的前端项目，你可以使用 ```make build``` 命令将前端代码部署在Secretpad项目中

```text
secretpad
├──secretpad-web
│  ├─src/main/resource
```

## 组网模式

支持两种组网模式：中心化组网模式和点对点组网模式。

#### 中心化组网模式

中心化组网模式下，多个节点共享控制平面，控制平面负责管理多个节点的资源和任务调度。这种模式下节点占用资源较少，称为 Lite 节点。
中心化组网模式适合于大型机构内部的节点互联，通过统一的控制平面，可显著降低运维和资源成本，且便于快速新增节点。
![img.png](./docs/imgs/master.png)

#### 点对点组网模式

在点对点（P2P: Peer-to-Peer）组网模式下，节点拥有独立的控制平面，节点实例和控制平面在同一个子网中，这种类型的节点被称为
Autonomy 节点。 在该模式下，参与方通过 InterConn Controller， 从调度方同步 Pod 到本集群，由本方 Scheduler 绑定到节点实例。
点对点组网模式适合小型机构或是安全性要求高的场景。
![img_1.png](./docs/imgs/p2p.png)

## 快速开始

### 从提供的启动包开始

#### Step 1: 下载 mvp all in one package 部署包

你可以在这里下载最新版本的MVP部署包 [latest mvp allInOnePackage](https://www.secretflow.org.cn/zh-CN/deployment).

例如 `secretflow-allinone-package-latest.tar.gz`:

```sh
tar -xzf secretflow-allinone-package-latest.tar.gz
cd secretflow-allinone-package-latest
```

#### Step 2: Start Server

在 **Linux/Unix/Windows/Mac(inter)** 系统上, 使用如下命令启动(需要docker环境) :

```sh
# 启动master节点，内置了alice、bob2个内置lite节点
sh install.sh master
```

```sh
# 部署一个新的lite节点
sh install.sh lite -n domainId -t token -p 30002 -m master endpoint -s 8089 -k 40812 -g 40813 -q 23801
```

```sh
# 部署一个P2P节点
sh install.sh p2p -n domainId  -s 8099 -g 8092 -k 8091 -p 8090 -q 33801
```

```sh
# 注册一个P2P节点到secretpad平台
sh install.sh autonomy-node -n domainId -g 8096 -k 8087 -p 8088 -P notls -q 13805 -m 'http://secretpad:port' -t "token for node register on pad"  -x 13086
```

详细内容, 请查看 [Development](./docs/deployment_experience/v0.6.0b0/deploy_secretpad.md)

## 组件版本

> 多数情况下secretpad版本是落后于其他组件版本的<br>
> 你可以用下面的版本号当做 docker image tag 来拉取镜像<br>
> 从这里可以查看历史的镜像列表 [hub.docker](https://hub.docker.com/r/secretflow/secretflow-lite-anolis8/tags)<br>
> 或者使用阿里云容器镜像服务如下： <br>
> > secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/secretpad<br>
> > secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/kuscia<br>
> > secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/secretflow-lite-anolis8<br>
> > secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/serving-anolis8<br>

| secretpad Version | Kuscia Version | secretflow Version | trustedflow Version | serving Version | dataproxy Version | scql Version |
|-------------------|----------------|--------------------|---------------------|-----------------|-------------------|--------------|
| 0.12.0b0          | 0.13.0b0       | 1.11.0b1           | 0.1.1b0             | 0.8.0b0         | 0.3.0b0           | 0.9.2b1      |
| 0.11.0b0          | 0.12.0b0       | 1.10.0b0/1.10.0b1  | 0.1.1b0             | 0.7.0b0         | 0.2.0b0           |              |
| 0.10.0b0/0.10.1b0 | 0.11.0b0       | 1.9.0b0/1.9.0b2    | 0.1.1b0             | 0.6.0b0         | 0.1.0b1           |              |
| 0.9.0b0           | 0.10.0b0       | 1.8.0b0            | 0.1.1b0             | 0.5.0b0         |                   |              |
| 0.8.0b0/0.8.1b0   | 0.9.0b0        | 1.7.0b0            | 0.1.1b0             | 0.4.0b0         |                   |              |
| 0.7.1b0/0.7.2b0   | 0.8.0b0        | 1.6.1b0            | 0.1.1b0             | 0.3.1b0         |                   |              |
| 0.6.0b0           | 0.7.0b0        | 1.5.0b0            | 0.1.1b0             | 0.2.1b0         |                   |              |
| 0.5.0b0           | 0.6.0b0        | 1.4.0b0            | 0.1.1b0             | 0.2.0b0         |                   |              |
| 0.4.1b0           | 0.5.0b0        | 1.4.0.dev24011601  | 0.1.1b0             |                 |                   |              |
| 0.3.0b0           | 0.4.0b0        | 1.3.0.dev20231109  | 0.1.1b0             |                 |                   |              |

## Contact

* [B站](https://space.bilibili.com/2073575923): secretflow B站账号
* 技术支持微信号: secretflow02
* 微信公众号：隐语的小剧场
* 微信隐私计算行业交流群：SecretFlow01

## 声明

非正式发布的 SecretPad 版本仅用于演示，请勿在生产环境中使用。尽管此版本已涵盖 SecretPad
的基础功能，但由于项目存在功能不足和待完善项，可能存在部分安全问题和功能缺陷。因此，我们欢迎你积极提出建议，并期待正式版本的发布。