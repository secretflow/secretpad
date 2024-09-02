# 不兼容改动点同步

## 1.项目邀约字段调整

### 1.1 变更背景
P2P模式下新增机构概念，机构代表一个组织或者部门。一个机构下可以挂载多个P2P节点，并通过一个Secretpad平台进行管理。
未来将支持同机构下多个节点模型搬运发布等功能，意在简化同机构下各个节点管理成本。

### 1.2 具体变更点
在P2P项目创建时，发起方发起邀约，参与方响应邀约
在引入机构概念之前，发起和同意邀约的主体是节点 nodeId
在引入机构概念之后，发起和同意邀约的主体是机构 instId
所以为了保证概念上的清晰，回归到项目邀约领域模型概念内，将具体的nodeId抽象为partyId，更好表达双方或者多方之间的项目邀约关系。

### 1.3 涉及版本
当前最新版本 0.10.b0 的改动，不兼容历史版本。


### 1.4 改动详情
原有类 VoteRequestDO.PartyVoteInfo
```
org.secretflow.secretpad.persistence.entity.VoteRequestDO.PartyVoteInfo

public static class PartyVoteInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 291568296509217011L;

        private String partyId; # nodeId -> partyId
        private String action;
        private String reason;

        # 以下代码省略 不做展示
    }
```
其中 nodeId 字段重命名为 partyId 表示参与方。

### 1.5 版本升级后对历史数据影响
如果历史存在邀约且有未处理邀约情况，会导致消息列表无法展示，新增的邀约无法处理并报错。

### 1.6 如何处理兼容数据
对于历史存在的voteRequest邀约数据，可以选择 update (建议) 或者 delete。

```
-- 筛选字段包含 nodeId的邀约
SELECT id FROM `vote_request` WHERE `party_vote_info` like '%nodeId%';

-- 替换并更新字段值为 partyId id=XX 就是上面筛选的Id 也可以合并成一个语句
update vote_request set party_vote_info=REPLACE(party_vote_info,'nodeId','partyId') where id=XX;

-- 合并后语句
update vote_request set party_vote_info=REPLACE(party_vote_info,'nodeId','partyId') where `party_vote_info` like '%nodeId%';
```

### 1.7 友情提示
以上代码本地sqlite测试验证，对于其他类型数据库，可以先进行语法测试。
对于线上已有应用场景，建议先备份数据，或者单条执行验证，然后再批量处理。

## 2.组件空值约定

### 2.1 变更背景
本期支持上传数据表同时指定自定义的空值。

### 2.2 具体变更点
数据表注册功能，行级过滤组件

### 2.3 涉及版本
当前最新版本 0.10.b0 新增功能
- 空值处理逻辑将自定义的空值转化为字符串 "NULL" 输出到结果表
- 系统默认字符串 "NULL" 作为空值进行后续判断，如果原始数据包含字符串 "NULL" 会被当做空值处理
- 此处严格匹配大写字符 "NULL"
