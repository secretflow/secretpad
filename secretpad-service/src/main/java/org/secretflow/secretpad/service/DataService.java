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

package org.secretflow.secretpad.service;

import org.secretflow.secretpad.service.model.data.*;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Data service interface
 *
 * @author xiaonan
 * @date 2023/6/7
 */
public interface DataService {

    /**
     * Upload data
     *
     * @param file   target file
     * @param nodeId target nodeId
     * @return data result view object
     */
    UploadDataResultVO upload(MultipartFile file, String nodeId);

    /**
     * Create data schema
     *
     * @param request create data request
     * @return domaindata id
     */
    String createData(CreateDataRequest request);

    /**
     * Download data
     *
     * @param request download data request
     * @return file name and path for downloading data（relative uri）
     */
    DownloadInfo download(DownloadDataRequest request);

    /**
     * createDataByDataSource
     *
     * @param request request info
     * @return domaindata id
     */
    String createDataByDataSource(CreateDataByDataSourceRequest request);

    /**
     * queryDataSources
     *
     * @return List<DataSourceVO>
     */
    List<DataSourceVO> queryDataSources();
}
