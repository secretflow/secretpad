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

package org.secretflow.secretpad.service.util;

import org.secretflow.secretpad.common.errorcode.ResultErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.service.model.report.ScqlReport;
import org.secretflow.secretpad.service.model.report.SfReport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.secretflow.secretpad.manager.integration.datatable.AbstractDatatableManager.DATA_TYPE_TABLE;
import static org.secretflow.secretpad.service.constant.Constants.SCQL_INVALID_VALUE;
import static org.secretflow.secretpad.service.constant.Constants.SCQL_REPORT;

/**
 * @author lufeng
 * @date 2024/9/30
 */
public class ResultConvertUtil {
    public static String convertScqlToSfReport(ScqlReport scqlReport) {
        SfReport sfReport = new SfReport();
        List<ScqlReport.OutColumn> outColumns = scqlReport.getOutColumns();
        if (outColumns.isEmpty()) {
            throw SecretpadException.of(ResultErrorCode.SCQL_RESULT_EMPTY);
        }
        int sfRows = getRowCount(outColumns);

        List<SfReport.Header> headers = createHeaders(outColumns);
        List<SfReport.Row> rows = createRows(outColumns, sfRows);

        SfReport.Table table = new SfReport.Table();
        table.setRows(rows);
        table.setHeaders(headers);

        SfReport.Child child = new SfReport.Child();
        child.setTable(table);
        child.setType(DATA_TYPE_TABLE);

        SfReport.Div div = new SfReport.Div();
        div.setChildren(Collections.singletonList(child));

        SfReport.Tab tab = new SfReport.Tab();
        tab.setDivs(Collections.singletonList(div));

        List<SfReport.Tab> tabs = Collections.singletonList(tab);
        SfReport.Meta meta = new SfReport.Meta();
        meta.setTabs(tabs);
        sfReport.setMeta(meta);
        sfReport.setName(SCQL_REPORT);
        return JsonUtils.toJSONString(sfReport);
    }

    private static int getRowCount(List<ScqlReport.OutColumn> outColumns) {
        Set<Integer> rowCounts = outColumns.stream()
                .map(outColumn -> Integer.parseInt(outColumn.getShape().getDim().get(0).getDimValue()))
                .collect(Collectors.toSet());

        if (rowCounts.size() != 1) {
            throw SecretpadException.of(ResultErrorCode.SCQL_RESULT_ERROR, "SCQL result row count is not consistent");
        }
        return rowCounts.iterator().next();
    }

    private static List<SfReport.Header> createHeaders(List<ScqlReport.OutColumn> outColumns) {
        return outColumns.stream()
                .map(outColumn -> {
                    SfReport.Header header = new SfReport.Header();
                    header.setName(outColumn.getName());
                    header.setType("str");
                    header.setDesc("");
                    return header;
                })
                .collect(Collectors.toList());
    }

    private static List<SfReport.Row> createRows(List<ScqlReport.OutColumn> outColumns, int sfRows) {
        List<SfReport.Row> rows = new ArrayList<>();

        // get the data_validity of all columns in advance
        List<List<Boolean>> columnsDataValidity = new ArrayList<>();
        for (ScqlReport.OutColumn column : outColumns) {
            columnsDataValidity.add(column.getDataValidity());
        }

        for (int row = 0; row < sfRows; row++) {
            SfReport.Row sfRow = new SfReport.Row();
            List<SfReport.Item> items = new ArrayList<>();
            String rowName = getRowName(outColumns, row);

            for (int index = 1; index < outColumns.size(); index++) {
                SfReport.Item item = createItem(outColumns.get(index), row, isValid(columnsDataValidity.get(index), row));
                items.add(item);
            }

            boolean isValid = isValid(columnsDataValidity.get(0), row);
            sfRow.setName(isValid ? rowName : SCQL_INVALID_VALUE);
            sfRow.setItems(items);
            rows.add(sfRow);
        }
        return rows;
    }

    private static boolean isValid(List<Boolean> dataValidity, int row) {
        return dataValidity.isEmpty() || dataValidity.get(row);
    }

    private static String getRowName(List<ScqlReport.OutColumn> outColumns, int rowIndex) {
        ScqlReport.OutColumn outColumn = outColumns.get(0);
        return getDataValue(outColumn, rowIndex);
    }

    private static SfReport.Item createItem(ScqlReport.OutColumn outColumn, int rowIndex, boolean isValid) {
        SfReport.Item item = new SfReport.Item();
        item.setS(isValid ? getDataValue(outColumn, rowIndex) : SCQL_INVALID_VALUE);
        return item;
    }

    private static String getDataValue(ScqlReport.OutColumn outColumn, int rowIndex) {
        return switch (outColumn.getElemType()) {
            case "INT", "INT8", "INT16", "INT32", "INTEGER" -> String.valueOf(outColumn.getInt32Data().get(rowIndex));
            case "INT64", "TIMESTAMP" -> String.valueOf(outColumn.getInt64Data().get(rowIndex));
            case "FLOAT", "FLOAT32" -> String.valueOf(outColumn.getFloatData().get(rowIndex));
            case "DOUBLE", "FLOAT64" -> String.valueOf(outColumn.getDoubleData().get(rowIndex));
            case "BOOL" -> String.valueOf(outColumn.getBoolData().get(rowIndex));
            case "STR", "STRING", "DATETIME" -> outColumn.getStringData().get(rowIndex);
            default -> throw new RuntimeException("unsupported type: " + outColumn.getElemType());
        };
    }

}
