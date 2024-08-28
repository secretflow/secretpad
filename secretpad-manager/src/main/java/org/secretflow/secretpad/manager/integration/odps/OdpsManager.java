/*
 * Copyright 2024 Ant Group Co., Ltd.
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

package org.secretflow.secretpad.manager.integration.odps;

import org.secretflow.secretpad.common.errorcode.DatasourceErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;

import com.aliyun.odps.Odps;
import com.aliyun.odps.task.SQLTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author yutu
 * @date 2024/07/23
 */
@Slf4j
@Service
public class OdpsManager {

    private final static String ODPS_TASK_TEST_SQL = "show flags;";

    public boolean testConnection(OdpsConfig odpsConfig) {
        Odps odps = OdpsFactory.buildOdpsClient(odpsConfig);
        try {
            SQLTask.run(odps, ODPS_TASK_TEST_SQL);
        } catch (Exception e) {
            log.error("test odps connection failed, error: {}", e.getMessage(), e);
            throw SecretpadException.of(DatasourceErrorCode.DATA_SOURCE_CREATE_FAIL, e, "odps test connection failed: " + e.getMessage());
        }
        return true;
    }

}