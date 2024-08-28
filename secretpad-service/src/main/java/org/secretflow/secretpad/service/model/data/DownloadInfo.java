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

package org.secretflow.secretpad.service.model.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;

/**
 * Download information
 *
 * @author yansi
 * @date 2023/5/10
 */
@Getter
@Setter
@Builder
public class DownloadInfo {

    /**
     * File name
     */
    @Schema(description = "file name")
    private String fileName;

    /**
     * File path
     */
    @Schema(description = "file length")
    private int fileLength;

    @Schema(description = "file byte array")
    private InputStream inputStream;
}