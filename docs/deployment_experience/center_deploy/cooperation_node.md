# 添加合作节点

<font color=#DF2A3F> 针对新安装的节点，若需要作为参与节点创建项目，则需提前将节点间的通讯建立起来。</font>

## 枢纽模式TEE节点添加合作

若需要使用枢纽模式，则需要提前将自己的节点和TEE的节点完成添加合作：登录edge平台——点击添加合作节点——选择tee计算节点——输入本方节点通讯地址——点击确定。

![Cooperation1](../../imgs/cooperation1.png)

## 添加合作步骤详情

点击添加合作节点——选择已注册的可用节点——输入合作节点的通讯地址——输入本节点对合作节点暴露的通讯地址——点击确定即可建立合作关系。

![Cooperation2](../../imgs/cooperation2.png)
![Cooperation3](../../imgs/cooperation3.png)

若需要查询节点端口号，则输入如下命令：

```shell
# 查询本节点端口号的命令
docker ps
```