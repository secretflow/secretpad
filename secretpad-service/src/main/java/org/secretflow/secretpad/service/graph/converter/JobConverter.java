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

package org.secretflow.secretpad.service.graph.converter;

import org.secretflow.secretpad.common.errorcode.GraphErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.secretflow.secretpad.service.graph.converter.JobConverter.DM_INPUT;

/**
 * Job converter
 *
 * @author yansi
 * @date 2023/5/30
 */
public interface JobConverter {
    Pattern RE_UNICODE = Pattern.compile("\\\\u([0-9a-zA-Z]{4})");

    String DM_INPUT = "dm://input/?";
    String DM_OUTPUT = "dm://output/?";
    String DEFAULT_DS = "default-data-source";

    /**
     * Replace unicode char from string
     *
     * @param s target string
     * @return a new string
     */
    default String decode(String s) {
        Matcher m = RE_UNICODE.matcher(s);
        StringBuilder sb = new StringBuilder(s.length());
        while (m.find()) {
            m.appendReplacement(sb,
                    Character.toString((char) Integer.parseInt(m.group(1), 16)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    default String buildDmInputUrl(String dataTableId) {
        return DmVO.builder().id(dataTableId).build().buildDmInputUrl();
    }

    default String buildDmOutputUrl(String dataTableId) {
        return DmVO.builder().id(dataTableId).uri(dataTableId).datasource_id(DEFAULT_DS).build().buildDmOutputUrl();
    }
}

@ToString
@Setter
@Getter
@Builder
class DmVO {
    private String id;
    private String datasource_id;
    private String uri;

    public String buildDmInputUrl() {
        if (StringUtils.isEmpty(id)) {
            throw SecretpadException.of(GraphErrorCode.GRAPH_DEPENDENT_NODE_NOT_RUN, "dm input id is null");
        }
        return DM_INPUT + "id=" + id;
    }

    public String buildDmOutputUrl() {
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(uri)) {
            throw SecretpadException.of(GraphErrorCode.GRAPH_DEPENDENT_NODE_NOT_RUN, "dm input id | uri is null");
        }
        if (StringUtils.isEmpty(datasource_id)) {
            datasource_id = JobConverter.DEFAULT_DS;
        }
        return JobConverter.DM_OUTPUT + "id=" + id +
                "&&datasource_id=" + datasource_id +
                "&&uri=" + id;
    }
}
