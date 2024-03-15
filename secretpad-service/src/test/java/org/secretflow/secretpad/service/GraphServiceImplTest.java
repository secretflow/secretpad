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

package org.secretflow.secretpad.service;

import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.persistence.model.GraphEdgeDO;
import org.secretflow.secretpad.service.impl.GraphServiceImpl;
import org.secretflow.secretpad.service.model.graph.GraphDetailVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yutu
 * @date 2024/03/06
 */
public class GraphServiceImplTest {
    String json = """
            {
              "projectId": "nqetidwt",
              "graphId": "phbnassf",
              "name": "金融风控模板",
              "nodes": [
                {
                  "codeName": "read_data/datatable",
                  "graphNodeId": "phbnassf-node-1",
                  "label": "样本表",
                  "x": -370,
                  "y": -250,
                  "inputs": [],
                  "outputs": [
                    "phbnassf-node-1-output-0"
                  ],
                  "nodeDef": {
                    "attrPaths": [
                      "datatable_selected"
                    ],
                    "attrs": [
                      {
                        "is_na": false,
                        "s": "alice-table"
                      }
                    ],
                    "domain": "read_data",
                    "name": "datatable",
                    "version": "0.0.1"
                  },
                  "status": "SUCCEED",
                  "jobId": "jmcl",
                  "taskId": "jmcl-phbnassf-node-1",
                  "results": null
                },
                {
                  "codeName": "stats/ss_vif",
                  "graphNodeId": "phbnassf-node-10",
                  "label": "VIF指标计算",
                  "x": -240,
                  "y": 190,
                  "inputs": [
                    "phbnassf-node-7-output-0"
                  ],
                  "outputs": [
                    "phbnassf-node-10-output-0"
                  ],
                  "nodeDef": {
                    "attrPaths": [
                      "input/input_data/feature_selects"
                    ],
                    "attrs": [
                      {
                        "is_na": false,
                        "ss": [
                          "age",
                          "education",
                          "default",
                          "balance",
                          "housing",
                          "loan",
                          "day",
                          "duration",
                          "campaign",
                          "pdays",
                          "previous",
                          "job_blue-collar",
                          "job_entrepreneur",
                          "job_housemaid",
                          "job_management",
                          "job_retired",
                          "job_self-employed",
                          "job_services",
                          "job_student",
                          "job_technician",
                          "job_unemployed",
                          "marital_divorced",
                          "marital_married",
                          "marital_single",
                          "contact_cellular",
                          "contact_telephone",
                          "contact_unknown",
                          "month_apr",
                          "month_aug",
                          "month_dec",
                          "month_feb",
                          "month_jan",
                          "month_jul",
                          "month_jun",
                          "month_mar",
                          "month_may",
                          "month_nov",
                          "month_oct",
                          "month_sep",
                          "poutcome_failure",
                          "poutcome_other",
                          "poutcome_success",
                          "poutcome_unknown"
                        ]
                      }
                    ],
                    "domain": "stats",
                    "name": "ss_vif",
                    "version": "0.0.1"
                  },
                  "status": "SUCCEED",
                  "jobId": "jmcl",
                  "taskId": "jmcl-phbnassf-node-10",
                  "results": null
                },
                {
                  "codeName": "ml.train/ss_sgd_train",
                  "graphNodeId": "phbnassf-node-11",
                  "label": "逻辑回归训练",
                  "x": -40,
                  "y": 220,
                  "inputs": [
                    "phbnassf-node-7-output-0"
                  ],
                  "outputs": [
                    "phbnassf-node-11-output-0"
                  ],
                  "nodeDef": {
                    "attrPaths": [
                      "input/train_dataset/label",
                      "input/train_dataset/feature_selects"
                    ],
                    "attrs": [
                      {
                        "is_na": false,
                        "ss": [
                          "y"
                        ]
                      },
                      {
                        "is_na": false,
                        "ss": [
                          "age",
                          "education",
                          "default",
                          "balance",
                          "housing",
                          "loan",
                          "day",
                          "duration",
                          "campaign",
                          "pdays",
                          "previous",
                          "job_blue-collar",
                          "job_entrepreneur",
                          "job_housemaid",
                          "job_management",
                          "job_retired",
                          "job_self-employed",
                          "job_services",
                          "job_student",
                          "job_technician",
                          "job_unemployed",
                          "marital_divorced",
                          "marital_married",
                          "marital_single",
                          "contact_cellular",
                          "contact_telephone",
                          "contact_unknown",
                          "month_apr",
                          "month_aug",
                          "month_dec",
                          "month_feb",
                          "month_jan",
                          "month_jul",
                          "month_jun",
                          "month_mar",
                          "month_may",
                          "month_nov",
                          "month_oct",
                          "month_sep",
                          "poutcome_failure",
                          "poutcome_other",
                          "poutcome_success",
                          "poutcome_unknown"
                        ]
                      }
                    ],
                    "domain": "ml.train",
                    "name": "ss_sgd_train",
                    "version": "0.0.1"
                  },
                  "status": "SUCCEED",
                  "jobId": "jmcl",
                  "taskId": "jmcl-phbnassf-node-11",
                  "results": null
                },
                {
                  "codeName": "ml.eval/ss_pvalue",
                  "graphNodeId": "phbnassf-node-12",
                  "label": "P-VALUE评估",
                  "x": -250,
                  "y": 310,
                  "inputs": [
                    "phbnassf-node-11-output-0",
                    "phbnassf-node-7-output-0"
                  ],
                  "outputs": [
                    "phbnassf-node-12-output-0"
                  ],
                  "nodeDef": {
                    "domain": "ml.eval",
                    "name": "ss_pvalue",
                    "version": "0.0.1"
                  },
                  "status": "SUCCEED",
                  "jobId": "jmcl",
                  "taskId": "jmcl-phbnassf-node-12",
                  "results": null
                },
                {
                  "codeName": "ml.predict/ss_sgd_predict",
                  "graphNodeId": "phbnassf-node-13",
                  "label": "逻辑回归预测",
                  "x": 40,
                  "y": 330,
                  "inputs": [
                    "phbnassf-node-11-output-0",
                    "phbnassf-node-8-output-0"
                  ],
                  "outputs": [
                    "phbnassf-node-13-output-0"
                  ],
                  "nodeDef": {
                    "attrPaths": [
                      "receiver",
                      "pred_name",
                      "save_label"
                    ],
                    "attrs": [
                      {
                        "is_na": false,
                        "s": "bob"
                      },
                      {
                        "is_na": false,
                        "s": "pred"
                      },
                      {
                        "b": true,
                        "is_na": false
                      }
                    ],
                    "domain": "ml.predict",
                    "name": "ss_sgd_predict",
                    "version": "0.0.1"
                  },
                  "status": "SUCCEED",
                  "jobId": "jmcl",
                  "taskId": "jmcl-phbnassf-node-13",
                  "results": null
                },
                {
                  "codeName": "ml.eval/biclassification_eval",
                  "graphNodeId": "phbnassf-node-14",
                  "label": "二分类评估",
                  "x": 130,
                  "y": 450,
                  "inputs": [
                    "phbnassf-node-13-output-0"
                  ],
                  "outputs": [
                    "phbnassf-node-14-output-0"
                  ],
                  "nodeDef": {
                    "attrPaths": [
                      "input/in_ds/label",
                      "input/in_ds/prediction"
                    ],
                    "attrs": [
                      {
                        "is_na": false,
                        "ss": [
                          "y"
                        ]
                      },
                      {
                        "is_na": false,
                        "ss": [
                          "pred"
                        ]
                      }
                    ],
                    "domain": "ml.eval",
                    "name": "biclassification_eval",
                    "version": "0.0.1"
                  },
                  "status": "SUCCEED",
                  "jobId": "jmcl",
                  "taskId": "jmcl-phbnassf-node-14",
                  "results": null
                },
                {
                  "codeName": "ml.eval/prediction_bias_eval",
                  "graphNodeId": "phbnassf-node-15",
                  "label": "预测偏差评估",
                  "x": -110,
                  "y": 540,
                  "inputs": [
                    "phbnassf-node-13-output-0"
                  ],
                  "outputs": [
                    "phbnassf-node-15-output-0"
                  ],
                  "nodeDef": {
                    "attrPaths": [
                      "input/in_ds/label",
                      "input/in_ds/prediction"
                    ],
                    "attrs": [
                      {
                        "is_na": false,
                        "ss": [
                          "y"
                        ]
                      },
                      {
                        "is_na": false,
                        "ss": [
                          "pred"
                        ]
                      }
                    ],
                    "domain": "ml.eval",
                    "name": "prediction_bias_eval",
                    "version": "0.0.1"
                  },
                  "status": "SUCCEED",
                  "jobId": "jmcl",
                  "taskId": "jmcl-phbnassf-node-15",
                  "results": null
                },
                {
                  "codeName": "read_data/datatable",
                  "graphNodeId": "phbnassf-node-2",
                  "label": "样本表",
                  "x": -140,
                  "y": -250,
                  "inputs": [],
                  "outputs": [
                    "phbnassf-node-2-output-0"
                  ],
                  "nodeDef": {
                    "attrPaths": [
                      "datatable_selected"
                    ],
                    "attrs": [
                      {
                        "is_na": false,
                        "s": "bob-table"
                      }
                    ],
                    "domain": "read_data",
                    "name": "datatable",
                    "version": "0.0.1"
                  },
                  "status": "SUCCEED",
                  "jobId": "jmcl",
                  "taskId": "jmcl-phbnassf-node-2",
                  "results": null
                },
                {
                  "codeName": "data_prep/psi",
                  "graphNodeId": "phbnassf-node-3",
                  "label": "隐私求交",
                  "x": -240,
                  "y": -160,
                  "inputs": [
                    "phbnassf-node-1-output-0",
                    "phbnassf-node-2-output-0"
                  ],
                  "outputs": [
                    "phbnassf-node-3-output-0"
                  ],
                  "nodeDef": {
                    "attrPaths": [
                      "input/receiver_input/key",
                      "input/sender_input/key"
                    ],
                    "attrs": [
                      {
                        "is_na": false,
                        "ss": [
                          "id1"
                        ]
                      },
                      {
                        "is_na": false,
                        "ss": [
                          "id2"
                        ]
                      }
                    ],
                    "domain": "data_prep",
                    "name": "psi",
                    "version": "0.0.2"
                  },
                  "status": "SUCCEED",
                  "jobId": "jmcl",
                  "taskId": "jmcl-phbnassf-node-3",
                  "results": null
                },
                {
                  "codeName": "stats/table_statistics",
                  "graphNodeId": "phbnassf-node-4",
                  "label": "全表统计",
                  "x": -430,
                  "y": -90,
                  "inputs": [
                    "phbnassf-node-3-output-0"
                  ],
                  "outputs": [
                    "phbnassf-node-4-output-0"
                  ],
                  "nodeDef": {
                    "attrPaths": [
                      "input/input_data/features"
                    ],
                    "attrs": [
                      {
                        "is_na": false,
                        "ss": [
                          "age",
                          "education",
                          "default",
                          "balance",
                          "housing",
                          "loan",
                          "day",
                          "duration",
                          "campaign",
                          "pdays",
                          "previous",
                          "job_blue-collar",
                          "job_entrepreneur",
                          "job_housemaid",
                          "job_management",
                          "job_retired",
                          "job_self-employed",
                          "job_services",
                          "job_student",
                          "job_technician",
                          "job_unemployed",
                          "marital_divorced",
                          "marital_married",
                          "marital_single",
                          "contact_cellular",
                          "contact_telephone",
                          "contact_unknown",
                          "month_apr",
                          "month_aug",
                          "month_dec",
                          "month_feb",
                          "month_jan",
                          "month_jul",
                          "month_jun",
                          "month_mar",
                          "month_may",
                          "month_nov",
                          "month_oct",
                          "month_sep",
                          "poutcome_failure",
                          "poutcome_other",
                          "poutcome_success",
                          "poutcome_unknown"
                        ]
                      }
                    ],
                    "domain": "stats",
                    "name": "table_statistics",
                    "version": "0.0.2"
                  },
                  "status": "SUCCEED",
                  "jobId": "jmcl",
                  "taskId": "jmcl-phbnassf-node-4",
                  "results": null
                },
                {
                  "codeName": "data_prep/train_test_split",
                  "graphNodeId": "phbnassf-node-5",
                  "label": "随机分割",
                  "x": -120,
                  "y": -80,
                  "inputs": [
                    "phbnassf-node-3-output-0"
                  ],
                  "outputs": [
                    "phbnassf-node-5-output-0",
                    "phbnassf-node-5-output-1"
                  ],
                  "nodeDef": {
                    "domain": "data_prep",
                    "name": "train_test_split",
                    "version": "0.0.1"
                  },
                  "status": "SUCCEED",
                  "jobId": "jmcl",
                  "taskId": "jmcl-phbnassf-node-5",
                  "results": null
                },
                {
                  "codeName": "feature/vert_woe_binning",
                  "graphNodeId": "phbnassf-node-6",
                  "label": "WOE分箱",
                  "x": -140,
                  "y": 20,
                  "inputs": [
                    "phbnassf-node-5-output-0"
                  ],
                  "outputs": [
                    "phbnassf-node-6-output-0",
                    "phbnassf-node-6-output-1"
                  ],
                  "nodeDef": {
                    "attrPaths": [
                      "input/input_data/feature_selects",
                      "input/input_data/label"
                    ],
                    "attrs": [
                      {
                        "is_na": false,
                        "ss": [
                          "age",
                          "education",
                          "default",
                          "balance",
                          "housing",
                          "loan",
                          "day",
                          "duration",
                          "campaign",
                          "pdays",
                          "previous",
                          "job_blue-collar",
                          "job_entrepreneur",
                          "job_housemaid",
                          "job_management",
                          "job_retired",
                          "job_self-employed",
                          "job_services",
                          "job_student",
                          "job_technician",
                          "job_unemployed",
                          "marital_divorced",
                          "marital_married",
                          "marital_single",
                          "contact_cellular",
                          "contact_telephone",
                          "contact_unknown",
                          "month_apr",
                          "month_aug",
                          "month_dec",
                          "month_feb",
                          "month_jan",
                          "month_jul",
                          "month_jun",
                          "month_mar",
                          "month_may",
                          "month_nov",
                          "month_oct",
                          "month_sep",
                          "poutcome_failure",
                          "poutcome_other",
                          "poutcome_success",
                          "poutcome_unknown"
                        ]
                      },
                      {
                        "is_na": false,
                        "ss": [
                          "y"
                        ]
                      }
                    ],
                    "domain": "feature",
                    "name": "vert_woe_binning",
                    "version": "0.0.2"
                  },
                  "status": "SUCCEED",
                  "jobId": "jmcl",
                  "taskId": "jmcl-phbnassf-node-6",
                  "results": null
                },
                {
                  "codeName": "preprocessing/vert_bin_substitution",
                  "graphNodeId": "phbnassf-node-7",
                  "label": "分箱转换",
                  "x": -320,
                  "y": 110,
                  "inputs": [
                    "phbnassf-node-5-output-0",
                    "phbnassf-node-6-output-0"
                  ],
                  "outputs": [
                    "phbnassf-node-7-output-0"
                  ],
                  "nodeDef": {
                    "domain": "preprocessing",
                    "name": "vert_bin_substitution",
                    "version": "0.0.1"
                  },
                  "status": "SUCCEED",
                  "jobId": "jmcl",
                  "taskId": "jmcl-phbnassf-node-7",
                  "results": null
                },
                {
                  "codeName": "preprocessing/vert_bin_substitution",
                  "graphNodeId": "phbnassf-node-8",
                  "label": "分箱转换",
                  "x": -10,
                  "y": 100,
                  "inputs": [
                    "phbnassf-node-5-output-1",
                    "phbnassf-node-6-output-0"
                  ],
                  "outputs": [
                    "phbnassf-node-8-output-0"
                  ],
                  "nodeDef": {
                    "domain": "preprocessing",
                    "name": "vert_bin_substitution",
                    "version": "0.0.1"
                  },
                  "status": "SUCCEED",
                  "jobId": "jmcl",
                  "taskId": "jmcl-phbnassf-node-8",
                  "results": null
                },
                {
                  "codeName": "stats/ss_pearsonr",
                  "graphNodeId": "phbnassf-node-9",
                  "label": "相关系数矩阵",
                  "x": -450,
                  "y": 190,
                  "inputs": [
                    "phbnassf-node-7-output-0"
                  ],
                  "outputs": [
                    "phbnassf-node-9-output-0"
                  ],
                  "nodeDef": {
                    "attrPaths": [
                      "input/input_data/feature_selects"
                    ],
                    "attrs": [
                      {
                        "is_na": false,
                        "ss": [
                          "age",
                          "education",
                          "default",
                          "balance",
                          "housing",
                          "loan",
                          "day",
                          "duration",
                          "campaign",
                          "pdays",
                          "previous",
                          "job_blue-collar",
                          "job_entrepreneur",
                          "job_housemaid",
                          "job_management",
                          "job_retired",
                          "job_self-employed",
                          "job_services",
                          "job_student",
                          "job_technician",
                          "job_unemployed",
                          "marital_divorced",
                          "marital_married",
                          "marital_single",
                          "contact_cellular",
                          "contact_telephone",
                          "contact_unknown",
                          "month_apr",
                          "month_aug",
                          "month_dec",
                          "month_feb",
                          "month_jan",
                          "month_jul",
                          "month_jun",
                          "month_mar",
                          "month_may",
                          "month_nov",
                          "month_oct",
                          "month_sep",
                          "poutcome_failure",
                          "poutcome_other",
                          "poutcome_success",
                          "poutcome_unknown"
                        ]
                      }
                    ],
                    "domain": "stats",
                    "name": "ss_pearsonr",
                    "version": "0.0.1"
                  },
                  "status": "SUCCEED",
                  "jobId": "jmcl",
                  "taskId": "jmcl-phbnassf-node-9",
                  "results": null
                }
              ],
              "edges": [
                {
                  "edgeId": "phbnassf-node-1-output-0__phbnassf-node-3-input-0",
                  "source": "phbnassf-node-1",
                  "sourceAnchor": "phbnassf-node-1-output-0",
                  "target": "phbnassf-node-3",
                  "targetAnchor": "phbnassf-node-3-input-0"
                },
                {
                  "edgeId": "phbnassf-node-2-output-0__phbnassf-node-3-input-1",
                  "source": "phbnassf-node-2",
                  "sourceAnchor": "phbnassf-node-2-output-0",
                  "target": "phbnassf-node-3",
                  "targetAnchor": "phbnassf-node-3-input-1"
                },
                {
                  "edgeId": "phbnassf-node-3-output-0__phbnassf-node-4-input-0",
                  "source": "phbnassf-node-3",
                  "sourceAnchor": "phbnassf-node-3-output-0",
                  "target": "phbnassf-node-4",
                  "targetAnchor": "phbnassf-node-4-input-0"
                },
                {
                  "edgeId": "phbnassf-node-3-output-0__phbnassf-node-5-input-0",
                  "source": "phbnassf-node-3",
                  "sourceAnchor": "phbnassf-node-3-output-0",
                  "target": "phbnassf-node-5",
                  "targetAnchor": "phbnassf-node-5-input-0"
                },
                {
                  "edgeId": "phbnassf-node-5-output-0__phbnassf-node-6-input-0",
                  "source": "phbnassf-node-5",
                  "sourceAnchor": "phbnassf-node-5-output-0",
                  "target": "phbnassf-node-6",
                  "targetAnchor": "phbnassf-node-6-input-0"
                },
                {
                  "edgeId": "phbnassf-node-5-output-0__phbnassf-node-7-input-0",
                  "source": "phbnassf-node-5",
                  "sourceAnchor": "phbnassf-node-5-output-0",
                  "target": "phbnassf-node-7",
                  "targetAnchor": "phbnassf-node-7-input-0"
                },
                {
                  "edgeId": "phbnassf-node-6-output-0__phbnassf-node-7-input-1",
                  "source": "phbnassf-node-6",
                  "sourceAnchor": "phbnassf-node-6-output-0",
                  "target": "phbnassf-node-7",
                  "targetAnchor": "phbnassf-node-7-input-1"
                },
                {
                  "edgeId": "phbnassf-node-5-output-1__phbnassf-node-8-input-0",
                  "source": "phbnassf-node-5",
                  "sourceAnchor": "phbnassf-node-5-output-1",
                  "target": "phbnassf-node-8",
                  "targetAnchor": "phbnassf-node-8-input-0"
                },
                {
                  "edgeId": "phbnassf-node-6-output-0__phbnassf-node-8-input-1",
                  "source": "phbnassf-node-6",
                  "sourceAnchor": "phbnassf-node-6-output-0",
                  "target": "phbnassf-node-8",
                  "targetAnchor": "phbnassf-node-8-input-1"
                },
                {
                  "edgeId": "phbnassf-node-7-output-0__phbnassf-node-9-input-0",
                  "source": "phbnassf-node-7",
                  "sourceAnchor": "phbnassf-node-7-output-0",
                  "target": "phbnassf-node-9",
                  "targetAnchor": "phbnassf-node-9-input-0"
                },
                {
                  "edgeId": "phbnassf-node-7-output-0__phbnassf-node-10-input-0",
                  "source": "phbnassf-node-7",
                  "sourceAnchor": "phbnassf-node-7-output-0",
                  "target": "phbnassf-node-10",
                  "targetAnchor": "phbnassf-node-10-input-0"
                },
                {
                  "edgeId": "phbnassf-node-7-output-0__phbnassf-node-11-input-0",
                  "source": "phbnassf-node-7",
                  "sourceAnchor": "phbnassf-node-7-output-0",
                  "target": "phbnassf-node-11",
                  "targetAnchor": "phbnassf-node-11-input-0"
                },
                {
                  "edgeId": "phbnassf-node-7-output-0__phbnassf-node-12-input-1",
                  "source": "phbnassf-node-7",
                  "sourceAnchor": "phbnassf-node-7-output-0",
                  "target": "phbnassf-node-12",
                  "targetAnchor": "phbnassf-node-12-input-1"
                },
                {
                  "edgeId": "phbnassf-node-11-output-0__phbnassf-node-12-input-0",
                  "source": "phbnassf-node-11",
                  "sourceAnchor": "phbnassf-node-11-output-0",
                  "target": "phbnassf-node-12",
                  "targetAnchor": "phbnassf-node-12-input-0"
                },
                {
                  "edgeId": "phbnassf-node-11-output-0__phbnassf-node-13-input-0",
                  "source": "phbnassf-node-11",
                  "sourceAnchor": "phbnassf-node-11-output-0",
                  "target": "phbnassf-node-13",
                  "targetAnchor": "phbnassf-node-13-input-0"
                },
                {
                  "edgeId": "phbnassf-node-8-output-0__phbnassf-node-13-input-1",
                  "source": "phbnassf-node-8",
                  "sourceAnchor": "phbnassf-node-8-output-0",
                  "target": "phbnassf-node-13",
                  "targetAnchor": "phbnassf-node-13-input-1"
                },
                {
                  "edgeId": "phbnassf-node-13-output-0__phbnassf-node-15-input-0",
                  "source": "phbnassf-node-13",
                  "sourceAnchor": "phbnassf-node-13-output-0",
                  "target": "phbnassf-node-15",
                  "targetAnchor": "phbnassf-node-15-input-0"
                },
                {
                  "edgeId": "phbnassf-node-13-output-0__phbnassf-node-14-input-0",
                  "source": "phbnassf-node-13",
                  "sourceAnchor": "phbnassf-node-13-output-0",
                  "target": "phbnassf-node-14",
                  "targetAnchor": "phbnassf-node-14-input-0"
                }
              ]
            }
            """;

    //    @Test
    void getPartyByReadTableEdges() {
        GraphDetailVO graphDetailVO = JsonUtils.toJavaObject(json, GraphDetailVO.class);
        Map<String, List<String>> map = new HashMap<>();
        GraphServiceImpl graphService = new GraphServiceImpl();
        List<GraphEdgeDO> edges = new ArrayList<>();
        graphDetailVO.getEdges().forEach(e -> {
            edges.add(GraphEdgeDO.builder().source(e.getSource()).target(e.getTarget()).build());
        });
//        graphService.getPartyByReadTableEdges(edges, "phbnassf-node-1", map);
    }
}