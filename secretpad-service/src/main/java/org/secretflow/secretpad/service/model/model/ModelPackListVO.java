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

package org.secretflow.secretpad.service.model.model;

import org.secretflow.secretpad.service.model.common.AbstractPageResponse;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author chenmingliang
 * @date 2024/01/19
 */
@Getter
@Setter
public class ModelPackListVO extends AbstractPageResponse {

    private List<ModelPackVO> modelPacks;

    public static ModelPackListVO instance(List<ModelPackVO> modelPacks, Integer pageNum, Integer pageSize, Long total) {
        ModelPackListVO vo = new ModelPackListVO();
        vo.setModelPacks(modelPacks);
        vo.setPageNum(pageNum);
        vo.setPageSize(pageSize);
        vo.setTotal(total);
        vo.setTotalPage(total % pageSize == 0 ? total / pageSize : total / pageSize + 1);
        return vo;
    }
}
