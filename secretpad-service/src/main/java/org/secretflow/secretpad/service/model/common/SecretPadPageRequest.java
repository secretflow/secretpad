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

import org.secretflow.secretpad.common.constant.DatabaseConstants;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yutu
 * @date 2023/08/06
 */
@Slf4j
@Getter
@Setter
@ToString
public class SecretPadPageRequest {

    /**
     * page num default 1
     */
    private int page = 1;
    /**
     * page size default 10
     */
    private int size = 10;
    /**
     * sort，property,property(,ASC|DESC) "createdDate,desc"
     */
    private Map<String, String> sort;

    public Pageable of() {
        Map<String, String> sortMap = this.getSort();
        Sort sort;
        if (CollectionUtils.isEmpty(sortMap)) {
            sort = Sort.by(Sort.Direction.DESC, DatabaseConstants.GMT_CREATE);
        } else {
            Set<String> keySet = sortMap.keySet();
            List<Sort.Order> list = new ArrayList<>(keySet.size());
            keySet.forEach(key -> {
                Sort.Direction direction;
                try {
                    direction = Sort.Direction.fromString(sortMap.get(key));
                } catch (Exception e) {
                    log.warn("SecretPadPageRequest Sort Direction error: {} now make it default DESC", sortMap.get(key));
                    direction = Sort.Direction.DESC;
                }
                list.add(new Sort.Order(direction, key));
            });
            sort = Sort.by(list);
        }
        return PageRequest.of(this.getPage() - 1, this.getSize(), sort);
    }
}