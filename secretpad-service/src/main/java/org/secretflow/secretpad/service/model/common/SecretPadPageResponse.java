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

package org.secretflow.secretpad.service.model.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

/**
 * SecretPadPageResponse
 *
 * @author yutu
 * @date 2023/08/02
 */
@Data
@AllArgsConstructor
public class SecretPadPageResponse<E> implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * page list
     */
    @Schema(description = "page data")
    private List<E> list;
    /**
     * total
     */
    @Schema(description = "total")
    private long total;

    public static <T> SecretPadPageResponse<T> toPage(Page<T> page) {
        return new SecretPadPageResponse<>(page.getContent(), page.getTotalElements());
    }

    public static <T> SecretPadPageResponse<T> toPage(List<T> content, long totalElements) {
        return new SecretPadPageResponse<>(content, totalElements);
    }
}