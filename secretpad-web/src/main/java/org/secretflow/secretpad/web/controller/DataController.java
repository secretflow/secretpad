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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.annotation.resource.ApiResource;
import org.secretflow.secretpad.common.annotation.resource.DataResource;
import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.enums.DataResourceTypeEnum;
import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.service.DataService;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.data.CreateDataRequest;
import org.secretflow.secretpad.service.model.data.DownloadDataRequest;
import org.secretflow.secretpad.service.model.data.DownloadInfo;
import org.secretflow.secretpad.service.model.data.UploadDataResultVO;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Data controller
 *
 * @author xiaonan
 * @date 2023/05/25
 */
@RestController
@RequestMapping(value = "/api/v1alpha1/data")
public class DataController {

    private final static Logger LOGGER = LoggerFactory.getLogger(DataController.class);

    public final DataService dataService;

    @Autowired
    public DataController(DataService dataService) {
        this.dataService = dataService;
    }

    /**
     * Upload data api
     *
     * @param nodeId target nodeId
     * @param file   multipart file
     * @return successful SecretPadResponse with upload data result view object
     */
    @ResponseBody
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResource(code = ApiResourceCodeConstants.DATA_UPLOAD)
    public SecretPadResponse<UploadDataResultVO> upload(
            @RequestParam(value = "Node-Id") String nodeId,
            @RequestParam("file") MultipartFile file
    ) {
        return SecretPadResponse.success(dataService.upload(file, nodeId));
    }

    /**
     * Create data api
     *
     * @param request create data request
     * @return successful SecretPadResponse with domain data id in apiLite
     */
    @ResponseBody
    @PostMapping(value = "/create", consumes = "application/json")
    @DataResource(field = "nodeId", resourceType = DataResourceTypeEnum.NODE_ID)
    @ApiResource(code = ApiResourceCodeConstants.DATA_CREATE)
    public SecretPadResponse<String> createData(
            @Valid @RequestBody CreateDataRequest request
    ) {
        return SecretPadResponse.success(dataService.createData(request));
    }

    /**
     * Download data api
     *
     * @param response http servlet response
     * @param request  download data request
     */
    @ResponseBody
    @PostMapping(value = "/download")
    @DataResource(field = "nodeId", resourceType = DataResourceTypeEnum.NODE_ID)
    @ApiResource(code = ApiResourceCodeConstants.DATA_DOWNLOAD)
    public void download(HttpServletResponse response, @Valid @RequestBody DownloadDataRequest request) {
        DownloadInfo downloadInfo = dataService.download(request);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + downloadInfo.getFileName());
        response.setContentLength((int) new File(downloadInfo.getFilePath()).length());
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            InputStream inputStream = new FileInputStream(downloadInfo.getFilePath());
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            inputStream.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw SecretpadException.of(SystemErrorCode.UNKNOWN_ERROR, e);
        }
    }
}
