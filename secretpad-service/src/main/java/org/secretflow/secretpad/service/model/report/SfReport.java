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

package org.secretflow.secretpad.service.model.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SfReport {
    private String name;
    private String type;
    private SystemInfo systemInfo;
    private Meta meta;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemInfo {
        private String app;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        private String type;
        private String name;
        private List<Tab> tabs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Tab {
        private List<Div> divs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Div {
        private List<Child> children;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Child {
        private String type;
        private Table table;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Table {
        private List<Header> headers;
        private List<Row> rows;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Header {
        private String name;
        private String type;
        private String desc;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Row {
        private String name;
        private List<Item> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String s;
        private float f;
        private String i64;
        private boolean b;
        private List<Float> fs = new ArrayList<>();
        private List<String> i64s = new ArrayList<>();
        private List<String> ss = new ArrayList<>();
        private List<Boolean> bs = new ArrayList<>();
        @JsonProperty("is_na")
        private boolean isNa;
    }
}
