/*
 * Copyright 2023 Ant Group Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.secretflow.secretpad.service.impl;

import org.secretflow.secretpad.common.constant.InstConstants;
import org.secretflow.secretpad.common.enums.NodeInstTokenStateEnum;
import org.secretflow.secretpad.common.errorcode.InstErrorCode;
import org.secretflow.secretpad.common.errorcode.NodeErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.CertUtils;
import org.secretflow.secretpad.common.util.FileUtils;
import org.secretflow.secretpad.common.util.TokenUtil;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.kuscia.v1alpha1.DynamicKusciaChannelProvider;
import org.secretflow.secretpad.kuscia.v1alpha1.constant.KusciaModeEnum;
import org.secretflow.secretpad.kuscia.v1alpha1.constant.KusciaProtocolEnum;
import org.secretflow.secretpad.kuscia.v1alpha1.model.KusciaGrpcConfig;
import org.secretflow.secretpad.manager.integration.model.CreateNodeParam;
import org.secretflow.secretpad.manager.integration.model.NodeDTO;
import org.secretflow.secretpad.manager.integration.node.NodeManager;
import org.secretflow.secretpad.persistence.entity.InstDO;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.repository.InstRepository;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.service.InstService;
import org.secretflow.secretpad.service.model.inst.InstRegisterRequest;
import org.secretflow.secretpad.service.model.inst.InstRequest;
import org.secretflow.secretpad.service.model.inst.InstTokenVO;
import org.secretflow.secretpad.service.model.inst.InstVO;
import org.secretflow.secretpad.service.model.node.CreateNodeRequest;
import org.secretflow.secretpad.service.model.node.NodeIdRequest;
import org.secretflow.secretpad.service.model.node.NodeTokenRequest;
import org.secretflow.secretpad.service.model.node.NodeVO;
import org.secretflow.secretpad.service.util.HttpUtils;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.secretflow.secretpad.service.util.RateLimitUtil.verifyRate;

/**
 * Institution service implementation class
 *
 * @author yansi
 * @date 2023/5/4
 */
@Service
public class InstServiceImpl implements InstService {

    private final static Logger LOGGER = LoggerFactory.getLogger(InstServiceImpl.class);

    public static String INST_ID = "";
    private final Object createLock = new Object();
    @Resource
    private InstRepository instRepository;
    @Resource
    private NodeRepository nodeRepository;
    @Resource
    private NodeManager nodeManager;
    @Resource
    private DynamicKusciaChannelProvider dynamicKusciaChannelProvider;
    @Value("${secretpad.certs.dir-path:/app/config/}")
    private String storeDir;

    @Override
    public InstVO getInst(InstRequest request) {
        String loginOwnerId = UserContext.getUser().getOwnerId();
        if (!StringUtils.equalsIgnoreCase(loginOwnerId, request.getInstId())) {
            LOGGER.warn("instId != loginOwnerId, loginOwnerId={}, instId={}", loginOwnerId, request.getInstId());
            throw SecretpadException.of(InstErrorCode.INST_MISMATCH_LOGIN, "owner not match");
        }
        InstDO instDO = instRepository.findByInstId(request.getInstId());
        if (instDO == null) {
            LOGGER.warn("instId not found ,loginOwnerId={}, instId={}", loginOwnerId, request.getInstId());
            throw SecretpadException.of(InstErrorCode.INST_NOT_EXISTS);
        }
        return InstVO.builder().instId(instDO.getInstId()).instName(instDO.getName()).build();
    }


    @Override
    public List<NodeVO> listNode() {
        final String instId = UserContext.getUser().getOwnerId();
        boolean exist = instRepository.existsById(instId);
        if (!exist) {
            LOGGER.error("list inst error, inst not exist instId = {}", instId);
            throw SecretpadException.of(InstErrorCode.INST_NOT_EXISTS);
        }
        List<NodeDTO> nodeDTOS = nodeManager.listNode(instId);
        return nodeDTOS.stream().map(NodeVO::from).collect(Collectors.toList());
    }

    @Override
    public Set<String> listNodeIds() {
        return listNode().stream().map(NodeVO::getNodeId).collect(Collectors.toSet());
    }


    @Override
    public InstTokenVO createNode(CreateNodeRequest request) {
        final String instId = UserContext.getUser().getOwnerId();
        NodeDTO node;
        synchronized (createLock) {
            Integer countNum = nodeRepository.countByInstId(instId);
            if (countNum >= InstConstants.MAX_NODE_NUM) {
                throw SecretpadException.of(InstErrorCode.INST_NODE_COUNT_LIMITED);
            }

            CreateNodeParam param = CreateNodeParam.builder()
                    .instId(instId)
                    .name(request.getName())
                    .mode(request.getMode())
                    .build();
            node = nodeManager.createP2PNodeForInst(param);
            if (node == null) {
                throw SecretpadException.of(NodeErrorCode.NODE_CREATE_ERROR);
            }
        }

        return InstTokenVO.builder().
                nodeId(node.getNodeId()).
                nodeName(node.getNodeName()).
                instToken(node.getInstToken()).
                build();
    }

    @Override
    public InstTokenVO getToken(NodeTokenRequest request) {

        String instId = UserContext.getUser().getOwnerId();
        NodeDO nodeDO = nodeRepository.findOneByInstId(instId, request.getNodeId());
        if (ObjectUtils.isEmpty(nodeDO)) {
            LOGGER.error("node not find by nodeId={}", request.getNodeId());
            throw SecretpadException.of(NodeErrorCode.NODE_NOT_EXIST_ERROR);
        }
        String instToken = nodeDO.getInstToken();
        try {
            if (StringUtils.isNotBlank(instToken)) {
                instToken = FileUtils.readFile2String(nodeDO.getInstToken());
            }
        } catch (Exception e) {
            LOGGER.error("read instToken error, nodeId={}", request.getNodeId(), e);
            throw SecretpadException.of(InstErrorCode.INST_TOKEN_MISMATCH, e, "token is missing");
        }

        return InstTokenVO.builder().
                nodeId(nodeDO.getNodeId()).
                nodeName(nodeDO.getName()).
                instToken(instToken).
                instTokenState(nodeDO.getInstTokenState()).
                build();
    }

    @Override
    public InstTokenVO newToken(NodeTokenRequest request) {
        verifyRate();
        String instId = UserContext.getUser().getOwnerId();
        String nodeId = request.getNodeId();
        String newToken = nodeManager.generateInstToken(instId, nodeId);
        try {
            newToken = FileUtils.readFile2String(newToken);
        } catch (IOException e) {
            LOGGER.error("read instToken error, nodeId={}", request.getNodeId(), e);
            throw SecretpadException.of(InstErrorCode.INST_TOKEN_MISMATCH, e, "token is missing");
        }
        return InstTokenVO.builder().
                nodeId(nodeId).
                instToken(newToken).
                build();
    }

    @Override
    public void deleteNode(NodeIdRequest request) {
        String instId = UserContext.getUser().getOwnerId();
        nodeManager.deleteNode(instId, request.getNodeId());
    }


    /**
     * if same file name then delete
     */
    private String writeFileToLocal(MultipartFile binFile, String baseDirPath, String targetFileName) {
        try {
            LOGGER.info("write file to local, file name={}", binFile.getOriginalFilename());
            File dir = new File(baseDirPath);
            if (!dir.exists()) {
                boolean mk = dir.mkdirs() || dir.mkdirs();
                LOGGER.info("mkdir ,baseDirPath={} ,result ={}", baseDirPath, mk);
            }

            String outputFileName = baseDirPath + targetFileName;
            File outputFile = new File(outputFileName);

            //SafeFileUtils.checkPathInWhitelist(target, List.of(baseDirPath));
            if (outputFile.exists()) {
                boolean deleted = outputFile.delete() ? (!outputFile.exists()) : outputFile.delete();
                LOGGER.info("delete old cert  file={},result ={}", outputFileName, deleted);
                boolean newFile = outputFile.createNewFile() || outputFile.createNewFile();
                LOGGER.info("newFile ,outputFileName={},result ={}", outputFileName, newFile);
            }

            binFile.transferTo(outputFile);
            return outputFileName;
        } catch (IOException e) {
            LOGGER.error("IOException: {}", e.getMessage());
            throw SecretpadException.of(InstErrorCode.INST_FILE_OPERATION_FAILED, e);
        }

    }


    /**
     * inst cert store
     */
    private String getStoreDir(String instId, String nodeId) {
        return storeDir + instId + FileUtils.FILE_SEPARATOR + nodeId + FileUtils.FILE_SEPARATOR;
    }


    @Override
    public void registerNode(InstRegisterRequest request) {
        String nodeId = request.getNodeId();
        String instToken = request.getInstToken();

        NodeDO node = nodeRepository.findByNodeId(nodeId);
        if (node == null) {
            LOGGER.error("node not exist , queryNodeId={}", nodeId);
            throw SecretpadException.of(NodeErrorCode.NODE_NOT_EXIST_ERROR, "node is invalid");
        }
        String instId = node.getInstId();
        InstDO instDO = instRepository.findByInstId(instId);
        if (instDO == null) {
            LOGGER.error("node not exist , queryNodeId={}", nodeId);
            throw SecretpadException.of(InstErrorCode.INST_NOT_EXISTS, "inst is invalid");
        }
        if (NodeInstTokenStateEnum.USED.equals(node.getInstTokenState())) {
            LOGGER.error("node inst token is used , nodeId={}", nodeId);
            throw SecretpadException.of(InstErrorCode.INST_TOKEN_USED, "instToken is used");
        }
        /* match db token **/
        String instTokenCheck = node.getInstToken();
        try {
            instTokenCheck = FileUtils.readFile2String(instTokenCheck);
        } catch (IOException e) {
            LOGGER.warn("IOException: readFile2String token", e);
        }
        if (!StringUtils.equals(instToken, instTokenCheck)) {
            LOGGER.error("token mismatch value in db , inputToken={} dbToken={}", instToken, node.getInstToken());
            throw SecretpadException.of(InstErrorCode.INST_TOKEN_MISMATCH, "token is expired");
        }

        /* token check **/
        boolean verify = TokenUtil.verify(instToken, instId, nodeId);
        if (!verify) {
            LOGGER.error("verify inst token failed, instToken={}", instToken);
            throw SecretpadException.of(InstErrorCode.INST_TOKEN_MISMATCH, "instToken is invalid");
        }
        /* check ?**/
        Boolean secureUrl = HttpUtils.isSecureUrl(request.getHost());
        if (!secureUrl) {
            LOGGER.warn("host url is insecure , host={}", request.getHost());
        }

        String baseDirPath = getStoreDir(instId, nodeId);
        String certFilePath = writeFileToLocal(request.getCertFile(), baseDirPath, "client.crt");
        String keyFilePath = writeFileToLocal(request.getKeyFile(), baseDirPath, "client.pem");
        String tokenFilePath = writeFileToLocal(request.getTokenFile(), baseDirPath, "token");

        try {
            if (StringUtils.isNotBlank(FileUtils.readFile2String(certFilePath))) {
                CertUtils.loadX509Cert(certFilePath);
            }
            if (StringUtils.isNotBlank(FileUtils.readFile2String(keyFilePath))) {
                CertUtils.loadPrivateKey(keyFilePath);
            }
        } catch (Exception e) {
            LOGGER.error("load x509 cert failed, certFilePath={} keyFilePath={}", certFilePath, keyFilePath, e);
            throw SecretpadException.of(InstErrorCode.INST_REGISTER_CHECK_FAILED, e, "cert is invalid");
        }


        /* all check pass*/
        KusciaGrpcConfig kConfig = KusciaGrpcConfig.builder().domainId(nodeId).
                host(request.getHost()).port(request.getPort())
                .mode(KusciaModeEnum.getByName(request.getMode()))
                .protocol(KusciaProtocolEnum.getByName(request.getProtocol()))
                .certFile(certFilePath)
                .keyFile(keyFilePath)
                .token(tokenFilePath)
                .build();

        dynamicKusciaChannelProvider.registerKuscia(kConfig);

        if (!dynamicKusciaChannelProvider.isChannelExist(node.getNodeId())) {
            LOGGER.error("kuscia channel add  failed, nodeId={}", node.getNodeId());
            throw SecretpadException.of(InstErrorCode.INST_REGISTER_CHECK_FAILED, "channel not exist");
        }

        /* update node address **/
        String address = kConfig.getHost() + ":" + request.getTransPort();
        node.setNetAddress(address);
        node.setProtocol(kConfig.getProtocol().name());
        node.setInstTokenState(NodeInstTokenStateEnum.USED);
        nodeRepository.saveAndFlush(node);

    }


    @Override
    public boolean checkNodeInInst(String instId, String nodeId) {
        return StringUtils.equals(nodeRepository.findByNodeId(nodeId).getInstId(), instId);
    }

    @Override
    public boolean checkNodesInInst(String instId, List<String> nodeIds) {
        if (CollectionUtils.isEmpty(nodeIds)) {
            return false;
        }
        List<NodeDO> nodeDOList = nodeRepository.findByInstId(instId);
        List<String> allNodes = nodeDOList.stream().map(NodeDO::getNodeId).toList();
        for (String nodeId : nodeIds) {
            if (!allNodes.contains(nodeId)) {
                return false;
            }
        }
        return true;
    }
}
