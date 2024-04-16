# 快速完成一个建模任务

创建合作节点授权、且双方准备好合作数据后，可进行合作项目的发起，并在项目内通过训练流的创建和任务执行完成一个建模任务。

## Step1：发起项目

点击“项目管理—新建项目”，输入项目名称、描述，计算功能与模式默认为“模型训练-联合建模”“管道模式”

![Task1](../../imgs/modeling_task1.png)
![Task2](../../imgs/modeling_task2.png)

点击“添加节点”，在已建立合作的节点列表中选择一个，点击“创建”按钮创建项目（也可进行选择多个节点）

![Task3](../../imgs/modeling_task3.png)

创建成功后，需对方节点同意后才可进行操作

![Task4](../../imgs/modeling_task4.png)

## Step2：受邀参与项目

受邀方登录平台后，需进行“同意”邀约的操作。

方式一：点击“项目管理—我受邀”，可看到所有的项目邀约，选择“同意”或“拒绝”

![Task2.1](../../imgs/modeling_task2.1.png)

方式二：点击“消息中心—我处理的”，可看到所有的项目邀约，选择“同意”或“拒绝”

![Task2.2](../../imgs/modeling_task2.2.png)

## Step3：进入项目

光标悬停在对应项目上—点击“进入项目”，第一次进入项目会弹出“新手引导”，可跟随“新手引导”学习页面菜单功能

![Task3.1](../../imgs/modeling_task3.1.png)
![Task3.2](../../imgs/modeling_task3.2.png)

## Step4：选择训练流

### 方式一：使用训练流模板

点击“训练流”—点击“创建训练流”—输入训练流名称—选择已有训练流模板—点击“创建”，目前内置了“金融风控”、“联合圈人”两种训练流模板，可根据需求自行选择

![Task4.1](../../imgs/modeling_task4.1.png)
![Task4.2](../../imgs/modeling_task4.2.png)

### 方式二：自定义训练流

点击“训练流”—点击“创建训练流”—输入训练流名称—选择“自定义训练流”—点击“创建”

![Task4.3](../../imgs/modeling_task4.3.png)

## Step5：添加数据并授权

点击“数据管理”—“添加数据”—点击上传或拖拽本地数据文件至右侧区域

![Task5.1](../../imgs/modeling_task5.1.png)
![Task5.2](../../imgs/modeling_task5.2.png)

设置数据表名称—输入数据表描述及特征描述—点击“提交”。

<font color=#DF2A3F> 重要：特征名称需和实际文件的schame名称保持一致。</font>

![Task5.3](../../imgs/modeling_task5.3.png)

点击“授权管理”—“添加授权”—选择“授权项目”、“关联键”（可关联两张表的唯一值）、“标签列”（非必填）—点击“保存”按钮

<font color=#DF2A3F> PS：双方节点都需上传训练数据并进行授权 </font>

![Task5.4](../../imgs/modeling_task5.4.png)
![Task5.5](../../imgs/modeling_task5.5.png)

## Step6：添加组件

根据自身需求，从左侧组件列表拖拽相关组件至右侧画布区域，连接相关组件

<font color=#DF2A3F> PS：搭建时请注意连接方式是否正确 </font>

![Task6.1](../../imgs/modeling_task6.1.png)
![Task6.2](../../imgs/modeling_task6.2.png)

## Step7：配置组件

点击组件“样本表”—选择数据表—保存配置

<font color=#DF2A3F> PS：灰色组件需要进行配置，蓝色不需要 </font>

![Task7.1](../../imgs/modeling_task7.1.png)

点击组件“隐私求交”—分别选择接收方和发送方的输入—保存配置

![Task7.2](../../imgs/modeling_task7.2.png)

## Step8：执行计算

流程搭建完成后—点击“全部执行”按钮—执行计算

<font color=#DF2A3F> PS：执行完成组件为绿色，执行中为蓝色 </font>

![Task8.1](../../imgs/modeling_task8.1.png)

## Step9：结果输出

光标悬停到对应组件—点击“执行结果”—下载执行结果

![Task9.1](../../imgs/modeling_task9.1.png)

也可点击“结果管理”—下载对应项目执行结果

![Task9.2](../../imgs/modeling_task9.2.png)