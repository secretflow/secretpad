package org.secretflow.secretpad.service.model.datatable;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreateDatatableVO {

    private List<DataTableNodeInfo> DataTableNodeInfos;

    private Map<String, String> failedCreatedNodes;


    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class DataTableNodeInfo {
        private String nodeId;
        private String domainDataId;
    }
}
