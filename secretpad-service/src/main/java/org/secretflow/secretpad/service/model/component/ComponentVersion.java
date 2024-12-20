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

package org.secretflow.secretpad.service.model.component;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComponentVersion {

    /**
     * teeDmImage
     */
    @Schema(description = "teeDmImage")
    private String teeDmImage;


    /**
     * teeAppImage
     */
    @Schema(description = "teeAppImage")
    private String teeAppImage;


    /**
     * capsuleManagerImage
     */
    @Schema(description = "capsuleManagerSimImage")
    private String capsuleManagerSimImage;


    /**
     * secretpadImage
     */
    @Schema(description = "secretpadImage")
    private String secretpadImage;

    /**
     * secretflowServingImage
     */
    @Schema(description = "secretflowServingImage")
    private String secretflowServingImage;


    /**
     * kusciaImage
     */
    @Schema(description = "kusciaImage")
    private String kusciaImage;


    /**
     * secretflowImage
     */
    @Schema(description = "secretflowImage")
    private String secretflowImage;

    /**
     * dataProxyImage
     */
    @Schema(description = "dataProxyImage")
    private String dataProxyImage;

    @Schema(description = "scqlImage")
    private String scqlImage;
}
