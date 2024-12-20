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
import org.secretflow.secretpad.common.errorcode.InstErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.service.InstService;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.inst.InstRegisterRequest;
import org.secretflow.secretpad.service.model.inst.InstRequest;
import org.secretflow.secretpad.service.model.inst.InstTokenVO;
import org.secretflow.secretpad.service.model.inst.InstVO;
import org.secretflow.secretpad.service.model.node.CreateNodeRequest;
import org.secretflow.secretpad.service.model.node.NodeIdRequest;
import org.secretflow.secretpad.service.model.node.NodeTokenRequest;
import org.secretflow.secretpad.service.model.node.NodeVO;
import org.secretflow.secretpad.web.util.RequestUtils;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Inst controller
 */
@RestController
@RequestMapping(value = "/api/v1alpha1/inst")
public class InstController {

    @Resource
    private InstService instService;

    @PostMapping(value = "/get", consumes = "application/json")
    @DataResource(field = "instId", resourceType = DataResourceTypeEnum.NODE_ID)
    @ApiResource(code = ApiResourceCodeConstants.INST_GET)
    public SecretPadResponse<InstVO> get(@Valid @RequestBody InstRequest request) {
        return SecretPadResponse.success(instService.getInst(request));
    }


    /**
     * list all node
     */
    @PostMapping(value = "/node/list")
    @ApiResource(code = ApiResourceCodeConstants.NODE_LIST)
    public SecretPadResponse<List<NodeVO>> listNode() {
        return SecretPadResponse.success(instService.listNode());
    }


    /**
     * create inst node
     */
    @PostMapping(value = "/node/add", consumes = "application/json")
    @ApiResource(code = ApiResourceCodeConstants.NODE_CREATE)
    public SecretPadResponse<InstTokenVO> createNode(@Valid @RequestBody CreateNodeRequest request) {
        return SecretPadResponse.success(instService.createNode(request));
    }


    /**
     * get current token
     **/
    @PostMapping(value = "/node/token", consumes = "application/json")
    @ApiResource(code = ApiResourceCodeConstants.NODE_TOKEN)
    public SecretPadResponse<InstTokenVO> token(@Valid @RequestBody NodeTokenRequest request) {
        return SecretPadResponse.success(instService.getToken(request));
    }


    @PostMapping(value = "/node/newToken", consumes = "application/json")
    @ApiResource(code = ApiResourceCodeConstants.NODE_NEW_TOKEN)
    public SecretPadResponse<InstTokenVO> newToken(@Valid @RequestBody NodeTokenRequest request) {
        return SecretPadResponse.success(instService.newToken(request));
    }


    @PostMapping(value = "/node/delete", consumes = "application/json")
    @DataResource(field = "nodeId", resourceType = DataResourceTypeEnum.NODE_ID)
    @ApiResource(code = ApiResourceCodeConstants.NODE_DELETE)
    public SecretPadResponse<Void> deleteNode(@Valid @RequestBody NodeIdRequest request) {
        instService.deleteNode(request);
        return SecretPadResponse.success();
    }


    /**
     * util.sh  post_kuscia_node
     **/
    @PostMapping(value = "/node/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResource(code = ApiResourceCodeConstants.DATA_UPLOAD)
    public SecretPadResponse<Void> registerNode(@Valid @RequestParam(value = "json_data") String jsonData,
                                                @NotNull @RequestParam("certFile") MultipartFile certFile,
                                                @NotNull @RequestParam("keyFile") MultipartFile keyFile,
                                                @NotNull @RequestParam("token") MultipartFile tokenFile) {

        InstRegisterRequest registerRequest = JsonUtils.toJavaObject(jsonData, InstRegisterRequest.class);
        registerRequest.setCertFile(certFile);
        registerRequest.setKeyFile(keyFile);
        registerRequest.setTokenFile(tokenFile);
        registerRequest.setHost(RequestUtils.getRemoteHost());
        if (!registerRequest.isValid()) {
            throw SecretpadException.of(InstErrorCode.INST_REGISTER_CHECK_FAILED, "register request is invalid");
        }

        instService.registerNode(registerRequest);

        return SecretPadResponse.success();
    }

}
