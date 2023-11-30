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

package org.secretflow.secretpad.common.util;

import org.springframework.data.domain.Page;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * PageUtils.
 *
 * @author cml
 * @date 2023/09/28
 */
public class PageUtils {


    public static <I, O> List<O> convert(Page<I> in, Function<I, O> function) {
        List<O> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(in.getContent())) {
            in.getContent().stream().forEach(e -> result.add(function.apply(e)));
        }
        return result;
    }


    public static <I, O> List<O> convert(Collection<I> in, Function<I, O> function) {
        List<O> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(in)) {
            in.stream().forEach(e -> result.add(function.apply(e)));
        }
        return result;
    }
}
