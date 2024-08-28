package org.secretflow.secretpad.manager.integration.node;

import java.util.Map;

public interface SearchTargetNodeManager {

    String getTargetNodeId(String currentNodeId , Map<String ,Object> extra);

}
