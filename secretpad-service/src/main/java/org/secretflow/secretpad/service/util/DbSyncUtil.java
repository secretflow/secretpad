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

package org.secretflow.secretpad.service.util;

import org.secretflow.secretpad.common.constant.DataSyncConstants;
import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.RestTemplateUtil;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.datasync.vote.DbSyncRequest;
import org.secretflow.secretpad.service.model.datasync.vote.VoteSyncRequest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * @author cml
 * @date 2023/11/14
 */
@Component
public class DbSyncUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(DbSyncUtil.class);
    private static final String HTTP_PREFIX = "http://";
    private static String kusciaLiteGateway;
    private static String routeHeader;
    private static String nodeId;

    public static SecretPadResponse dbDataSyncToCenter(DbSyncRequest dbSyncRequest) {
        String redirectUrl = HTTP_PREFIX + kusciaLiteGateway + DataSyncConstants.VOTE_DATA_SYNC;
        LOGGER.info("data push to center,request center before sync,dbSyncRequest = {}", JsonUtils.toJSONString(dbSyncRequest));
        SecretPadResponse secretPadResponse = RestTemplateUtil.sendPostJson(redirectUrl, VoteSyncRequest.builder().dbSyncRequests(Lists.newArrayList(dbSyncRequest)).build(), parseRequestHeader(), SecretPadResponse.class);
        LOGGER.info("dbSyncRequest secretPadResponse status = {}", secretPadResponse.getStatus());
        if (secretPadResponse.getStatus().getCode() != 0) {
            throw SecretpadException.of(SystemErrorCode.REMOTE_CALL_ERROR, secretPadResponse.getStatus().getMsg());
        }
        return secretPadResponse;
    }

    public static SecretPadResponse dbDataSyncToCenter(VoteSyncRequest voteSyncRequest) {
        String redirectUrl = HTTP_PREFIX + kusciaLiteGateway + DataSyncConstants.VOTE_DATA_SYNC;
        LOGGER.info("data push to center,request center before sync,voteSyncRequest = {}", JsonUtils.toJSONString(voteSyncRequest));
        SecretPadResponse secretPadResponse = RestTemplateUtil.sendPostJson(redirectUrl, voteSyncRequest, parseRequestHeader(), SecretPadResponse.class);
        LOGGER.info("voteSyncRequest secretPadResponse status = {}", secretPadResponse.getStatus());
        if (secretPadResponse.getStatus().getCode() != 0) {
            throw SecretpadException.of(SystemErrorCode.REMOTE_CALL_ERROR, secretPadResponse.getStatus().getMsg());
        }
        return secretPadResponse;
    }

    public static SecretPadResponse dbDataSyncToNode(VoteSyncRequest voteSyncRequest, String destination) {
        String redirectUrl = HTTP_PREFIX + kusciaLiteGateway + DataSyncConstants.VOTE_DATA_SYNC;
        LOGGER.info("data push to node, destination is {}, voteSyncRequest = {}", destination, JsonUtils.toJSONString(voteSyncRequest));
        SecretPadResponse secretPadResponse = RestTemplateUtil.sendPostJson(redirectUrl, voteSyncRequest, parseRequestHeader(destination), SecretPadResponse.class);
        LOGGER.info("secretPadResponse status = {}", secretPadResponse.getStatus());
        if (secretPadResponse.getStatus().getCode() != 0) {
            throw SecretpadException.of(SystemErrorCode.REMOTE_CALL_ERROR, secretPadResponse.getStatus().getMsg());
        }
        return secretPadResponse;
    }

    private static ImmutableMap<String, String> parseRequestHeader() {
        return ImmutableMap.of("Host", routeHeader, "kuscia-origin-source", nodeId);
    }

    private static ImmutableMap<String, String> parseRequestHeader(String nodeId) {
        return ImmutableMap.of("Host", routeHeader, "kuscia-origin-source", nodeId);
    }

    @Value("${secretpad.gateway}")
    public void setKusciaLiteGateway(String kusciaLiteGateway) {
        DbSyncUtil.kusciaLiteGateway = kusciaLiteGateway;
    }

    @Value("${secretpad.center-platform-service}")
    public void setRouteHeader(String routeHeader) {
        DbSyncUtil.routeHeader = routeHeader;
    }

    @Value("${secretpad.node-id}")
    public void setNodeId(String nodeId) {
        DbSyncUtil.nodeId = nodeId;
    }
}
