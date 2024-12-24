# 自定义SCQL分析组件使用说明

平台使用自定义SCQL分析组件时，请注意以下要求和配置：

## 字段命名规范

在使用SCQL自定义分析组件时，所使用的表中字段命名不得包含SQL关键字和中划线。例如，禁止使用 `default` 和 包含`-`的字段作为字段名。

## 数据按字段授权CCL配置

在进行数据按字段授权（CCL）配置时，配置仅对项目的其他参与方生效，对本方不设限制。

## 使用场景

当前自定义SCQL分析组件可以用于样本表和隐私求交之后的数据分析。

## 结果返回规则

SCQL遵循“任务谁发起，结果给谁”的规则。

### CENTER模式

- 发起方为中心化master节点。
- 接收方需要选择具体的节点。

在CENTER模式下，结果接收方可以是项目中的任何一个参与方。对于样本表和隐私求交，如果结果接收方没有提供表参与，自定义SCQL分析组件生成的结果平台将无法在平台上展示，需要到kuscia容器中查看，domainDataId为 `jobId + outputId`。

```shell
# 在kuscia查看结果
# kubectl get domaindata -n NAMESPACE NAME -oyaml
kubectl get domaindata -n bob tjuj-ewfvyzni-node-34-output-0 -oyaml
```

### P2P模式

- 结果接收方只能是画布的创建方。
- 无论结果接收方是否提供表，都可以查看自定义SCQL分析组件的结果。
