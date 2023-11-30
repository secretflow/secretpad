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

package org.secretflow.secretpad.service.constant;

/**
 * Tee job Constants
 *
 * @author xujiening
 * @date 2023/9/19
 */
public interface TeeJobConstants {

    String DATA_MANAGEMENT = "data_management";
    String TEE_PUSH_NAME = "upload";
    String TEE_AUTH_NAME = "authorize";
    String TEE_PULL_NAME = "download";
    String TEE_DELETE_NAME = "delete";
    String VERSION = "0.0.1";

    String TEE_PUSH_ATTR_PATH_DOMAIN = "uploader/domain_id";
    String TEE_PUSH_ATTR_PATH_CERT = "uploader/cert_chain";
    String TEE_AUTH_ATTR_OWNER_DOMAIN = "owner/domain_id";
    String TEE_AUTH_ATTR_AUTH_DOMAINS = "authorization_info/domain_ids";
    String TEE_AUTH_ATTR_ROOT_CERTS = "authorization_info/root_certs";
    String TEE_AUTH_ATTR_PROJECT_ID = "authorization_info/project_id";
    String TEE_AUTH_ATTR_COLUMNS = "authorization_info/columns";
    String TEE_PULL_ATTR_PATH_DOMAIN = "receiver/domain_id";
    String TEE_PULL_ATTR_VOTE_RESULT = "receiver/vote_result";
    String TEE_DELETE_ATTR_PATH_DOMAIN = "deleter/domain_id";

    String PUSH_INPUT_NAME = "uploader_input";
    String AUTH_INPUT_NAME = "authorize_input";
    String PULL_INPUT_NAME = "downloader_input";
    String DELETE_INPUT_NAME = "deleter_input";
    String PUSH_INPUT_TYPE = "sf.table.individual";
    String DATA_REF = "dm://input?id=";
    String OUTPUT_DATASOURCE_REF = "dm://output?datasource_id=";
    String OUTPUT_ID_REF = "&id=";
    String OUTPUT_RELATIVE_URI_REF = "&uri=";

    String MOCK_VOTE_RESULT = "";
}
