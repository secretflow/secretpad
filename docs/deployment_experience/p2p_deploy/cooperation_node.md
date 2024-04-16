# 添加合作节点

节点之间通过<添加合作节点>建立授权关系，此步骤是后续创建合作项目进行联合计算的前提。需要双方分别添加对方节点才可建立双向授权。

## step1:查看/编辑本方节点信息

默认通讯地址为本机地址，需用户自行修改为对外通讯地址，修改后系统会更新节点认证码；

<font color=#DF2A3F>PS：由于本机会有多网卡，以及不同局域网和广域网的地址出口 IP 不同，需要用户自行查看判断 IP 地址，

查看方法：以 Linux 为例，命令行输入命令 ifconfig；端口号为部署 SecretPad 时用户自行确定的 Kuscia p2p 节点的gateway
端口。</font>

```shell
ifconfig
```

通过点击“我的节点”获取节点认证码、节点名称、节点ID、公钥。

![Cooperation1](../../imgs/cooperation_p2p1.png)

推荐用“节点认证码”用于给其他方进行<添加合作节点>操作。

![Cooperation2](../../imgs/cooperation_p2p2.png)

## Step2:添加合作节点

Step1：点击<添加合作节点>

Step2：输入要添加的合作节点的节点认证码（输入后点击<识别解析>
默认填充合作节点名称、ID、通讯地址、公钥），点击确定即可建立合作关系；也可以手动输入合作节点的名称、节点ID、通讯地址和公钥.

<font color=#DF2A3F>重要：合作双方需分别添加对应合作节点</font>

![Cooperation3](../../imgs/cooperation_p2p3.png)
![Cooperation4](../../imgs/cooperation_p2p4.png)