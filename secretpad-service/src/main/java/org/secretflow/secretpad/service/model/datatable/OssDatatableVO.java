package org.secretflow.secretpad.service.model.datatable;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OssDatatableVO {

    private String domainDataId;

    private Map<String, String> failedCreatedNodes;
}
