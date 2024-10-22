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

package org.secretflow.secretpad.persistence.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * scheduled  graph create request
 *
 * @author yutu
 * @date 2024/08/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledGraphCreateRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 5005877919773504643L;

    @NotBlank
    @Size(max = 20, message = "scheduleId length must be less than or equal to 20")
    private String scheduleId;

    @Size(max = 200, message = "scheduleDesc length must be less than or equal to 200")
    private String scheduleDesc;

    @Valid
    private Cron cron;

    @NotBlank
    private String projectId;

    @NotBlank
    private String graphId;

    @NotEmpty
    private List<String> nodes;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Cron implements Serializable {
        @Serial
        private static final long serialVersionUID = 5005877919773504643L;

        @NotBlank
        private String startTime;
        @NotBlank
        private String endTime;
        @NotBlank
        private String scheduleCycle;

        private String scheduleDate;
        @NotBlank
        private String scheduleTime;
    }
}