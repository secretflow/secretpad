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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;
import org.secretflow.secretpad.common.errorcode.*;
import org.secretflow.secretpad.common.util.DateTimes;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.datatable.DatatableManager;
import org.secretflow.secretpad.manager.integration.node.NodeManager;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.ResultKind;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.model.node.*;
import org.secretflow.secretpad.web.utils.FakerUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.DomainOuterClass;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.*;

import static org.secretflow.secretpad.common.errorcode.NodeErrorCode.NODE_TOKEN_IS_EMPTY_ERROR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Node controller test
 *
 * @author xjn
 * @date 2023/8/2
 */
class NodeControllerTest extends ControllerTest {

    private static final String PROJECT_ID = "projectagdasvacaghyhbvscvyjnba";
    private static final String GRAPH_ID = "graphagdasvacaghyhbvscvyjnba";

    @MockBean
    private ProjectResultRepository resultRepository;

    @MockBean
    private ProjectReportRepository reportRepository;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private ProjectJobRepository projectJobRepository;

    @MockBean
    private ProjectGraphRepository projectGraphRepository;

    @MockBean
    private ProjectDatatableRepository datatableRepository;

    @MockBean
    private NodeRepository nodeRepository;

    @MockBean
    private InstRepository instRepository;

    @MockBean
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;

    @Resource
    private NodeManager nodeManager;

    @Resource
    private DatatableManager datatableManager;

    private static final String sfContent = "{\n" +
            "    \"name\": \"lrwd_cvbizkoe_node_3_output_1\",\n" +
            "    \"type\": \"sf.report\",\n" +
            "    \"systemInfo\": {\n" +
            "        \"app\": \"\"\n" +
            "    },\n" +
            "    \"meta\": {\n" +
            "        \"@type\": \"type.googleapis.com/secretflow.spec.v1.Report\",\n" +
            "        \"name\": \"psi_report\",\n" +
            "        \"tabs\": [\n" +
            "            {\n" +
            "                \"divs\": [\n" +
            "                    {\n" +
            "                        \"children\": [\n" +
            "                            {\n" +
            "                                \"type\": \"table\",\n" +
            "                                \"table\": {\n" +
            "                                    \"headers\": [\n" +
            "                                        {\n" +
            "                                            \"name\": \"party\",\n" +
            "                                            \"type\": \"str\",\n" +
            "                                            \"desc\": \"\"\n" +
            "                                        },\n" +
            "                                        {\n" +
            "                                            \"name\": \"original_count\",\n" +
            "                                            \"type\": \"int\",\n" +
            "                                            \"desc\": \"\"\n" +
            "                                        },\n" +
            "                                        {\n" +
            "                                            \"name\": \"output_count\",\n" +
            "                                            \"type\": \"int\",\n" +
            "                                            \"desc\": \"\"\n" +
            "                                        }\n" +
            "                                    ],\n" +
            "                                    \"rows\": [\n" +
            "                                        {\n" +
            "                                            \"name\": \"0\",\n" +
            "                                            \"items\": [\n" +
            "                                                {\n" +
            "                                                    \"s\": \"alice\",\n" +
            "                                                    \"f\": 0,\n" +
            "                                                    \"i64\": \"0\",\n" +
            "                                                    \"b\": false,\n" +
            "                                                    \"fs\": [],\n" +
            "                                                    \"i64s\": [],\n" +
            "                                                    \"ss\": [],\n" +
            "                                                    \"bs\": [],\n" +
            "                                                    \"isNa\": false\n" +
            "                                                },\n" +
            "                                                {\n" +
            "                                                    \"i64\": \"9892\",\n" +
            "                                                    \"f\": 0,\n" +
            "                                                    \"s\": \"\",\n" +
            "                                                    \"b\": false,\n" +
            "                                                    \"fs\": [],\n" +
            "                                                    \"i64s\": [],\n" +
            "                                                    \"ss\": [],\n" +
            "                                                    \"bs\": [],\n" +
            "                                                    \"isNa\": false\n" +
            "                                                },\n" +
            "                                                {\n" +
            "                                                    \"i64\": \"9892\",\n" +
            "                                                    \"f\": 0,\n" +
            "                                                    \"s\": \"\",\n" +
            "                                                    \"b\": false,\n" +
            "                                                    \"fs\": [],\n" +
            "                                                    \"i64s\": [],\n" +
            "                                                    \"ss\": [],\n" +
            "                                                    \"bs\": [],\n" +
            "                                                    \"isNa\": false\n" +
            "                                                }\n" +
            "                                            ],\n" +
            "                                            \"desc\": \"\"\n" +
            "                                        },\n" +
            "                                        {\n" +
            "                                            \"name\": \"1\",\n" +
            "                                            \"items\": [\n" +
            "                                                {\n" +
            "                                                    \"s\": \"bob\",\n" +
            "                                                    \"f\": 0,\n" +
            "                                                    \"i64\": \"0\",\n" +
            "                                                    \"b\": false,\n" +
            "                                                    \"fs\": [],\n" +
            "                                                    \"i64s\": [],\n" +
            "                                                    \"ss\": [],\n" +
            "                                                    \"bs\": [],\n" +
            "                                                    \"isNa\": false\n" +
            "                                                },\n" +
            "                                                {\n" +
            "                                                    \"i64\": \"9892\",\n" +
            "                                                    \"f\": 0,\n" +
            "                                                    \"s\": \"\",\n" +
            "                                                    \"b\": false,\n" +
            "                                                    \"fs\": [],\n" +
            "                                                    \"i64s\": [],\n" +
            "                                                    \"ss\": [],\n" +
            "                                                    \"bs\": [],\n" +
            "                                                    \"isNa\": false\n" +
            "                                                },\n" +
            "                                                {\n" +
            "                                                    \"i64\": \"9892\",\n" +
            "                                                    \"f\": 0,\n" +
            "                                                    \"s\": \"\",\n" +
            "                                                    \"b\": false,\n" +
            "                                                    \"fs\": [],\n" +
            "                                                    \"i64s\": [],\n" +
            "                                                    \"ss\": [],\n" +
            "                                                    \"bs\": [],\n" +
            "                                                    \"isNa\": false\n" +
            "                                                }\n" +
            "                                            ],\n" +
            "                                            \"desc\": \"\"\n" +
            "                                        }\n" +
            "                                    ],\n" +
            "                                    \"name\": \"\",\n" +
            "                                    \"desc\": \"\"\n" +
            "                                }\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"name\": \"\",\n" +
            "                        \"desc\": \"\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"name\": \"\",\n" +
            "                \"desc\": \"\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"desc\": \"\",\n" +
            "        \"errCode\": 0,\n" +
            "        \"errDetail\": \"\"\n" +
            "    },\n" +
            "    \"dataRefs\": []\n" +
            "}";

    public static final String scqlContent = "{\n" +
            "    \"affected_rows\": \"0\",\n" +
            "    \"warnings\": [],\n" +
            "    \"cost_time_s\": 4.129290037,\n" +
            "    \"out_columns\": [\n" +
            "        {\n" +
            "            \"name\": \"id1\",\n" +
            "            \"shape\": {\n" +
            "                \"dim\": [\n" +
            "                    {\n" +
            "                        \"dim_value\": \"987\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"dim_value\": \"1\"\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            \"elem_type\": \"INT64\",\n" +
            "            \"option\": \"VALUE\",\n" +
            "            \"annotation\": {\n" +
            "                \"status\": \"TENSORSTATUS_UNKNOWN\"\n" +
            "            },\n" +
            "            \"int32_data\": [],\n" +
            "            \"int64_data\": [\n" +
            "                \"0\",\n" +
            "                \"10\",\n" +
            "                \"20\",\n" +
            "                \"30\",\n" +
            "                \"40\",\n" +
            "                \"50\",\n" +
            "                \"60\",\n" +
            "                \"70\",\n" +
            "                \"80\",\n" +
            "                \"90\",\n" +
            "                \"100\",\n" +
            "                \"110\",\n" +
            "                \"120\",\n" +
            "                \"130\",\n" +
            "                \"140\",\n" +
            "                \"150\",\n" +
            "                \"160\",\n" +
            "                \"170\",\n" +
            "                \"180\",\n" +
            "                \"190\",\n" +
            "                \"200\",\n" +
            "                \"210\",\n" +
            "                \"220\",\n" +
            "                \"230\",\n" +
            "                \"240\",\n" +
            "                \"250\",\n" +
            "                \"260\",\n" +
            "                \"270\",\n" +
            "                \"290\",\n" +
            "                \"300\",\n" +
            "                \"310\",\n" +
            "                \"320\",\n" +
            "                \"330\",\n" +
            "                \"340\",\n" +
            "                \"360\",\n" +
            "                \"370\",\n" +
            "                \"380\",\n" +
            "                \"390\",\n" +
            "                \"400\",\n" +
            "                \"410\",\n" +
            "                \"420\",\n" +
            "                \"430\",\n" +
            "                \"440\",\n" +
            "                \"450\",\n" +
            "                \"460\",\n" +
            "                \"470\",\n" +
            "                \"480\",\n" +
            "                \"490\",\n" +
            "                \"500\",\n" +
            "                \"510\",\n" +
            "                \"520\",\n" +
            "                \"530\",\n" +
            "                \"550\",\n" +
            "                \"560\",\n" +
            "                \"570\",\n" +
            "                \"580\",\n" +
            "                \"600\",\n" +
            "                \"630\",\n" +
            "                \"640\",\n" +
            "                \"650\",\n" +
            "                \"670\",\n" +
            "                \"680\",\n" +
            "                \"690\",\n" +
            "                \"700\",\n" +
            "                \"710\",\n" +
            "                \"720\",\n" +
            "                \"730\",\n" +
            "                \"740\",\n" +
            "                \"750\",\n" +
            "                \"760\",\n" +
            "                \"770\",\n" +
            "                \"780\",\n" +
            "                \"790\",\n" +
            "                \"810\",\n" +
            "                \"820\",\n" +
            "                \"830\",\n" +
            "                \"840\",\n" +
            "                \"850\",\n" +
            "                \"860\",\n" +
            "                \"870\",\n" +
            "                \"880\",\n" +
            "                \"890\",\n" +
            "                \"900\",\n" +
            "                \"920\",\n" +
            "                \"930\",\n" +
            "                \"940\",\n" +
            "                \"950\",\n" +
            "                \"960\",\n" +
            "                \"970\",\n" +
            "                \"980\",\n" +
            "                \"1000\",\n" +
            "                \"1010\",\n" +
            "                \"1020\",\n" +
            "                \"1030\",\n" +
            "                \"1040\",\n" +
            "                \"1050\",\n" +
            "                \"1060\",\n" +
            "                \"1070\",\n" +
            "                \"1080\",\n" +
            "                \"1090\",\n" +
            "                \"1100\",\n" +
            "                \"1110\",\n" +
            "                \"1130\",\n" +
            "                \"1140\",\n" +
            "                \"1150\",\n" +
            "                \"1160\",\n" +
            "                \"1170\",\n" +
            "                \"1180\",\n" +
            "                \"1200\",\n" +
            "                \"1210\",\n" +
            "                \"1230\",\n" +
            "                \"1240\",\n" +
            "                \"1250\",\n" +
            "                \"1260\",\n" +
            "                \"1270\",\n" +
            "                \"1280\",\n" +
            "                \"1290\",\n" +
            "                \"1300\",\n" +
            "                \"1310\",\n" +
            "                \"1320\",\n" +
            "                \"1330\",\n" +
            "                \"1340\",\n" +
            "                \"1350\",\n" +
            "                \"1360\",\n" +
            "                \"1370\",\n" +
            "                \"1380\",\n" +
            "                \"1390\",\n" +
            "                \"1400\",\n" +
            "                \"1410\",\n" +
            "                \"1420\",\n" +
            "                \"1430\",\n" +
            "                \"1440\",\n" +
            "                \"1450\",\n" +
            "                \"1460\",\n" +
            "                \"1480\",\n" +
            "                \"1490\",\n" +
            "                \"1500\",\n" +
            "                \"1510\",\n" +
            "                \"1520\",\n" +
            "                \"1530\",\n" +
            "                \"1540\",\n" +
            "                \"1550\",\n" +
            "                \"1560\",\n" +
            "                \"1570\",\n" +
            "                \"1580\",\n" +
            "                \"1590\",\n" +
            "                \"1600\",\n" +
            "                \"1610\",\n" +
            "                \"1630\",\n" +
            "                \"1640\",\n" +
            "                \"1650\",\n" +
            "                \"1660\",\n" +
            "                \"1670\",\n" +
            "                \"1680\",\n" +
            "                \"1690\",\n" +
            "                \"1700\",\n" +
            "                \"1710\",\n" +
            "                \"1730\",\n" +
            "                \"1750\",\n" +
            "                \"1760\",\n" +
            "                \"1770\",\n" +
            "                \"1790\",\n" +
            "                \"1800\",\n" +
            "                \"1820\",\n" +
            "                \"1830\",\n" +
            "                \"1840\",\n" +
            "                \"1850\",\n" +
            "                \"1860\",\n" +
            "                \"1870\",\n" +
            "                \"1880\",\n" +
            "                \"1890\",\n" +
            "                \"1900\",\n" +
            "                \"1910\",\n" +
            "                \"1920\",\n" +
            "                \"1930\",\n" +
            "                \"1940\",\n" +
            "                \"1950\",\n" +
            "                \"1960\",\n" +
            "                \"1970\",\n" +
            "                \"1980\",\n" +
            "                \"1990\",\n" +
            "                \"2000\",\n" +
            "                \"2010\",\n" +
            "                \"2020\",\n" +
            "                \"2030\",\n" +
            "                \"2040\",\n" +
            "                \"2050\",\n" +
            "                \"2060\",\n" +
            "                \"2080\",\n" +
            "                \"2090\",\n" +
            "                \"2100\",\n" +
            "                \"2110\",\n" +
            "                \"2120\",\n" +
            "                \"2130\",\n" +
            "                \"2140\",\n" +
            "                \"2150\",\n" +
            "                \"2160\",\n" +
            "                \"2170\",\n" +
            "                \"2180\",\n" +
            "                \"2190\",\n" +
            "                \"2200\",\n" +
            "                \"2210\",\n" +
            "                \"2220\",\n" +
            "                \"2230\",\n" +
            "                \"2240\",\n" +
            "                \"2250\",\n" +
            "                \"2260\",\n" +
            "                \"2270\",\n" +
            "                \"2280\",\n" +
            "                \"2290\",\n" +
            "                \"2300\",\n" +
            "                \"2310\",\n" +
            "                \"2320\",\n" +
            "                \"2330\",\n" +
            "                \"2340\",\n" +
            "                \"2350\",\n" +
            "                \"2360\",\n" +
            "                \"2370\",\n" +
            "                \"2380\",\n" +
            "                \"2390\",\n" +
            "                \"2410\",\n" +
            "                \"2420\",\n" +
            "                \"2430\",\n" +
            "                \"2450\",\n" +
            "                \"2460\",\n" +
            "                \"2470\",\n" +
            "                \"2480\",\n" +
            "                \"2500\",\n" +
            "                \"2510\",\n" +
            "                \"2520\",\n" +
            "                \"2550\",\n" +
            "                \"2560\",\n" +
            "                \"2570\",\n" +
            "                \"2580\",\n" +
            "                \"2590\",\n" +
            "                \"2600\",\n" +
            "                \"2610\",\n" +
            "                \"2620\",\n" +
            "                \"2630\",\n" +
            "                \"2640\",\n" +
            "                \"2650\",\n" +
            "                \"2670\",\n" +
            "                \"2680\",\n" +
            "                \"2690\",\n" +
            "                \"2700\",\n" +
            "                \"2710\",\n" +
            "                \"2720\",\n" +
            "                \"2730\",\n" +
            "                \"2740\",\n" +
            "                \"2750\",\n" +
            "                \"2760\",\n" +
            "                \"2780\",\n" +
            "                \"2790\",\n" +
            "                \"2810\",\n" +
            "                \"2830\",\n" +
            "                \"2840\",\n" +
            "                \"2860\",\n" +
            "                \"2870\",\n" +
            "                \"2880\",\n" +
            "                \"2890\",\n" +
            "                \"2900\",\n" +
            "                \"2910\",\n" +
            "                \"2920\",\n" +
            "                \"2930\",\n" +
            "                \"2940\",\n" +
            "                \"2950\",\n" +
            "                \"2960\",\n" +
            "                \"2970\",\n" +
            "                \"2980\",\n" +
            "                \"2990\",\n" +
            "                \"3000\",\n" +
            "                \"3010\",\n" +
            "                \"3020\",\n" +
            "                \"3030\",\n" +
            "                \"3040\",\n" +
            "                \"3050\",\n" +
            "                \"3060\",\n" +
            "                \"3070\",\n" +
            "                \"3080\",\n" +
            "                \"3090\",\n" +
            "                \"3100\",\n" +
            "                \"3110\",\n" +
            "                \"3130\",\n" +
            "                \"3140\",\n" +
            "                \"3160\",\n" +
            "                \"3170\",\n" +
            "                \"3180\",\n" +
            "                \"3190\",\n" +
            "                \"3200\",\n" +
            "                \"3210\",\n" +
            "                \"3220\",\n" +
            "                \"3230\",\n" +
            "                \"3240\",\n" +
            "                \"3250\",\n" +
            "                \"3260\",\n" +
            "                \"3270\",\n" +
            "                \"3280\",\n" +
            "                \"3290\",\n" +
            "                \"3300\",\n" +
            "                \"3310\",\n" +
            "                \"3320\",\n" +
            "                \"3330\",\n" +
            "                \"3340\",\n" +
            "                \"3350\",\n" +
            "                \"3360\",\n" +
            "                \"3370\",\n" +
            "                \"3380\",\n" +
            "                \"3390\",\n" +
            "                \"3400\",\n" +
            "                \"3420\",\n" +
            "                \"3440\",\n" +
            "                \"3470\",\n" +
            "                \"3480\",\n" +
            "                \"3490\",\n" +
            "                \"3510\",\n" +
            "                \"3520\",\n" +
            "                \"3530\",\n" +
            "                \"3540\",\n" +
            "                \"3550\",\n" +
            "                \"3560\",\n" +
            "                \"3570\",\n" +
            "                \"3580\",\n" +
            "                \"3590\",\n" +
            "                \"3600\",\n" +
            "                \"3610\",\n" +
            "                \"3620\",\n" +
            "                \"3630\",\n" +
            "                \"3640\",\n" +
            "                \"3660\",\n" +
            "                \"3670\",\n" +
            "                \"3680\",\n" +
            "                \"3690\",\n" +
            "                \"3700\",\n" +
            "                \"3710\",\n" +
            "                \"3720\",\n" +
            "                \"3730\",\n" +
            "                \"3740\",\n" +
            "                \"3750\",\n" +
            "                \"3760\",\n" +
            "                \"3770\",\n" +
            "                \"3780\",\n" +
            "                \"3790\",\n" +
            "                \"3800\",\n" +
            "                \"3810\",\n" +
            "                \"3820\",\n" +
            "                \"3830\",\n" +
            "                \"3840\",\n" +
            "                \"3860\",\n" +
            "                \"3870\",\n" +
            "                \"3880\",\n" +
            "                \"3890\",\n" +
            "                \"3900\",\n" +
            "                \"3910\",\n" +
            "                \"3920\",\n" +
            "                \"3930\",\n" +
            "                \"3950\",\n" +
            "                \"3960\",\n" +
            "                \"3970\",\n" +
            "                \"3980\",\n" +
            "                \"3990\",\n" +
            "                \"4000\",\n" +
            "                \"4010\",\n" +
            "                \"4020\",\n" +
            "                \"4040\",\n" +
            "                \"4050\",\n" +
            "                \"4060\",\n" +
            "                \"4070\",\n" +
            "                \"4090\",\n" +
            "                \"4100\",\n" +
            "                \"4110\",\n" +
            "                \"4120\",\n" +
            "                \"4130\",\n" +
            "                \"4140\",\n" +
            "                \"4150\",\n" +
            "                \"4160\",\n" +
            "                \"4170\",\n" +
            "                \"4180\",\n" +
            "                \"4190\",\n" +
            "                \"4200\",\n" +
            "                \"4220\",\n" +
            "                \"4230\",\n" +
            "                \"4240\",\n" +
            "                \"4250\",\n" +
            "                \"4260\",\n" +
            "                \"4280\",\n" +
            "                \"4300\",\n" +
            "                \"4320\",\n" +
            "                \"4330\",\n" +
            "                \"4340\",\n" +
            "                \"4350\",\n" +
            "                \"4360\",\n" +
            "                \"4370\",\n" +
            "                \"4380\",\n" +
            "                \"4390\",\n" +
            "                \"4400\",\n" +
            "                \"4430\",\n" +
            "                \"4440\",\n" +
            "                \"4450\",\n" +
            "                \"4460\",\n" +
            "                \"4470\",\n" +
            "                \"4480\",\n" +
            "                \"4500\",\n" +
            "                \"4510\",\n" +
            "                \"4520\",\n" +
            "                \"4530\",\n" +
            "                \"4540\",\n" +
            "                \"4550\",\n" +
            "                \"4560\",\n" +
            "                \"4570\",\n" +
            "                \"4580\",\n" +
            "                \"4590\",\n" +
            "                \"4600\",\n" +
            "                \"4610\",\n" +
            "                \"4620\",\n" +
            "                \"4630\",\n" +
            "                \"4640\",\n" +
            "                \"4650\",\n" +
            "                \"4660\",\n" +
            "                \"4670\",\n" +
            "                \"4680\",\n" +
            "                \"4690\",\n" +
            "                \"4700\",\n" +
            "                \"4710\",\n" +
            "                \"4720\",\n" +
            "                \"4740\",\n" +
            "                \"4750\",\n" +
            "                \"4760\",\n" +
            "                \"4770\",\n" +
            "                \"4780\",\n" +
            "                \"4790\",\n" +
            "                \"4800\",\n" +
            "                \"4810\",\n" +
            "                \"4820\",\n" +
            "                \"4830\",\n" +
            "                \"4840\",\n" +
            "                \"4850\",\n" +
            "                \"4870\",\n" +
            "                \"4880\",\n" +
            "                \"4890\",\n" +
            "                \"4900\",\n" +
            "                \"4920\",\n" +
            "                \"4930\",\n" +
            "                \"4950\",\n" +
            "                \"4960\",\n" +
            "                \"4970\",\n" +
            "                \"4980\",\n" +
            "                \"4990\",\n" +
            "                \"5000\",\n" +
            "                \"5010\",\n" +
            "                \"5020\",\n" +
            "                \"5030\",\n" +
            "                \"5040\",\n" +
            "                \"5050\",\n" +
            "                \"5060\",\n" +
            "                \"5070\",\n" +
            "                \"5090\",\n" +
            "                \"5110\",\n" +
            "                \"5120\",\n" +
            "                \"5130\",\n" +
            "                \"5140\",\n" +
            "                \"5150\",\n" +
            "                \"5160\",\n" +
            "                \"5170\",\n" +
            "                \"5180\",\n" +
            "                \"5190\",\n" +
            "                \"5200\",\n" +
            "                \"5210\",\n" +
            "                \"5220\",\n" +
            "                \"5230\",\n" +
            "                \"5240\",\n" +
            "                \"5260\",\n" +
            "                \"5270\",\n" +
            "                \"5280\",\n" +
            "                \"5300\",\n" +
            "                \"5310\",\n" +
            "                \"5320\",\n" +
            "                \"5340\",\n" +
            "                \"5350\",\n" +
            "                \"5360\",\n" +
            "                \"5370\",\n" +
            "                \"5380\",\n" +
            "                \"5390\",\n" +
            "                \"5420\",\n" +
            "                \"5430\",\n" +
            "                \"5440\",\n" +
            "                \"5450\",\n" +
            "                \"5460\",\n" +
            "                \"5470\",\n" +
            "                \"5480\",\n" +
            "                \"5490\",\n" +
            "                \"5500\",\n" +
            "                \"5510\",\n" +
            "                \"5520\",\n" +
            "                \"5530\",\n" +
            "                \"5540\",\n" +
            "                \"5550\",\n" +
            "                \"5560\",\n" +
            "                \"5570\",\n" +
            "                \"5580\",\n" +
            "                \"5600\",\n" +
            "                \"5610\",\n" +
            "                \"5620\",\n" +
            "                \"5630\",\n" +
            "                \"5640\",\n" +
            "                \"5660\",\n" +
            "                \"5670\",\n" +
            "                \"5680\",\n" +
            "                \"5700\",\n" +
            "                \"5710\",\n" +
            "                \"5720\",\n" +
            "                \"5730\",\n" +
            "                \"5740\",\n" +
            "                \"5750\",\n" +
            "                \"5760\",\n" +
            "                \"5770\",\n" +
            "                \"5780\",\n" +
            "                \"5790\",\n" +
            "                \"5800\",\n" +
            "                \"5810\",\n" +
            "                \"5820\",\n" +
            "                \"5830\",\n" +
            "                \"5840\",\n" +
            "                \"5860\",\n" +
            "                \"5870\",\n" +
            "                \"5880\",\n" +
            "                \"5890\",\n" +
            "                \"5900\",\n" +
            "                \"5910\",\n" +
            "                \"5920\",\n" +
            "                \"5930\",\n" +
            "                \"5940\",\n" +
            "                \"5960\",\n" +
            "                \"5970\",\n" +
            "                \"5980\",\n" +
            "                \"5990\",\n" +
            "                \"6000\",\n" +
            "                \"6010\",\n" +
            "                \"6020\",\n" +
            "                \"6030\",\n" +
            "                \"6040\",\n" +
            "                \"6050\",\n" +
            "                \"6060\",\n" +
            "                \"6070\",\n" +
            "                \"6080\",\n" +
            "                \"6090\",\n" +
            "                \"6100\",\n" +
            "                \"6110\",\n" +
            "                \"6120\",\n" +
            "                \"6130\",\n" +
            "                \"6140\",\n" +
            "                \"6150\",\n" +
            "                \"6160\",\n" +
            "                \"6170\",\n" +
            "                \"6180\",\n" +
            "                \"6190\",\n" +
            "                \"6200\",\n" +
            "                \"6220\",\n" +
            "                \"6230\",\n" +
            "                \"6240\",\n" +
            "                \"6250\",\n" +
            "                \"6260\",\n" +
            "                \"6270\",\n" +
            "                \"6280\",\n" +
            "                \"6290\",\n" +
            "                \"6300\",\n" +
            "                \"6310\",\n" +
            "                \"6320\",\n" +
            "                \"6330\",\n" +
            "                \"6340\",\n" +
            "                \"6350\",\n" +
            "                \"6370\",\n" +
            "                \"6380\",\n" +
            "                \"6410\",\n" +
            "                \"6420\",\n" +
            "                \"6430\",\n" +
            "                \"6440\",\n" +
            "                \"6450\",\n" +
            "                \"6460\",\n" +
            "                \"6470\",\n" +
            "                \"6480\",\n" +
            "                \"6490\",\n" +
            "                \"6500\",\n" +
            "                \"6530\",\n" +
            "                \"6540\",\n" +
            "                \"6550\",\n" +
            "                \"6560\",\n" +
            "                \"6570\",\n" +
            "                \"6580\",\n" +
            "                \"6590\",\n" +
            "                \"6600\",\n" +
            "                \"6610\",\n" +
            "                \"6620\",\n" +
            "                \"6630\",\n" +
            "                \"6640\",\n" +
            "                \"6660\",\n" +
            "                \"6670\",\n" +
            "                \"6680\",\n" +
            "                \"6690\",\n" +
            "                \"6700\",\n" +
            "                \"6710\",\n" +
            "                \"6720\",\n" +
            "                \"6730\",\n" +
            "                \"6740\",\n" +
            "                \"6750\",\n" +
            "                \"6770\",\n" +
            "                \"6780\",\n" +
            "                \"6790\",\n" +
            "                \"6800\",\n" +
            "                \"6810\",\n" +
            "                \"6820\",\n" +
            "                \"6830\",\n" +
            "                \"6840\",\n" +
            "                \"6850\",\n" +
            "                \"6860\",\n" +
            "                \"6870\",\n" +
            "                \"6880\",\n" +
            "                \"6890\",\n" +
            "                \"6900\",\n" +
            "                \"6920\",\n" +
            "                \"6930\",\n" +
            "                \"6940\",\n" +
            "                \"6960\",\n" +
            "                \"6970\",\n" +
            "                \"6990\",\n" +
            "                \"7010\",\n" +
            "                \"7020\",\n" +
            "                \"7030\",\n" +
            "                \"7050\",\n" +
            "                \"7060\",\n" +
            "                \"7070\",\n" +
            "                \"7090\",\n" +
            "                \"7100\",\n" +
            "                \"7120\",\n" +
            "                \"7130\",\n" +
            "                \"7140\",\n" +
            "                \"7150\",\n" +
            "                \"7160\",\n" +
            "                \"7170\",\n" +
            "                \"7180\",\n" +
            "                \"7190\",\n" +
            "                \"7200\",\n" +
            "                \"7210\",\n" +
            "                \"7220\",\n" +
            "                \"7250\",\n" +
            "                \"7260\",\n" +
            "                \"7270\",\n" +
            "                \"7280\",\n" +
            "                \"7300\",\n" +
            "                \"7310\",\n" +
            "                \"7320\",\n" +
            "                \"7330\",\n" +
            "                \"7340\",\n" +
            "                \"7350\",\n" +
            "                \"7370\",\n" +
            "                \"7380\",\n" +
            "                \"7390\",\n" +
            "                \"7400\",\n" +
            "                \"7410\",\n" +
            "                \"7420\",\n" +
            "                \"7430\",\n" +
            "                \"7440\",\n" +
            "                \"7450\",\n" +
            "                \"7460\",\n" +
            "                \"7480\",\n" +
            "                \"7500\",\n" +
            "                \"7510\",\n" +
            "                \"7520\",\n" +
            "                \"7530\",\n" +
            "                \"7550\",\n" +
            "                \"7560\",\n" +
            "                \"7570\",\n" +
            "                \"7590\",\n" +
            "                \"7600\",\n" +
            "                \"7620\",\n" +
            "                \"7630\",\n" +
            "                \"7640\",\n" +
            "                \"7660\",\n" +
            "                \"7670\",\n" +
            "                \"7680\",\n" +
            "                \"7690\",\n" +
            "                \"7710\",\n" +
            "                \"7720\",\n" +
            "                \"7730\",\n" +
            "                \"7740\",\n" +
            "                \"7750\",\n" +
            "                \"7770\",\n" +
            "                \"7780\",\n" +
            "                \"7790\",\n" +
            "                \"7800\",\n" +
            "                \"7810\",\n" +
            "                \"7820\",\n" +
            "                \"7830\",\n" +
            "                \"7860\",\n" +
            "                \"7870\",\n" +
            "                \"7880\",\n" +
            "                \"7890\",\n" +
            "                \"7900\",\n" +
            "                \"7910\",\n" +
            "                \"7920\",\n" +
            "                \"7930\",\n" +
            "                \"7950\",\n" +
            "                \"7960\",\n" +
            "                \"7970\",\n" +
            "                \"7980\",\n" +
            "                \"7990\",\n" +
            "                \"8000\",\n" +
            "                \"8010\",\n" +
            "                \"8030\",\n" +
            "                \"8040\",\n" +
            "                \"8050\",\n" +
            "                \"8060\",\n" +
            "                \"8070\",\n" +
            "                \"8080\",\n" +
            "                \"8090\",\n" +
            "                \"8110\",\n" +
            "                \"8120\",\n" +
            "                \"8130\",\n" +
            "                \"8140\",\n" +
            "                \"8150\",\n" +
            "                \"8160\",\n" +
            "                \"8170\",\n" +
            "                \"8180\",\n" +
            "                \"8190\",\n" +
            "                \"8210\",\n" +
            "                \"8220\",\n" +
            "                \"8230\",\n" +
            "                \"8240\",\n" +
            "                \"8250\",\n" +
            "                \"8260\",\n" +
            "                \"8270\",\n" +
            "                \"8280\",\n" +
            "                \"8290\",\n" +
            "                \"8300\",\n" +
            "                \"8310\",\n" +
            "                \"8320\",\n" +
            "                \"8330\",\n" +
            "                \"8340\",\n" +
            "                \"8350\",\n" +
            "                \"8370\",\n" +
            "                \"8380\",\n" +
            "                \"8390\",\n" +
            "                \"8400\",\n" +
            "                \"8410\",\n" +
            "                \"8420\",\n" +
            "                \"8440\",\n" +
            "                \"8460\",\n" +
            "                \"8470\",\n" +
            "                \"8480\",\n" +
            "                \"8490\",\n" +
            "                \"8500\",\n" +
            "                \"8510\",\n" +
            "                \"8520\",\n" +
            "                \"8530\",\n" +
            "                \"8550\",\n" +
            "                \"8560\",\n" +
            "                \"8570\",\n" +
            "                \"8590\",\n" +
            "                \"8600\",\n" +
            "                \"8610\",\n" +
            "                \"8630\",\n" +
            "                \"8640\",\n" +
            "                \"8650\",\n" +
            "                \"8660\",\n" +
            "                \"8670\",\n" +
            "                \"8680\",\n" +
            "                \"8690\",\n" +
            "                \"8700\",\n" +
            "                \"8710\",\n" +
            "                \"8720\",\n" +
            "                \"8730\",\n" +
            "                \"8740\",\n" +
            "                \"8750\",\n" +
            "                \"8770\",\n" +
            "                \"8790\",\n" +
            "                \"8800\",\n" +
            "                \"8810\",\n" +
            "                \"8820\",\n" +
            "                \"8830\",\n" +
            "                \"8840\",\n" +
            "                \"8850\",\n" +
            "                \"8870\",\n" +
            "                \"8880\",\n" +
            "                \"8890\",\n" +
            "                \"8900\",\n" +
            "                \"8910\",\n" +
            "                \"8920\",\n" +
            "                \"8930\",\n" +
            "                \"8940\",\n" +
            "                \"8950\",\n" +
            "                \"8970\",\n" +
            "                \"8980\",\n" +
            "                \"9000\",\n" +
            "                \"9010\",\n" +
            "                \"9020\",\n" +
            "                \"9030\",\n" +
            "                \"9040\",\n" +
            "                \"9050\",\n" +
            "                \"9060\",\n" +
            "                \"9070\",\n" +
            "                \"9080\",\n" +
            "                \"9090\",\n" +
            "                \"9100\",\n" +
            "                \"9110\",\n" +
            "                \"9120\",\n" +
            "                \"9130\",\n" +
            "                \"9140\",\n" +
            "                \"9150\",\n" +
            "                \"9170\",\n" +
            "                \"9180\",\n" +
            "                \"9190\",\n" +
            "                \"9200\",\n" +
            "                \"9210\",\n" +
            "                \"9230\",\n" +
            "                \"9240\",\n" +
            "                \"9250\",\n" +
            "                \"9260\",\n" +
            "                \"9270\",\n" +
            "                \"9280\",\n" +
            "                \"9290\",\n" +
            "                \"9300\",\n" +
            "                \"9310\",\n" +
            "                \"9320\",\n" +
            "                \"9330\",\n" +
            "                \"9340\",\n" +
            "                \"9360\",\n" +
            "                \"9380\",\n" +
            "                \"9390\",\n" +
            "                \"9400\",\n" +
            "                \"9410\",\n" +
            "                \"9420\",\n" +
            "                \"9430\",\n" +
            "                \"9440\",\n" +
            "                \"9450\",\n" +
            "                \"9460\",\n" +
            "                \"9470\",\n" +
            "                \"9480\",\n" +
            "                \"9490\",\n" +
            "                \"9500\",\n" +
            "                \"9510\",\n" +
            "                \"9520\",\n" +
            "                \"9530\",\n" +
            "                \"9540\",\n" +
            "                \"9550\",\n" +
            "                \"9560\",\n" +
            "                \"9570\",\n" +
            "                \"9580\",\n" +
            "                \"9590\",\n" +
            "                \"9600\",\n" +
            "                \"9610\",\n" +
            "                \"9620\",\n" +
            "                \"9630\",\n" +
            "                \"9650\",\n" +
            "                \"9660\",\n" +
            "                \"9670\",\n" +
            "                \"9680\",\n" +
            "                \"9700\",\n" +
            "                \"9710\",\n" +
            "                \"9730\",\n" +
            "                \"9740\",\n" +
            "                \"9750\",\n" +
            "                \"9760\",\n" +
            "                \"9770\",\n" +
            "                \"9780\",\n" +
            "                \"9790\",\n" +
            "                \"9800\",\n" +
            "                \"9810\",\n" +
            "                \"9820\",\n" +
            "                \"9830\",\n" +
            "                \"9840\",\n" +
            "                \"9850\",\n" +
            "                \"9860\",\n" +
            "                \"9870\",\n" +
            "                \"9880\",\n" +
            "                \"9890\",\n" +
            "                \"9900\",\n" +
            "                \"9910\",\n" +
            "                \"9920\",\n" +
            "                \"9940\",\n" +
            "                \"9950\",\n" +
            "                \"9960\",\n" +
            "                \"9970\",\n" +
            "                \"9980\",\n" +
            "                \"9990\",\n" +
            "                \"10000\",\n" +
            "                \"10010\",\n" +
            "                \"10020\",\n" +
            "                \"10030\",\n" +
            "                \"10040\",\n" +
            "                \"10060\",\n" +
            "                \"10070\",\n" +
            "                \"10080\",\n" +
            "                \"10090\",\n" +
            "                \"10100\",\n" +
            "                \"10110\",\n" +
            "                \"10120\",\n" +
            "                \"10130\",\n" +
            "                \"10140\",\n" +
            "                \"10150\",\n" +
            "                \"10160\",\n" +
            "                \"10170\",\n" +
            "                \"10180\",\n" +
            "                \"10190\",\n" +
            "                \"10200\",\n" +
            "                \"10210\",\n" +
            "                \"10220\",\n" +
            "                \"10230\",\n" +
            "                \"10240\",\n" +
            "                \"10260\",\n" +
            "                \"10270\",\n" +
            "                \"10280\",\n" +
            "                \"10290\",\n" +
            "                \"10300\",\n" +
            "                \"10310\",\n" +
            "                \"10320\",\n" +
            "                \"10330\",\n" +
            "                \"10340\",\n" +
            "                \"10350\",\n" +
            "                \"10360\",\n" +
            "                \"10370\",\n" +
            "                \"10390\",\n" +
            "                \"10400\",\n" +
            "                \"10410\",\n" +
            "                \"10420\",\n" +
            "                \"10440\",\n" +
            "                \"10450\",\n" +
            "                \"10460\",\n" +
            "                \"10470\",\n" +
            "                \"10480\",\n" +
            "                \"10490\",\n" +
            "                \"10500\",\n" +
            "                \"10510\",\n" +
            "                \"10520\",\n" +
            "                \"10530\",\n" +
            "                \"10540\",\n" +
            "                \"10550\",\n" +
            "                \"10560\",\n" +
            "                \"10580\",\n" +
            "                \"10590\",\n" +
            "                \"10610\",\n" +
            "                \"10620\",\n" +
            "                \"10630\",\n" +
            "                \"10640\",\n" +
            "                \"10650\",\n" +
            "                \"10670\",\n" +
            "                \"10680\",\n" +
            "                \"10690\",\n" +
            "                \"10700\",\n" +
            "                \"10710\",\n" +
            "                \"10720\",\n" +
            "                \"10730\",\n" +
            "                \"10740\",\n" +
            "                \"10750\",\n" +
            "                \"10770\",\n" +
            "                \"10780\",\n" +
            "                \"10790\",\n" +
            "                \"10820\",\n" +
            "                \"10840\",\n" +
            "                \"10850\",\n" +
            "                \"10860\",\n" +
            "                \"10870\",\n" +
            "                \"10880\",\n" +
            "                \"10890\",\n" +
            "                \"10900\",\n" +
            "                \"10910\",\n" +
            "                \"10920\",\n" +
            "                \"10940\",\n" +
            "                \"10950\",\n" +
            "                \"10960\",\n" +
            "                \"10970\",\n" +
            "                \"10980\",\n" +
            "                \"10990\",\n" +
            "                \"11000\",\n" +
            "                \"11010\",\n" +
            "                \"11020\",\n" +
            "                \"11030\",\n" +
            "                \"11040\",\n" +
            "                \"11050\",\n" +
            "                \"11060\",\n" +
            "                \"11070\",\n" +
            "                \"11080\",\n" +
            "                \"11090\",\n" +
            "                \"11100\",\n" +
            "                \"11110\",\n" +
            "                \"11120\",\n" +
            "                \"11130\",\n" +
            "                \"11140\",\n" +
            "                \"11150\"\n" +
            "            ],\n" +
            "            \"float_data\": [],\n" +
            "            \"double_data\": [],\n" +
            "            \"bool_data\": [],\n" +
            "            \"string_data\": [],\n" +
            "            \"data_validity\": [],\n" +
            "            \"ref_num\": 0\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    @BeforeEach
    public void setUp() {
        Domaindatasource.QueryDomainDataSourceResponse response = Domaindatasource.QueryDomainDataSourceResponse.newBuilder()
                .setData(Domaindatasource.DomainDataSource.newBuilder()
                        .setType(StringUtils.toRootLowerCase(DataSourceTypeEnum.OSS.name()))
                        .setDatasourceId("datasource")
                        .build())
                .build();
        Mockito.when(kusciaGrpcClientAdapter.queryDomainDataSource(any(Domaindatasource.QueryDomainDataSourceRequest.class))).thenReturn(response);
    }

    private List<NodeDO> buildNodeDOList() {
        List<NodeDO> nodeDOList = new ArrayList<>();
        nodeDOList.add(NodeDO.builder().nodeId("alice").name("alice").description("alice").instId("alice").auth("alice").type("mpc").build());
        return nodeDOList;
    }


    private List<ProjectResultDO> buildProjectResultDOList() {
        List<ProjectResultDO> projectResultDOList = new ArrayList<>();
        ProjectResultDO.UPK upk = new ProjectResultDO.UPK();
        upk.setKind(ResultKind.FedTable);
        upk.setNodeId("alice");
        upk.setRefId("alice-ref1");
        upk.setProjectId(PROJECT_ID);
        ProjectResultDO projectResultDO = ProjectResultDO.builder().upk(upk).taskId("task-dabgvasfasdasdas").jobId("op-psiv3-dabgvasfasdasdas").build();
        projectResultDO.setGmtCreate(DateTimes.utcFromRfc3339("2023-08-02T08:30:15.235+08:00"));
        projectResultDO.setGmtModified(DateTimes.utcFromRfc3339("2023-08-02T16:30:15.235+08:00"));
        projectResultDOList.add(projectResultDO);
        return projectResultDOList;
    }

    private List<ProjectResultDO> buildProjectResultDOList2() {
        List<ProjectResultDO> projectResultDOList = new ArrayList<>();
        ProjectResultDO.UPK upk = new ProjectResultDO.UPK();
        upk.setKind(ResultKind.Report);
        upk.setNodeId("alice");
        upk.setRefId("alice-ref1");
        upk.setProjectId(PROJECT_ID);
        ProjectResultDO projectResultDO = ProjectResultDO.builder().upk(upk).taskId("task-dabgvasfasdasdas").jobId("op-psiv3-dabgvasfasdasdas").build();
        projectResultDO.setGmtCreate(DateTimes.utcFromRfc3339("2023-08-02T08:30:15.235+08:00"));
        projectResultDO.setGmtModified(DateTimes.utcFromRfc3339("2023-08-02T16:30:15.235+08:00"));
        projectResultDOList.add(projectResultDO);
        return projectResultDOList;
    }

    private ProjectJobDO buildProjectJobDO(boolean isTaskEmpty) {
        ProjectJobDO.UPK upk = new ProjectJobDO.UPK();
        upk.setProjectId(PROJECT_ID);
        upk.setJobId("op-psiv3-dabgvasfasdasdas");
        ProjectJobDO projectJobDO = ProjectJobDO.builder().upk(upk).graphId(GRAPH_ID).edges(Collections.emptyList()).build();
        Map<String, ProjectTaskDO> projectTaskDOMap = new HashMap<>();
        ProjectTaskDO.UPK taskUpk = new ProjectTaskDO.UPK();
        if (!isTaskEmpty) {
            taskUpk.setTaskId("task-dabgvasfasdasdas");
            projectTaskDOMap.put("task-dabgvasfasdasdas", ProjectTaskDO.builder().upk(taskUpk).graphNode(buildProjectGraphNodeDO()).extraInfo(new ProjectTaskDO.ExtraInfo()).build());
        } else {
            taskUpk.setTaskId("task-dabgvasfasdasdasssss");
            projectTaskDOMap.put("task-dabgvasfasdasdasssss", ProjectTaskDO.builder().upk(taskUpk).graphNode(buildProjectGraphNodeDO()).extraInfo(new ProjectTaskDO.ExtraInfo()).build());
        }
        projectJobDO.setTasks(projectTaskDOMap);
        projectJobDO.setGmtCreate(DateTimes.utcFromRfc3339("2023-08-02T08:30:15.235+08:00"));
        projectJobDO.setGmtModified(DateTimes.utcFromRfc3339("2023-08-02T16:30:15.235+08:00"));
        return projectJobDO;
    }


    private ProjectGraphNodeDO buildProjectGraphNodeDO() {
        ProjectGraphNodeDO.UPK upk = new ProjectGraphNodeDO.UPK();
        upk.setGraphNodeId("alice");
        return ProjectGraphNodeDO.builder().upk(upk).build();
    }

    private ProjectGraphDO buildProjectGraphDO() {
        ProjectGraphDO.UPK upk = new ProjectGraphDO.UPK();
        upk.setProjectId(PROJECT_ID);
        return ProjectGraphDO.builder().upk(upk).build();
    }

    private ProjectDatatableDO buildProjectDatatableDO() {
        ProjectDatatableDO.UPK upk = new ProjectDatatableDO.UPK();
        upk.setDatatableId("alice-ref1");
        upk.setNodeId("alice");
        upk.setProjectId(PROJECT_ID);
        List<ProjectDatatableDO.TableColumnConfig> tableConfig = new ArrayList<>();
        ProjectDatatableDO.TableColumnConfig config = new ProjectDatatableDO.TableColumnConfig();
        config.setColType("id1");
        config.setColType("string");
        tableConfig.add(config);
        return ProjectDatatableDO.builder().upk(upk).tableConfig(tableConfig).build();
    }

    private Domaindata.ListDomainDataResponse buildListDomainDataResponse(Integer code) {
        Common.Status status = Common.Status.newBuilder().setCode(code).build();
        return Domaindata.ListDomainDataResponse.newBuilder().setStatus(status).build();
    }

    private DomainOuterClass.QueryDomainResponse buildQueryDomainResponse(Integer code) {
        return DomainOuterClass.QueryDomainResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private DomainOuterClass.BatchQueryDomainResponse buildBatchQueryDomainResponse(Integer code) {
        return DomainOuterClass.BatchQueryDomainResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private DomainOuterClass.CreateDomainResponse buildCreateDomainResponse(Integer code) {
        return DomainOuterClass.CreateDomainResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private DomainOuterClass.DeleteDomainResponse buildDeleteDomainResponse(Integer code) {
        return DomainOuterClass.DeleteDomainResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private Domaindata.BatchQueryDomainDataResponse buildBatchQueryDomainDataResponse(Integer code) {
        return Domaindata.BatchQueryDomainDataResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).setData(
                Domaindata.DomainDataList.newBuilder().addDomaindataList(Domaindata.DomainData.newBuilder().setDomainId("alice").
                        setDomaindataId("alice-ref1").setType("2").setRelativeUri("dmds://psi_125676513").setDatasourceId("datasourceId").build()).build()
        ).build();
    }

    private Domaindata.QueryDomainDataResponse buildQueryDomainDataResponse(Integer code) {
        return Domaindata.QueryDomainDataResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).setData(
                Domaindata.DomainData.newBuilder().setDomainId("alice").
                        setDomaindataId("alice-ref1").setType("2").setRelativeUri("dmds://psi_125676513").setDatasourceId("alice-datasource-ref1").build()
        ).build();
    }
    private Domaindata.QueryDomainDataResponse buildQueryDomainDataResponse2(Integer code) {
        return Domaindata.QueryDomainDataResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).setData(
                Domaindata.DomainData.newBuilder().setDomainId("bob").
                        setDomaindataId("bob-ref1").setType("22").setRelativeUri("dmds://psi_1256765132").setDatasourceId("alice-datasource-ref2").build()
        ).build();
    }
    private Domaindata.QueryDomainDataResponse buildQueryDomainDataResponse3(Integer code) {
        return Domaindata.QueryDomainDataResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).setData(
                Domaindata.DomainData.newBuilder().setDomainId("alice").
                        setDomaindataId("alice-ref1").setType("2").setRelativeUri("dmds://psi_125676513").setDatasourceId("alice-datasource-ref1").build()
        ).build();
    }

    private Domaindata.QueryDomainDataResponse buildEmptyQueryDomainDataResponse(Integer code) {
        return Domaindata.QueryDomainDataResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    @Test
    void listNode() throws Exception {
        assertResponse(() -> {
            InstDO instDO = new InstDO();
            instDO.setInstId("alice");
            instDO.setName("test_inst_name");
            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_LIST));
            ReflectionTestUtils.setField(nodeManager, "platformType", "AUTONOMY");
            DomainOuterClass.BatchQueryDomainResponse batchQueryDomainResponse = buildBatchQueryDomainResponse(0);
            Mockito.when(instRepository.findById("alice")).thenReturn(Optional.of(instDO));
            Mockito.when(kusciaGrpcClientAdapter.batchQueryDomain(any())).thenReturn(batchQueryDomainResponse);
            Mockito.when(nodeRepository.findAll()).thenReturn(buildNodeDOList());
            Domaindata.ListDomainDataResponse response = buildListDomainDataResponse(0);
            Mockito.when(kusciaGrpcClientAdapter.listDomainData(any(), Mockito.anyString())).thenReturn(response);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "listNode"));
        });
    }

    @Test
    void listNodeByQueryDatatableFailedException() throws Exception {
        assertResponseWithEmptyValue(() -> {
            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_LIST));

            DomainOuterClass.BatchQueryDomainResponse batchQueryDomainResponse = buildBatchQueryDomainResponse(0);
            Mockito.when(kusciaGrpcClientAdapter.batchQueryDomain(any())).thenReturn(batchQueryDomainResponse);
            Mockito.when(nodeRepository.findAll()).thenReturn(buildNodeDOList());
            Domaindata.ListDomainDataResponse response = buildListDomainDataResponse(1);
            Mockito.when(kusciaGrpcClientAdapter.listDomainData(any(), Mockito.anyString())).thenReturn(response);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "listNode"));


        },"nodeStatus");
    }

    @Test
    void createNode() throws Exception {
        assertResponse(() -> {
            CreateNodeRequest request = FakerUtils.fake(CreateNodeRequest.class);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_CREATE));

            DomainOuterClass.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(1);
            request.setMode(0);
            Mockito.when(kusciaGrpcClientAdapter.queryDomain(any())).thenReturn(queryDomainResponse);
            Mockito.when(nodeRepository.findByNodeId(any())).thenReturn(FakerUtils.fake(NodeDO.class));
            DomainOuterClass.CreateDomainResponse createDomainResponse = buildCreateDomainResponse(0);
            Mockito.when(kusciaGrpcClientAdapter.createDomain(any())).thenReturn(createDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "createNode", CreateNodeRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void createNodeByNodeAlreadyExistsException() throws Exception {
        assertErrorCode(() -> {
            CreateNodeRequest request = FakerUtils.fake(CreateNodeRequest.class);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_CREATE));
            DomainOuterClass.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
            request.setMode(0);
            Mockito.when(kusciaGrpcClientAdapter.queryDomain(any())).thenReturn(queryDomainResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "createNode", CreateNodeRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, SystemErrorCode.VALIDATION_ERROR);
    }

    @Test
    void createNodeByNodeCreateFailedException() throws Exception {
        assertResponse(() -> {
            CreateNodeRequest request = FakerUtils.fake(CreateNodeRequest.class);
            request.setMode(0);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_CREATE));
            DomainOuterClass.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(1);
            Mockito.when(kusciaGrpcClientAdapter.queryDomain(any())).thenReturn(queryDomainResponse);
            Mockito.when(nodeRepository.findByNodeId(any())).thenReturn(FakerUtils.fake(NodeDO.class));

            DomainOuterClass.CreateDomainResponse createDomainResponse = buildCreateDomainResponse(1);
            Mockito.when(kusciaGrpcClientAdapter.createDomain(any())).thenReturn(createDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "createNode", CreateNodeRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void deleteNode() throws Exception {
        assertResponseWithEmptyData(() -> {
            String nodeId = FakerUtils.fake(String.class);
            NodeIdRequest request = new NodeIdRequest(nodeId);
            request.setNodeId("alice");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_DELETE));
            DomainOuterClass.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
            Mockito.when(kusciaGrpcClientAdapter.queryDomain(any())).thenReturn(queryDomainResponse);

            DomainOuterClass.DeleteDomainResponse deleteDomainResponse = buildDeleteDomainResponse(0);
            Mockito.when(kusciaGrpcClientAdapter.deleteDomain(any())).thenReturn(deleteDomainResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "deleteNode", NodeIdRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void deleteNodeByNodeNotExistsException() throws Exception {
        assertResponseWithEmptyData(() -> {
            String nodeId = FakerUtils.fake(String.class);
            NodeIdRequest request = new NodeIdRequest(nodeId);
            request.setNodeId("alice");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_DELETE));
            DomainOuterClass.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(1);
            Mockito.when(kusciaGrpcClientAdapter.queryDomain(any())).thenReturn(queryDomainResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "deleteNode", NodeIdRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void deleteNodeByNodeDeleteFailedException() throws Exception {
        assertResponseWithEmptyData(() -> {
            String nodeId = FakerUtils.fake(String.class);
            NodeIdRequest request = new NodeIdRequest(nodeId);
            request.setNodeId("alice");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_DELETE));
            DomainOuterClass.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
            Mockito.when(kusciaGrpcClientAdapter.queryDomain(any())).thenReturn(queryDomainResponse);

            DomainOuterClass.DeleteDomainResponse deleteDomainResponse = buildDeleteDomainResponse(1);
            Mockito.when(kusciaGrpcClientAdapter.deleteDomain(any())).thenReturn(deleteDomainResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "deleteNode", NodeIdRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void listResults() throws Exception {
        assertResponse(() -> {
            ListNodeResultRequest request = FakerUtils.fake(ListNodeResultRequest.class);
            request.setKindFilters(Collections.singletonList("table"));
            request.setNameFilter("alice");
            request.setPageSize(20);
            request.setPageNumber(1);
            request.setOwnerId("alice");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_RESULT_LIST));

            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList();
            Mockito.when(resultRepository.findByNodeId(anyString())).thenReturn(projectResultDOS);

            Domaindata.BatchQueryDomainDataResponse batchQueryDomainDataResponse = buildBatchQueryDomainDataResponse(0);
            Mockito.when(kusciaGrpcClientAdapter.batchQueryDomainData(any())).thenReturn(batchQueryDomainDataResponse);



            NodeDO nodeDO = new NodeDO();
            nodeDO.setNodeId("alice");
            nodeDO.setName("name is alice");
            Mockito.when(nodeRepository.findByNodeId(Mockito.eq("alice"))).thenReturn(nodeDO);

            List<NodeDO> nodeDOList = List.of(nodeDO);
            Mockito.when(nodeRepository.findByType(Mockito.anyString())).thenReturn(nodeDOList);


            Mockito.when(kusciaGrpcClientAdapter.queryDomainDataSource(any())).thenReturn(buildQueryDomainDatasourceResponse(0));

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "listResults", ListNodeResultRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }
    private Domaindatasource.QueryDomainDataSourceResponse buildQueryDomainDatasourceResponse(Integer code) {
        return Domaindatasource.QueryDomainDataSourceResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).setData(
                Domaindatasource.DomainDataSource.newBuilder().setDomainId("domainId").setDatasourceId("datasourceId")
                        .setType("OSS").setName("name")
                        .setDatasourceId("datasourceId").build()
        ).build();
    }

    @Test
    void listResultsByDomainDataNotExistsException() throws Exception {
        assertErrorCode(() -> {
            ListNodeResultRequest request = FakerUtils.fake(ListNodeResultRequest.class);
            request.setKindFilters(Collections.singletonList("table"));
            request.setNameFilter("alice");
            request.setPageSize(20);
            request.setPageNumber(1);
            request.setOwnerId("alice");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_RESULT_LIST));

            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList();
            Mockito.when(resultRepository.findByNodeId(anyString())).thenReturn(projectResultDOS);

            Domaindata.BatchQueryDomainDataResponse batchQueryDomainDataResponse = buildBatchQueryDomainDataResponse(1);
            Mockito.when(kusciaGrpcClientAdapter.batchQueryDomainData(any())).thenReturn(batchQueryDomainDataResponse);


            NodeDO nodeDO = new NodeDO();
            nodeDO.setNodeId("alice");
            nodeDO.setName("name is alice");
            Mockito.when(nodeRepository.findByNodeId(Mockito.eq("alice"))).thenReturn(nodeDO);

            List<NodeDO> nodeDOList = List.of(nodeDO);
            Mockito.when(nodeRepository.findByType(Mockito.anyString())).thenReturn(nodeDOList);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "listResults", ListNodeResultRequest.class)).
                    content(JsonUtils.toJSONString(request));
        },DatatableErrorCode.QUERY_DATATABLE_FAILED);
    }

    @Test
    void listResultsByOutOfRangeException() throws Exception {
        assertErrorCode(() -> {
            ListNodeResultRequest request = FakerUtils.fake(ListNodeResultRequest.class);
            request.setKindFilters(Collections.singletonList("table"));
            request.setNameFilter("alice");
            request.setPageSize(20);
            request.setPageNumber(2);
            request.setOwnerId("alice");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_RESULT_LIST));

            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList();
            Mockito.when(resultRepository.findByNodeId(anyString())).thenReturn(projectResultDOS);

            Domaindata.BatchQueryDomainDataResponse batchQueryDomainDataResponse = buildBatchQueryDomainDataResponse(0);
            Mockito.when(kusciaGrpcClientAdapter.batchQueryDomainData(any())).thenReturn(batchQueryDomainDataResponse);

            NodeDO nodeDO = new NodeDO();
            nodeDO.setNodeId("alice");
            nodeDO.setName("name is alice");
            Mockito.when(nodeRepository.findByNodeId(Mockito.eq("alice"))).thenReturn(nodeDO);

            List<NodeDO> nodeDOList = List.of(nodeDO);
            Mockito.when(nodeRepository.findByType(Mockito.anyString())).thenReturn(nodeDOList);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "listResults", ListNodeResultRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, SystemErrorCode.UNKNOWN_ERROR);
    }

    @Test
    void getNodeResultDetail() throws Exception {
        assertResponse(() -> {
            GetNodeResultDetailRequest request = FakerUtils.fake(GetNodeResultDetailRequest.class);
            request.setNodeId("alice");
            request.setDomainDataId("alice-ref1");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_RESULT_DETAIL));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildQueryDomainDataResponse(0);
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(any())).thenReturn(queryDomainDataResponse);

            Mockito.when(projectRepository.findById(anyString())).thenReturn(Optional.of(ProjectDO.builder().projectId(PROJECT_ID).build()));
            Mockito.when(nodeRepository.findByNodeId(anyString())).thenReturn(buildNodeDOList().get(0));

            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList();
            Mockito.when(resultRepository.findByNodeIdAndRefId(anyString(), anyString())).thenReturn(Optional.of(projectResultDOS.get(0)));

            Mockito.when(projectJobRepository.findByJobId(anyString())).thenReturn(Optional.of(buildProjectJobDO(false)));
            Mockito.when(projectJobRepository.findById(any())).thenReturn(Optional.of(buildProjectJobDO(false)));

            Mockito.when(projectGraphRepository.findByGraphId(anyString(), anyString())).thenReturn(Optional.of(buildProjectGraphDO()));

            Mockito.when(resultRepository.findByProjectJobId(anyString(), anyString())).thenReturn(projectResultDOS);

            Mockito.when(datatableRepository.findById(any())).thenReturn(Optional.of(buildProjectDatatableDO()));

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "getNodeResultDetail", GetNodeResultDetailRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }
    @Test
    void getNodeResultDetail2() throws Exception {
        assertErrorCode(() -> {
            GetNodeResultDetailRequest request = FakerUtils.fake(GetNodeResultDetailRequest.class);
            request.setNodeId("alice");
            request.setDomainDataId("alice-ref");
            Domaindata.QueryDomainDataRequest queryDomainDataRequest = Domaindata.QueryDomainDataRequest.newBuilder()
                    .setData(Domaindata.QueryDomainDataRequestData.newBuilder()
                            .setDomainId("alice").setDomaindataId("alice-ref").build())
                    .build();
            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_RESULT_DETAIL));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildQueryDomainDataResponse(0);
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(queryDomainDataRequest)).thenReturn(queryDomainDataResponse);
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(any(), any())).thenReturn(queryDomainDataResponse);


            Mockito.when(projectRepository.findById(anyString())).thenReturn(Optional.of(ProjectDO.builder().projectId(PROJECT_ID).build()));
            Mockito.when(nodeRepository.findByNodeId(anyString())).thenReturn(buildNodeDOList().get(0));
            Mockito.when(projectJobRepository.findByJobId(anyString())).thenReturn(Optional.of(buildProjectJobDO(false)));
            Mockito.when(projectJobRepository.findById(any())).thenReturn(Optional.of(buildProjectJobDO(false)));

            Mockito.when(projectGraphRepository.findByGraphId(anyString(), anyString())).thenReturn(Optional.of(buildProjectGraphDO()));


            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList2();
            Mockito.when(resultRepository.findByNodeIdAndRefId(anyString(), anyString())).thenReturn(Optional.of(projectResultDOS.get(0)));
            Mockito.when(reportRepository.findById(any(ProjectReportDO.UPK.class))).thenReturn(Optional.empty());
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "getNodeResultDetail", GetNodeResultDetailRequest.class)).
                    content(JsonUtils.toJSONString(request));
        },GraphErrorCode.GRAPH_NODE_OUTPUT_NOT_EXISTS);
    }

    @Test
    void getNodeResultDetail3() throws Exception {
        assertResponse(() -> {
            GetNodeResultDetailRequest request = FakerUtils.fake(GetNodeResultDetailRequest.class);
            request.setNodeId("alice");
            request.setDomainDataId("alice-ref1");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_RESULT_DETAIL));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildQueryDomainDataResponse3(0);
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(any())).thenReturn(queryDomainDataResponse);
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(any(), any())).thenReturn(queryDomainDataResponse);

            Mockito.when(projectRepository.findById(anyString())).thenReturn(Optional.of(ProjectDO.builder().projectId(PROJECT_ID).build()));
            Mockito.when(nodeRepository.findByNodeId(anyString())).thenReturn(buildNodeDOList().get(0));
            Mockito.when(projectJobRepository.findByJobId(anyString())).thenReturn(Optional.of(buildProjectJobDO(false)));
            Mockito.when(projectJobRepository.findById(any())).thenReturn(Optional.of(buildProjectJobDO(false)));

            Mockito.when(projectGraphRepository.findByGraphId(anyString(), anyString())).thenReturn(Optional.of(buildProjectGraphDO()));

            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList2();
            Mockito.when(resultRepository.findByNodeIdAndRefId(anyString(), anyString())).thenReturn(Optional.of(projectResultDOS.get(0)));
            Mockito.when(reportRepository.findById(any(ProjectReportDO.UPK.class))).thenReturn(buildProjectReportDO());
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "getNodeResultDetail", GetNodeResultDetailRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }
    @Test
    void getNodeResultDetail4() throws Exception {
        assertResponse(() -> {
            GetNodeResultDetailRequest request = FakerUtils.fake(GetNodeResultDetailRequest.class);
            request.setNodeId("alice");
            request.setDomainDataId("alice-ref1");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_RESULT_DETAIL));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildQueryDomainDataResponse2(0);
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(any())).thenReturn(queryDomainDataResponse);
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(any(), any())).thenReturn(queryDomainDataResponse);

            Mockito.when(projectRepository.findById(anyString())).thenReturn(Optional.of(ProjectDO.builder().projectId(PROJECT_ID).build()));
            Mockito.when(nodeRepository.findByNodeId(anyString())).thenReturn(buildNodeDOList().get(0));
            Mockito.when(projectJobRepository.findByJobId(anyString())).thenReturn(Optional.of(buildProjectJobDO(false)));
            Mockito.when(projectJobRepository.findById(any())).thenReturn(Optional.of(buildProjectJobDO(false)));

            Mockito.when(projectGraphRepository.findByGraphId(anyString(), anyString())).thenReturn(Optional.of(buildProjectGraphDO()));

            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList2();
            Mockito.when(resultRepository.findByNodeIdAndRefId(anyString(), anyString())).thenReturn(Optional.of(projectResultDOS.get(0)));
            Mockito.when(reportRepository.findById(any(ProjectReportDO.UPK.class))).thenReturn(buildProjectReportDO2());
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "getNodeResultDetail", GetNodeResultDetailRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    private Optional<ProjectReportDO> buildProjectReportDO() {
        ProjectReportDO projectReportDO = ProjectReportDO.builder()
                .upk(new ProjectReportDO.UPK("testProjectId", "testReportId"))
                .content(sfContent)
                .build();
        return Optional.of(projectReportDO);
    }
    private Optional<ProjectReportDO> buildProjectReportDO2() {

        ProjectReportDO projectReportDO = ProjectReportDO.builder()
                .upk(new ProjectReportDO.UPK("testProjectId", "testReportId"))
                .content(scqlContent)
                .build();
        return Optional.of(projectReportDO);
    }


    @Test
    void getNodeResultDetailByProjectResultNotFoundException() throws Exception {
        assertErrorCode(() -> {
            GetNodeResultDetailRequest request = FakerUtils.fake(GetNodeResultDetailRequest.class);
            request.setNodeId("alice");
            request.setDomainDataId("alice-ref1");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_RESULT_DETAIL));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildQueryDomainDataResponse(0);
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(any())).thenReturn(queryDomainDataResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "getNodeResultDetail", GetNodeResultDetailRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ProjectErrorCode.PROJECT_RESULT_NOT_FOUND);
    }

    @Test
    void getNodeResultDetailByProjectNotExistsException() throws Exception {
        assertErrorCode(() -> {
            GetNodeResultDetailRequest request = FakerUtils.fake(GetNodeResultDetailRequest.class);
            request.setNodeId("alice");
            request.setDomainDataId("alice-ref1");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_RESULT_DETAIL));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildQueryDomainDataResponse(0);
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(any())).thenReturn(queryDomainDataResponse);

            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList();
            Mockito.when(resultRepository.findByNodeIdAndRefId(anyString(), anyString())).thenReturn(Optional.of(projectResultDOS.get(0)));

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "getNodeResultDetail", GetNodeResultDetailRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ProjectErrorCode.PROJECT_NOT_EXISTS);
    }

    @Test
    void getNodeResultDetailByProjectJobNotExistsException() throws Exception {
        assertErrorCode(() -> {
            GetNodeResultDetailRequest request = FakerUtils.fake(GetNodeResultDetailRequest.class);
            request.setNodeId("alice");
            request.setDomainDataId("alice-ref1");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_RESULT_DETAIL));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildQueryDomainDataResponse(0);
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(any())).thenReturn(queryDomainDataResponse);

            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList();
            Mockito.when(resultRepository.findByNodeIdAndRefId(anyString(), anyString())).thenReturn(Optional.of(projectResultDOS.get(0)));

            Mockito.when(projectRepository.findById(anyString())).thenReturn(Optional.of(ProjectDO.builder().projectId(PROJECT_ID).build()));

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "getNodeResultDetail", GetNodeResultDetailRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_NOT_EXISTS);
    }

    @Test
    void getNodeResultDetailByQueryDatatableFailedException() throws Exception {
        assertErrorCode(() -> {
            GetNodeResultDetailRequest request = FakerUtils.fake(GetNodeResultDetailRequest.class);
            request.setNodeId("alice");
            request.setDomainDataId("alice-ref1");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_RESULT_DETAIL));

            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList();
            Mockito.when(resultRepository.findByNodeIdAndRefId(anyString(), anyString())).thenReturn(Optional.of(projectResultDOS.get(0)));

            Mockito.when(projectRepository.findById(anyString())).thenReturn(Optional.of(ProjectDO.builder().projectId(PROJECT_ID).build()));

            Mockito.when(projectJobRepository.findById(any())).thenReturn(Optional.of(buildProjectJobDO(false)));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildQueryDomainDataResponse(1);
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(any())).thenReturn(queryDomainDataResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "getNodeResultDetail", GetNodeResultDetailRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, DatatableErrorCode.QUERY_DATATABLE_FAILED);
    }

    @Test
    void getNodeResultDetailByProjectJobNotExistsExceptionInGraphService() throws Exception {
        assertErrorCode(() -> {
            GetNodeResultDetailRequest request = FakerUtils.fake(GetNodeResultDetailRequest.class);
            request.setNodeId("alice");
            request.setDomainDataId("alice-ref1");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_RESULT_DETAIL));

            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList();
            Mockito.when(resultRepository.findByNodeIdAndRefId(anyString(), anyString())).thenReturn(Optional.of(projectResultDOS.get(0)));

            Mockito.when(projectRepository.findById(anyString())).thenReturn(Optional.of(ProjectDO.builder().projectId(PROJECT_ID).build()));

            Mockito.when(projectJobRepository.findById(any())).thenReturn(Optional.of(buildProjectJobDO(false)));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildEmptyQueryDomainDataResponse(0);
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(any())).thenReturn(queryDomainDataResponse);
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(any(), any())).thenReturn(queryDomainDataResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "getNodeResultDetail", GetNodeResultDetailRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_NOT_EXISTS);
    }

    @Test
    void getNodeResultDetailByProjectJobTaskNotExistsException() throws Exception {
        assertErrorCode(() -> {
            GetNodeResultDetailRequest request = FakerUtils.fake(GetNodeResultDetailRequest.class);
            request.setNodeId("alice");
            request.setDomainDataId("alice-ref1");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_RESULT_DETAIL));

            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList();
            Mockito.when(resultRepository.findByNodeIdAndRefId(anyString(), anyString())).thenReturn(Optional.of(projectResultDOS.get(0)));

            Mockito.when(projectRepository.findById(anyString())).thenReturn(Optional.of(ProjectDO.builder().projectId(PROJECT_ID).build()));

            Mockito.when(projectJobRepository.findById(any())).thenReturn(Optional.of(buildProjectJobDO(false)));
            Mockito.when(projectJobRepository.findByJobId(anyString())).thenReturn(Optional.of(buildProjectJobDO(true)));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildEmptyQueryDomainDataResponse(0);
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(any())).thenReturn(queryDomainDataResponse);
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(any(), any())).thenReturn(queryDomainDataResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "getNodeResultDetail", GetNodeResultDetailRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_TASK_NOT_EXISTS);
    }

    @Test
    void getNodeResultDetailByDatatableNotExistsException() throws Exception {
        assertErrorCode(() -> {
            GetNodeResultDetailRequest request = FakerUtils.fake(GetNodeResultDetailRequest.class);
            request.setNodeId("alice");
            request.setDomainDataId("alice-ref1");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_RESULT_DETAIL));

            List<ProjectResultDO> projectResultDOS = buildProjectResultDOList();
            Mockito.when(resultRepository.findByNodeIdAndRefId(anyString(), anyString())).thenReturn(Optional.of(projectResultDOS.get(0)));

            Mockito.when(projectRepository.findById(anyString())).thenReturn(Optional.of(ProjectDO.builder().projectId(PROJECT_ID).build()));

            Mockito.when(projectJobRepository.findById(any())).thenReturn(Optional.of(buildProjectJobDO(false)));
            Mockito.when(projectJobRepository.findByJobId(anyString())).thenReturn(Optional.of(buildProjectJobDO(false)));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildEmptyQueryDomainDataResponse(0);
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(any())).thenReturn(queryDomainDataResponse);
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(any(), any())).thenReturn(queryDomainDataResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "getNodeResultDetail", GetNodeResultDetailRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, DatatableErrorCode.DATATABLE_NOT_EXISTS);
    }

    @Test
    void updateNode() throws Exception {
        assertResponse(() -> {
            UpdateNodeRequest request = FakerUtils.fake(UpdateNodeRequest.class);
            request.setNodeId("alice");
            request.setNetAddress("127.0.0.1:28080");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_UPDATE));
            NodeDO alice = NodeDO.builder().nodeId(request.getNodeId()).build();
            Mockito.when(nodeRepository.findByNodeId(any())).thenReturn(alice);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "update", UpdateNodeRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void updateNodeByNodeNotExistsException() throws Exception {
        assertErrorCode(() -> {
            UpdateNodeRequest request = FakerUtils.fake(UpdateNodeRequest.class);
            request.setNodeId("alice");
            request.setNetAddress("127.0.0.1:28080");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_UPDATE));
            Mockito.when(nodeRepository.findByNodeId(any())).thenReturn(null);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "update", UpdateNodeRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.NODE_NOT_EXIST_ERROR);
    }

    @Test
    void pageNode() throws Exception {
        assertResponse(() -> {
            PageNodeRequest request = FakerUtils.fake(PageNodeRequest.class);
            request.setSearch("");
            request.setPage(1);
            request.setSize(10);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_PAGE));
            NodeDO alice = NodeDO.builder().nodeId("alice").build();
            List<NodeDO> list = new ArrayList<>();
            list.add(alice);
            Page<NodeDO> page = new PageImpl<>(list);
            Mockito.when(nodeRepository.findAll(Specification.anyOf(), request.of())).thenReturn(page);
            Mockito.when(nodeRepository.findByNodeId(any())).thenReturn(alice);
            DomainOuterClass.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
            Mockito.when(kusciaGrpcClientAdapter.queryDomain(any())).thenReturn(queryDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "page", PageNodeRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void pageNodeByKusciaNodeNotExists() throws Exception {
        assertResponse(() -> {
            PageNodeRequest request = FakerUtils.fake(PageNodeRequest.class);
            request.setSearch("");
            request.setPage(1);
            request.setSize(10);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_PAGE));
            NodeDO alice = NodeDO.builder().nodeId("alice").build();
            Page<NodeDO> page = new PageImpl<>(buildNodeDOList());
            Mockito.when(nodeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            Mockito.when(nodeRepository.findByNodeId(any())).thenReturn(alice);
            DomainOuterClass.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(-1);
            Mockito.when(kusciaGrpcClientAdapter.queryDomain(any())).thenReturn(queryDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "page", PageNodeRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void pageNodeByNodeNotExists() throws Exception {
        assertErrorCode(() -> {
            PageNodeRequest request = FakerUtils.fake(PageNodeRequest.class);
            request.setSearch("");
            request.setPage(1);
            request.setSize(10);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_PAGE));
            NodeDO alice = NodeDO.builder().nodeId("alice").build();
            Page<NodeDO> page = new PageImpl<>(buildNodeDOList());
            Mockito.when(nodeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            Mockito.when(nodeRepository.findByNodeId(any())).thenReturn(null);
            DomainOuterClass.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
            Mockito.when(kusciaGrpcClientAdapter.queryDomain(any())).thenReturn(queryDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "page", PageNodeRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.NODE_NOT_EXIST_ERROR);
    }

    @Test
    void getNode() throws Exception {
        assertResponse(() -> {
            NodeIdRequest request = FakerUtils.fake(NodeIdRequest.class);
            request.setNodeId("alice");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_GET));
            NodeDO alice = NodeDO.builder().nodeId("alice").build();
            Mockito.when(nodeRepository.findByNodeId(any())).thenReturn(alice);
            DomainOuterClass.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
            Mockito.when(kusciaGrpcClientAdapter.queryDomain(any())).thenReturn(queryDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "get", NodeIdRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void getNodeByNodeNotExists() throws Exception {
        assertErrorCode(() -> {
            NodeIdRequest request = FakerUtils.fake(NodeIdRequest.class);
            request.setNodeId("alice");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_GET));
            Mockito.when(nodeRepository.findByNodeId(any())).thenReturn(null);
            DomainOuterClass.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
            Mockito.when(kusciaGrpcClientAdapter.queryDomain(any())).thenReturn(queryDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "get", NodeIdRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.NODE_NOT_EXIST_ERROR);
    }

    @Test
    void getNodeByKusciaNodeNotExists() throws Exception {
        assertResponse(() -> {
            NodeIdRequest request = FakerUtils.fake(NodeIdRequest.class);
            request.setNodeId("alice");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_GET));
            NodeDO alice = NodeDO.builder().nodeId("alice").build();
            Mockito.when(nodeRepository.findByNodeId(any())).thenReturn(alice);
            DomainOuterClass.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(-1);
            Mockito.when(kusciaGrpcClientAdapter.queryDomain(any())).thenReturn(queryDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "get", NodeIdRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void tokenNode() throws Exception {
        assertResponse(() -> {
            NodeTokenRequest request = FakerUtils.fake(NodeTokenRequest.class);
            request.setNodeId("alice");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_TOKEN));
            NodeDO alice = NodeDO.builder().nodeId("alice").build();
            Mockito.when(nodeRepository.findByNodeId(any())).thenReturn(alice);
            DomainOuterClass.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
            queryDomainResponse = queryDomainResponse.toBuilder().setData(
                    DomainOuterClass.QueryDomainResponseData.newBuilder().addDeployTokenStatuses(
                            DomainOuterClass.DeployTokenStatus.newBuilder().setToken("123").setState("used").buildPartial()).build()).build();
            Mockito.when(kusciaGrpcClientAdapter.queryDomain(any())).thenReturn(queryDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "token", NodeTokenRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void tokenNodeByKusciaNodeNotExists() throws Exception {
        assertErrorCode(() -> {
            NodeTokenRequest request = FakerUtils.fake(NodeTokenRequest.class);
            request.setNodeId("alice");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_TOKEN));
            NodeDO alice = NodeDO.builder().nodeId("alice").build();
            Mockito.when(nodeRepository.findByNodeId(any())).thenReturn(alice);
            DomainOuterClass.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(-1);
            Mockito.when(kusciaGrpcClientAdapter.queryDomain(any())).thenReturn(queryDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "token", NodeTokenRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NODE_TOKEN_IS_EMPTY_ERROR);
    }

    @Test
    void refreshNode() throws Exception {
        assertResponse(() -> {
            NodeIdRequest request = FakerUtils.fake(NodeIdRequest.class);
            request.setNodeId("alice");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_REFRESH));
            NodeDO alice = NodeDO.builder().nodeId("alice").build();
            Mockito.when(nodeRepository.findByNodeId(any())).thenReturn(alice);
            DomainOuterClass.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
            Mockito.when(kusciaGrpcClientAdapter.queryDomain(any())).thenReturn(queryDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "refresh", NodeIdRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void refreshNodeByKusciaNodeNotExists() throws Exception {
        assertResponse(() -> {
            NodeIdRequest request = FakerUtils.fake(NodeIdRequest.class);
            request.setNodeId("alice");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_REFRESH));
            NodeDO alice = NodeDO.builder().nodeId("alice").build();
            Mockito.when(nodeRepository.findByNodeId(any())).thenReturn(alice);
            DomainOuterClass.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(-1);
            Mockito.when(kusciaGrpcClientAdapter.queryDomain(any())).thenReturn(queryDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "refresh", NodeIdRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }
}