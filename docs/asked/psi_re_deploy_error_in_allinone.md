# 重新部署allinone后，运行psi报错

## 问题描述
重新部署 allinone 后，运行 psi 报错：decrypt data source info failed, crypto/rsa: decryption error
## 问题原因
节点重装后节点的私钥变了，导致无法解密之前的数据
## 解决方案
遇到该问题，找到kuscia.yaml文件和k3s目录并删除，尝试重新安装；1.9.0版本已经解决该问题。