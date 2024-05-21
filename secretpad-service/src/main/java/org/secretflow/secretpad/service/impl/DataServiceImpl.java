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


import org.secretflow.secretpad.common.constant.KusciaDataSourceConstants;
import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.errorcode.AuthErrorCode;
import org.secretflow.secretpad.common.errorcode.DataErrorCode;
import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.CompressUtils;
import org.secretflow.secretpad.common.util.SafeFileUtils;
import org.secretflow.secretpad.common.util.TypeConvertUtils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.manager.integration.data.AbstractDataManager;
import org.secretflow.secretpad.manager.integration.model.NodeResultDTO;
import org.secretflow.secretpad.manager.integration.node.AbstractNodeManager;
import org.secretflow.secretpad.service.DataService;
import org.secretflow.secretpad.service.model.data.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Data service implementation class
 *
 * @author : xiaonan.fhn
 * @date 2023/5/25
 */
@Service
public class DataServiceImpl implements DataService {

    private final static Logger LOGGER = LoggerFactory.getLogger(DataServiceImpl.class);

    private final static List<String> SUPPORT_FILE_TYPE = List.of(".csv");

    public final static String DEFAULT_DATASOURCE = "default-data-source";
    public final static String HTTP_DATASOURCE = "http-data-source";

    private final static String DEFAULT_DATASOURCE_TYPE = "localfs";

    private final static SecureRandom RANDOM = new SecureRandom(TypeConvertUtils.long2Bytes(System.currentTimeMillis()));

    private final static String FILE_SEPETATOR = "/";

    @Autowired
    private AbstractDataManager dataManager;

    @Autowired
    private AbstractNodeManager nodeManager;

    @Value("${secretpad.data.dir-path:/app/data/}")
    private String storeDir;

    @Override
    public UploadDataResultVO upload(MultipartFile file, String nodeId) {
        checkDataPermissions(nodeId);
        String fileName = file.getOriginalFilename();
        fileNameCheck(fileName);
        nodeIdValidCheck(nodeId);
        String dirPath = storeDir + nodeId + FILE_SEPETATOR;
        String randomFileName = null;
        File target = null;
        for (int i = 0; i < 5; i++) {
            randomFileName = getRandomFileName(fileName);
            target = new File(dirPath + randomFileName);
            if (!target.exists()) {
                break;
            }
        }
        SafeFileUtils.checkPathInWhitelist(target, List.of(storeDir));
        if (target.exists()) {
            LOGGER.warn("After try some times generate random file name, the target random file {} still exists.", dirPath + randomFileName);
            throw SecretpadException.of(DataErrorCode.FILE_EXISTS_ERROR);
        }
        createDirIfNotExist(dirPath);
        try {
            file.transferTo(target);
        } catch (IOException e) {
            LOGGER.error("IOException: {}", e.getMessage());
            throw SecretpadException.of(SystemErrorCode.UNKNOWN_ERROR, e);
        }
        return UploadDataResultVO.builder()
                .name(fileName)
                .realName(randomFileName)
                .datasource(DEFAULT_DATASOURCE)
                .datasourceType(DEFAULT_DATASOURCE_TYPE)
                .build();
    }

    @Override
    public String createData(CreateDataRequest request) {
        return dataManager.createData(
                request.getNodeId(),
                request.getName(),
                request.getRealName(),
                request.getTableName(),
                request.getDescription(),
                request.getDatatableSchema()
        );
    }

    @Override
    public String createDataByDataSource(CreateDataByDataSourceRequest request) {
        return dataManager.createDataByDataSource(request.getNodeId(), request.getName(), request.getTablePath(),
                request.getDatasourceId(), request.getDescription(), request.getDatatableSchema());
    }

    @Override
    public DownloadInfo download(DownloadDataRequest request) {
        NodeResultDTO nodeResult = nodeManager.getNodeResult(request.getNodeId(), request.getDomainDataId());
        String relativeUri = nodeResult.getRelativeUri();
        relativeUriValidCheck(relativeUri);
        String dirPath = storeDir + request.getNodeId();
        String dir = dirPath + FILE_SEPETATOR;
        String filePath = dir + relativeUri;
        File f = new File(filePath);
        try {
            if (!f.exists()) {
                LOGGER.warn("The result ralative uri file {} not exits.", filePath);
                // Todo: the result so far is that an empty file is returned if it does not exist
                if (!f.getParentFile().exists()) {
                    f.getParentFile().mkdirs();
                }
                if (!f.createNewFile()) {
                    LOGGER.error("failed to create empty file.");
                    throw SecretpadException.of(SystemErrorCode.UNKNOWN_ERROR, "failed to create empty file for return.");
                }
            }
            // Security Recommendation Verification The download file path will not be overridden and is a subdirectory in the storeDir directory
            File dirPathFile = new File(dirPath);
            if (!f.getCanonicalPath().startsWith(dirPathFile.getCanonicalPath())) {
                LOGGER.error("The result ralative uri file {} is not in the storeDir {}", filePath, dir);
                throw SecretpadException.of(DataErrorCode.FILE_NOT_EXISTS_ERROR);
            }
            String downloadFilePath = null;
            String fileName = null;
            if (f.isDirectory()) {
                LOGGER.info("Download process got a dir to download, whose relative uri = {}", relativeUri);
                CompressUtils.compress(filePath, dir, relativeUri);
                fileName = relativeUri + ".tar.gz";
                // since it is a new compressed file, add a suffix
                downloadFilePath = dir + fileName;

            } else {
                LOGGER.info("Download process got a  real csv file to download, whose relative uri = {}", relativeUri);
                fileName = relativeUri + ".csv";
                // since the source file is already csv, there is no need to add a suffix, but the file name returned above is suffixed
                downloadFilePath = dir + relativeUri;
            }
            LOGGER.info("When download, the ralative uri = {}. the real file path = {}", relativeUri, filePath);
            return DownloadInfo.builder()
                    .fileName(fileName)
                    .filePath(downloadFilePath)
                    .build();
        } catch (IOException e) {
            LOGGER.error("IO exception: {}", e.getMessage());
            throw SecretpadException.of(SystemErrorCode.UNKNOWN_ERROR, e);
        } catch (Exception e) {
            LOGGER.error("got Exception: {}", e.getMessage());
            throw SecretpadException.of(SystemErrorCode.UNKNOWN_ERROR, e);
        }
    }

    /**
     * Create directory if not exists
     *
     * @param dir target directory path
     */
    private void createDirIfNotExist(String dir) {
        File f = new File(dir);
        if (!f.exists()) {
            LOGGER.info("The target dir {} is not exist, try to create new dir", dir);
            if (!f.mkdirs()) {
                LOGGER.error("Failed to create new dir {}", dir);
                throw SecretpadException.of(SystemErrorCode.UNKNOWN_ERROR, "Failed to create new dir when download.");
            }
        }
    }

    /**
     * Check fileName
     *
     * @param fileName file name
     */
    private void fileNameCheck(String fileName) {
        if (fileName == null) {
            LOGGER.error("The user input fileName {} is empty!", fileName);
            throw SecretpadException.of(DataErrorCode.FILE_NAME_EMPTY);
        }
        if (fileName.contains("/") || fileName.contains("\\")) {
            LOGGER.error("The user input filName {} contains / or \\, which will cause cross dir attack!", fileName);
            throw SecretpadException.of(DataErrorCode.ILLEGAL_PARAMS_ERROR, "file name cannot contains \\ or /");
        }
        String suffixName = fileName.substring(fileName.lastIndexOf('.'));
        if (!SUPPORT_FILE_TYPE.contains(suffixName)) {
            LOGGER.error("The user input fileName {} type {} not support yet.", fileName, suffixName);
            throw SecretpadException.of(DataErrorCode.FILE_TYPE_NOT_SUPPORT, "does not support " + suffixName + " type file.");
        }
    }

    /**
     * Build random file name via random Integer
     *
     * @param fileName file name
     * @return random file name
     */
    private String getRandomFileName(String fileName) {
        String prefix = fileName.substring(0, fileName.lastIndexOf('.'));
        String suffixName = fileName.substring(fileName.lastIndexOf('.'));
        String randomFileName = prefix + "_" + RANDOM.nextInt(Integer.MAX_VALUE) + suffixName;
        LOGGER.info("generate random upload file name: {}", randomFileName);
        return randomFileName;
    }

    /**
     * Valid nodeId if contains impermissible char
     *
     * @param nodeId target nodeId
     */
    private void nodeIdValidCheck(String nodeId) {
        if (nodeId.contains("/") || nodeId.contains("\\")) {
            LOGGER.error("node id {} contains / or \\, which not allowed.", nodeId);
            throw SecretpadException.of(DataErrorCode.ILLEGAL_PARAMS_ERROR, "node ID cannot contains \\ or /");
        }
    }

    /**
     * Valid relative Uri if illegal
     *
     * @param relativeUri relative Uri
     */
    private void relativeUriValidCheck(String relativeUri) {
        if (relativeUri == null || "".equals(relativeUri)) {
            throw SecretpadException.of(DataErrorCode.ILLEGAL_PARAMS_ERROR,
                    "relative uri is empty!"
            );
        }
        if (relativeUri.contains("..")) {
            throw SecretpadException.of(
                    DataErrorCode.ILLEGAL_PARAMS_ERROR,
                    "relative uri " +
                            relativeUri +
                            " contains invalid character"
            );
        }
    }

    @Override
    public List<DataSourceVO> queryDataSources() {
        List<DataSourceVO> list = new ArrayList<>();
        list.add(DataSourceVO.builder().name(KusciaDataSourceConstants.DEFAULT_DATA_SOURCE).path(KusciaDataSourceConstants.DEFAULT_DATA_SOURCE_PATH).build());
        return list;
    }

    private void checkDataPermissions(String nodeId) {
        UserContextDTO user = UserContext.getUser();
        if (user.getPlatformType().equals(PlatformTypeEnum.EDGE) && !user.getOwnerId().equals(nodeId)) {
            throw SecretpadException.of(AuthErrorCode.AUTH_FAILED, "no Permissions");
        }
    }
}
