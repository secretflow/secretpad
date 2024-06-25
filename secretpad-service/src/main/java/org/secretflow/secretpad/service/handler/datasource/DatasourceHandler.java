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

package org.secretflow.secretpad.service.handler.datasource;

import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;
import org.secretflow.secretpad.service.model.datasource.*;

import java.util.List;

/**
 * @author chenmingliang
 * @date 2024/05/27
 */
public interface DatasourceHandler {

    List<DataSourceTypeEnum> supports();

    List<DatasourceListInfo> listDatasource(DatasourceListRequest datasourceListRequest);

    CreateDatasourceVO createDatasource(CreateDatasourceRequest createDatasourceRequest);

    void deleteDatasource(DeleteDatasourceRequest deleteDatasourceRequest);

    DatasourceDetailVO datasourceDetail(DatasourceDetailRequest datasourceDetailRequest);

}
