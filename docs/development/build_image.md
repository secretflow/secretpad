# 打包 SecretPad 镜像


## 环境准备
[环境准备](build_SecretPad_cn.md#开发环境搭建)
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


## SecretPad (Java) 镜像构建

在 SecretPad 项目根目录下：

执行`make image`命令，该命令将会使用 Docker 命令构建出 SecretPad 镜像。目前 SecretPad 支持构建 linux/amd64 和 linux/arm64 的 Anolis 镜像。

```shell
make image
```
