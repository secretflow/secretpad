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

package org.secretflow.secretpad.manager.integration.datasource.odps;

import com.aliyun.odps.Odps;
import com.aliyun.odps.account.Account;
import com.aliyun.odps.account.AliyunAccount;
import org.springframework.util.Assert;

/**
 * odps factory
 *
 * @author yutu
 * @date 2024/07/23
 */
public final class OdpsFactory {

    private OdpsFactory() {
    }

    public static Odps buildOdpsClient(OdpsConfig odpsConfig) {
        Assert.notNull(odpsConfig, "odpsConfig must not be null");
        odpsConfig.validate();
        Account account = new AliyunAccount(odpsConfig.getAccessId(), odpsConfig.getAccessKey());
        Odps odps = new Odps(account);
        odps.setEndpoint(odpsConfig.getEndpoint());
        odps.setDefaultProject(odpsConfig.getProject());
        return odps;
    }
}